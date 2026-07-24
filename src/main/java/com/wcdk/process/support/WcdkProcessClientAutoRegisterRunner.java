package com.wcdk.process.support;

import com.wcdk.process.WcdkProcessClient;
import com.wcdk.process.WcdkProcessConnectionConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @auther WCDK
 * @date 2026/7/17
 * @version 1.0
 **/
@Slf4j
public class WcdkProcessClientAutoRegisterRunner implements ApplicationRunner, DisposableBean {

    private final WcdkProcessClient wcdkProcessClient;

    private final ProcessBeanRegistry processBeanRegistry;

    private final Duration activeReportInterval;

    private final ScheduledExecutorService registerExecutor = Executors.newSingleThreadScheduledExecutor(runnable -> {
        Thread thread = new Thread(runnable, "wcdk-process-client-register");
        thread.setDaemon(true);
        return thread;
    });

    private final AtomicBoolean started = new AtomicBoolean(false);

    private final AtomicBoolean destroyed = new AtomicBoolean(false);

    private final AtomicBoolean registering = new AtomicBoolean(false);

    private final AtomicReference<Disposable> currentRegister = new AtomicReference<>();

    public WcdkProcessClientAutoRegisterRunner(WcdkProcessClient wcdkProcessClient,
                                               ProcessBeanRegistry processBeanRegistry,
                                               WcdkProcessConnectionConfig connectionConfig) {
        this.wcdkProcessClient = wcdkProcessClient;
        this.processBeanRegistry = processBeanRegistry;
        this.activeReportInterval = connectionConfig.getActiveReportInterval();
    }

    @Override
    public void run(ApplicationArguments args) {
        if (destroyed.get()) {
            return;
        }
        if (!started.compareAndSet(false, true)) {
            return;
        }
        registerClient();
        long registerIntervalSeconds = resolveRegisterIntervalSeconds();
        registerExecutor.scheduleWithFixedDelay(this::registerClient,
                registerIntervalSeconds,
                registerIntervalSeconds,
                TimeUnit.SECONDS);
    }

    @Override
    public void destroy() {
        destroyed.set(true);
        started.set(false);
        Disposable disposable = currentRegister.getAndSet(null);
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
        registerExecutor.shutdown();
    }

    private void registerClient() {
        if (destroyed.get() || !registering.compareAndSet(false, true)) {
            return;
        }
        Disposable disposable = wcdkProcessClient.registerClient(processBeanRegistry.getProcessBeanNames())
//                .doOnSuccess(unused -> log.info("流程客户端注册信息已发送，processBeanNames={}", processBeanRegistry.getProcessBeanNames()))
                .doOnError(exception -> log.warn("流程客户端注册信息发送失败", exception))
                .onErrorResume(exception -> Mono.empty())
                .doFinally(signalType -> {
                    registering.set(false);
                    currentRegister.compareAndSet(currentRegister.get(), null);
                })
                .subscribe();
        currentRegister.set(disposable);
    }

    private long resolveRegisterIntervalSeconds() {
        if (activeReportInterval == null || activeReportInterval.isNegative() || activeReportInterval.isZero()) {
            return 10L;
        }
        return Math.max(1L, activeReportInterval.toSeconds());
    }
}
