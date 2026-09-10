package com.wcdk.process.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.wcdk.process.annotation.ProcessBean;
import com.wcdk.process.dto.WcdkProcessConnectionEvent;
import jakarta.annotation.PostConstruct;
import org.reactivestreams.Publisher;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * @auther WCDK
 *
 * @version 1.0
 **/
public class ProcessBeanRegistry {

    private final ApplicationContext applicationContext;

    private final ObjectMapper objectMapper;

    private final Map<String, ProcessBeanInvoker> invokerMap = new LinkedHashMap<>();

    public ProcessBeanRegistry(ApplicationContext applicationContext, ObjectMapper objectMapper) {
        this.applicationContext = applicationContext;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void registerProcessBean() {
        for (String beanName : applicationContext.getBeanDefinitionNames()) {
            Class<?> beanType = applicationContext.getType(beanName);
            if (beanType == null) {
                continue;
            }
            ReflectionUtils.doWithMethods(beanType, method -> registerMethod(beanName, method),
                    method -> method.isAnnotationPresent(ProcessBean.class));
        }
    }

    public Mono<Object> invoke(String processBeanName, WcdkProcessConnectionEvent event) {
        ProcessBeanInvoker invoker = invokerMap.get(processBeanName);
        if (invoker == null) {
            return Mono.error(new IllegalArgumentException("未注册对应的流程处理接口：" + processBeanName));
        }
        return invoker.invoke(event);
    }

    public Set<String> getProcessBeanNames() {
        return invokerMap.keySet();
    }

    private void registerMethod(String beanName, Method method) {
        ProcessBean processBean = method.getAnnotation(ProcessBean.class);
        String processBeanName = processBean.value();
        if (!StringUtils.hasText(processBeanName)) {
            throw new IllegalStateException("ProcessBean 注解必须指定接口名称，方法：" + method.getName());
        }
        if (method.getParameterCount() > 1) {
            throw new IllegalStateException("ProcessBean 标注的方法最多只能有一个参数，方法：" + method.getName());
        }
        if (invokerMap.containsKey(processBeanName)) {
            throw new IllegalStateException("流程处理接口名称重复：" + processBeanName);
        }
        invokerMap.put(processBeanName, new ProcessBeanInvoker(applicationContext, objectMapper, beanName, method));
    }

    private static final class ProcessBeanInvoker {

        private final ApplicationContext applicationContext;

        private final ObjectMapper objectMapper;

        private final String beanName;

        private final Method method;

        private ProcessBeanInvoker(ApplicationContext applicationContext, ObjectMapper objectMapper, String beanName, Method method) {
            this.applicationContext = applicationContext;
            this.objectMapper = objectMapper;
            this.beanName = beanName;
            this.method = method;
        }

        private Mono<Object> invoke(WcdkProcessConnectionEvent event) {
            try {
                Object bean = applicationContext.getBean(beanName);
                Method targetMethod = AopUtils.getTargetClass(bean).getMethod(method.getName(), method.getParameterTypes());
                ReflectionUtils.makeAccessible(targetMethod);
                Object result;
                if (targetMethod.getParameterCount() == 0) {
                    result = targetMethod.invoke(bean);
                } else {
                    result = targetMethod.invoke(bean, resolveArgument(targetMethod.getParameterTypes()[0], event));
                }
                return adaptResult(result);
            } catch (Exception exception) {
                return Mono.error(new IllegalStateException("执行流程处理接口失败：" + method.getName(), exception));
            }
        }

        private Mono<Object> adaptResult(Object result) {
            if (result == null) {
                return Mono.empty();
            }
            if (result instanceof Mono<?> mono) {
                return mono.cast(Object.class);
            }
            if (result instanceof Flux<?> flux) {
                return flux.collectList().cast(Object.class);
            }
            if (result instanceof Publisher<?> publisher) {
                return Mono.from(publisher).cast(Object.class);
            }
            return Mono.just(result);
        }

        private Object resolveArgument(Class<?> parameterType, WcdkProcessConnectionEvent event) {
            if (WcdkProcessConnectionEvent.class.isAssignableFrom(parameterType)) {
                return event;
            }
            Map<String, Object> requestBody = event == null
                    ? new LinkedHashMap<>()
                    : objectMapper.convertValue(event, Map.class);
            if (Map.class.isAssignableFrom(parameterType) || Object.class.equals(parameterType)) {
                return requestBody;
            }
            return objectMapper.copy()
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    .convertValue(requestBody, parameterType);
        }
    }
}
