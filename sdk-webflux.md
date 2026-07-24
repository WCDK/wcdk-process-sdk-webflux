# WCDK Process WebFlux SDK 使用手册

版本：`wcdk-process-sdk-webflux 1.0.0`  
适用项目：Spring Boot 3.5.x / Spring WebFlux / Java 21  
包名：`com.wcdk.process`

## 1. SDK 概述

`wcdk-process-sdk-webflux` 用于 WebFlux 业务系统接入 WCDK 流程中心。SDK 提供两类能力：

- 客户端自动注册：业务系统启动后，自动向流程中心注册 `clientId`、回调地址和本地 `@ProcessBean` 处理器名称。
- 流程接口调用：通过 `WcdkProcessFlowClient` 以 `Mono` 方式调用流程申请、流程实例、任务、模型、部署和客户端管理接口。

核心入口如下：

| 类型 | 类名 | 说明 |
|---|---|---|
| 自动装配 | `WcdkProcessAutoConfiguration` | 根据 `wcdk.process` 配置自动创建 SDK Bean |
| 流程客户端 | `WcdkProcessFlowClient` | 推荐业务代码直接使用的反应式流程方法封装 |
| 通用客户端 | `WcdkProcessClient` | 底层 `WebClient` 调用封装，可调用自定义路径 |
| 回调注解 | `@ProcessBean` | 标记业务系统内的流程回调处理方法 |
| 回调入口 | `WcdkProcessBeanController` | 自动暴露 `POST /wcdk_process/{processBeanName}` |
| 回调鉴权 | `WcdkProcessAuthWebFilter` | WebFlux `WebFilter`，校验 `WCDK_AUTH` 请求头 |

## 2. Maven 依赖

在 WebFlux 业务系统中引入 SDK：

```xml
<dependency>
    <groupId>com.wcdk.process</groupId>
    <artifactId>wcdk-process-sdk-webflux</artifactId>
    <version>1.0.0</version>
</dependency>
```

SDK 基于 `spring-boot-starter-webflux`，不依赖 `spring-webmvc` 和 Servlet API。

## 3. 配置说明

在业务系统 `application.yaml` 中配置：

```yaml
wcdk:
  process:
    client-id: demo-client
    client-name: wcdk-process-demo
    endpoint: http://localhost:58082
    timeout-seconds: 30
    username: admin
    password: admin123
    callback-url: http://localhost:58083
    auth-flg: WCDK
    active-report: 10
```
```微服务配置
wcdk:
  process:
    client-id: ${spring.application.name}
    client-name: ${spring.application.name}
    service-name: ${spring.application.name}
    endpoint: lb://wcdk-process
    timeout-seconds: 30
    username: admin
    password: admin123
    auth-flg: WCDK
    active-report: 10
```
| 配置项 | 必填 | 默认值 | 说明 |
|---|---:|---|---|
| `client-id` | 是 | 无 | 业务系统客户端唯一标识 |
| `client-name` | 是 | 无 | 客户端展示名称 |
| `endpoint` | 是 | 无 | WCDK 流程中心服务地址 |
| `username` | 是 | 无 | 调用流程中心接口的 Basic Auth 用户名 |
| `password` | 是 | 无 | 调用流程中心接口的 Basic Auth 密码 |
| `callback-url` | 否 | 空 | 业务系统回调根地址，例如 `http://localhost:58083` |
| `timeout-seconds` | 是 | `30` | HTTP 请求超时时间，单位秒 |
| `active-report` | 是 | `10` | 客户端注册心跳上报间隔，单位秒，最小值为 1 |
| `auth-flg` | 否 | 空 | 回调认证标识，流程中心回调时通过 `WCDK_AUTH` 请求头传入 |

## 4. 自动装配 Bean

引入 SDK 并完成配置后，Spring 容器会自动创建以下 Bean：

| Bean | 类型 | 说明 |
|---|---|---|
| `wcdkProcessConnectionConfig` | `WcdkProcessConnectionConfig` | 客户端连接配置 |
| `wcdkProcessServerConfig` | `WcdkProcessServerConfig` | 流程中心服务端配置 |
| `wcdkProcessWebClientBuilder` | `WebClient.Builder` | SDK 缺省 `WebClient` 构建器 |
| `wcdkProcessWebClient` | `WebClient` | SDK 调用流程中心使用的反应式 HTTP 客户端 |
| `wcdkProcessObjectMapper` | `ObjectMapper` | JSON 序列化组件 |
| `wcdkProcessClient` | `WcdkProcessClient` | 通用 HTTP 调用客户端 |
| `wcdkProcessFlowClient` | `WcdkProcessFlowClient` | 流程业务调用客户端 |
| `processBeanRegistry` | `ProcessBeanRegistry` | 扫描和调用 `@ProcessBean` 方法 |
| `wcdkProcessBeanController` | `WcdkProcessBeanController` | 暴露本地回调接口 |
| `wcdkProcessAuthWebFilter` | `WcdkProcessAuthWebFilter` | 回调鉴权过滤器 |
| `wcdkProcessClientAutoRegisterRunner` | `WcdkProcessClientAutoRegisterRunner` | 应用启动后自动注册客户端并定时上报 |

`processBeanRegistry`、`wcdkProcessBeanController`、`wcdkProcessAuthWebFilter` 和自动注册 Runner 仅在 Reactive Web 应用中启用。

## 5. 快速开始

### 5.1 注入流程客户端

```java
import com.wcdk.process.WcdkProcessFlowClient;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class DemoProcessService {

    @Resource
    private WcdkProcessFlowClient flowClient;
}
```

### 5.2 创建流程申请

```java
import com.wcdk.process.dto.ProcessRequestCreateRequest;
import com.wcdk.process.dto.ProcessRequestResponse;
import reactor.core.publisher.Mono;

import java.util.Map;

public Mono<ProcessRequestResponse> createLeaveRequest() {
    ProcessRequestCreateRequest request = ProcessRequestCreateRequest.builder()
            .processDefinitionKey("leave_process")
            .taskName("请假申请")
            .formData(Map.of("days", 2, "reason", "年休假"))
            .submit(true)
            .processBeanName("leaveHandler")
            .build();

    return flowClient.createProcessRequest(request);
}
```

### 5.3 审批流程申请

```java
import com.wcdk.process.dto.ProcessRequestApproveRequest;
import reactor.core.publisher.Mono;

public Mono<Void> approve(String taskId) {
    ProcessRequestApproveRequest request = ProcessRequestApproveRequest.builder()
            .taskId(taskId)
            .approved(true)
            .comment("同意")
            .build();

    return flowClient.approveProcessRequest(request);
}
```

### 5.4 在 Controller 中返回 Mono

```java
import com.wcdk.process.dto.ProcessRequestResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class DemoController {

    private final DemoProcessService demoProcessService;

    public DemoController(DemoProcessService demoProcessService) {
        this.demoProcessService = demoProcessService;
    }

    @GetMapping("/demo/process/{id}")
    public Mono<ProcessRequestResponse> detail(@PathVariable Long id) {
        return demoProcessService.getProcessRequest(id);
    }
}
```

## 6. 回调处理

SDK 会在业务系统中暴露：

```text
POST /wcdk_process/{processBeanName}
```

流程中心调用该地址时，SDK 根据 `processBeanName` 找到本地 `@ProcessBean` 方法并执行。

### 6.1 普通回调方法

```java
import com.wcdk.process.annotation.ProcessBean;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class DemoProcessHandler {

    @ProcessBean("leaveHandler")
    public String handleLeave(Map<String, Object> payload) {
        return "处理完成";
    }
}
```

### 6.2 反应式回调方法

```java
import com.wcdk.process.annotation.ProcessBean;
import com.wcdk.process.dto.WcdkProcessConnectionEvent;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class ReactiveProcessHandler {

    @ProcessBean("reactiveLeaveHandler")
    public Mono<String> handle(WcdkProcessConnectionEvent event) {
        return Mono.just(event.getBusinessKey());
    }
}
```

`@ProcessBean` 方法规则：

- `value` 必须填写，并且同一应用内不能重复。
- 方法最多只能有一个参数。
- 参数可以是 `WcdkProcessConnectionEvent`、`Map`、`Object`，也可以是自定义 DTO。
- 自定义 DTO 会从 `WcdkProcessConnectionEvent.payload` 转换。
- 返回值可以是普通对象、`Mono`、`Flux` 或其他 `Publisher`。
- `Flux` 返回值会被收集为列表后放入 `ApiResponse.data`。

### 6.3 回调鉴权

如果配置了：

```yaml
wcdk:
  process:
    auth-flg: WCDK
```

流程中心回调业务系统时必须带请求头：

```text
WCDK_AUTH: WCDK
```

不匹配时返回：

```json
{"code":401,"message":"回调鉴权失败","data":null}
```

## 7. 返回包装与异常

流程中心接口统一返回：

```java
ApiResponse<T>
```

SDK 内部会自动解析 `data`，业务侧拿到的是 `Mono<T>`。

当 HTTP 状态码不是 2xx，或 `ApiResponse.code != 200` 时，`Mono` 会以 `WcdkProcessClientException` 结束。

示例：

```java
flowClient.getProcessRequest(1L)
        .doOnError(WcdkProcessClientException.class, exception -> {
            // 按业务需要记录日志或转换异常
        });
```

分页返回类型：

```java
PageResponse<T>
```

字段包括 `total`、`pageNum`、`pageSize`、`records`。

## 8. WcdkProcessFlowClient 方法清单

### 8.1 流程申请

| 方法 | 返回值 | 说明 |
|---|---|---|
| `createProcessRequest(ProcessRequestCreateRequest request)` | `Mono<ProcessRequestResponse>` | 创建流程申请，可按 `submit` 决定是否立即提交 |
| `submitProcessRequest(Long id)` | `Mono<ProcessRequestResponse>` | 提交已创建的流程申请 |
| `getProcessRequest(Long id)` | `Mono<ProcessRequestResponse>` | 查询流程申请详情 |
| `getProcessRequestDiagramDetail(Long id)` | `Mono<ProcessDefinitionDetailResponse>` | 查询申请对应流程图详情和当前活动节点 |
| `listProcessRequest(...)` | `Mono<PageResponse<ProcessRequestResponse>>` | 分页查询流程申请 |
| `approveProcessRequest(ProcessRequestApproveRequest request)` | `Mono<Void>` | 审批流程申请当前任务 |
| `deleteProcessRequest(Long id, String deleteReason)` | `Mono<Void>` | 删除流程申请 |

### 8.2 流程实例与任务

| 方法 | 返回值 | 说明 |
|---|---|---|
| `startProcess(StartProcessRequest request)` | `Mono<ProcessInstanceResponse>` | 直接启动流程实例 |
| `getProcessInstance(String processInstanceId)` | `Mono<ProcessInstanceResponse>` | 查询流程实例 |
| `listTask(String assignee)` | `Mono<List<TaskResponse>>` | 查询指定办理人的待办任务 |
| `completeTask(TaskCompleteRequest request)` | `Mono<Void>` | 完成任务并提交变量 |
| `deleteProcessInstance(String processInstanceId, String deleteReason)` | `Mono<Void>` | 删除流程实例 |
| `deleteTask(String taskId, String deleteReason)` | `Mono<Void>` | 删除任务 |

### 8.3 模型管理

| 方法 | 返回值 | 说明 |
|---|---|---|
| `createModel(ModelCreateRequest request)` | `Mono<ModelResponse>` | 创建流程模型 |
| `updateModel(String modelId, ModelUpdateRequest request)` | `Mono<ModelResponse>` | 更新流程模型 |
| `listModel()` | `Mono<List<ModelResponse>>` | 查询全部模型 |
| `listModel(String modelName, String modelKey, String category, String deployed)` | `Mono<List<ModelResponse>>` | 按条件查询模型 |
| `getModelXml(String modelId)` | `Mono<String>` | 查询模型 BPMN XML |
| `deployModel(String modelId, String processBeanName)` | `Mono<DeploymentResponse>` | 部署模型，不指定客户端 |
| `deployModel(String modelId, String clientId, String processBeanName)` | `Mono<DeploymentResponse>` | 部署模型并绑定客户端回调 |
| `deleteModel(String modelId)` | `Mono<Void>` | 删除模型 |

`deployModel(modelId, clientId, processBeanName)` 要求 `clientId` 与 `processBeanName` 同时为空或同时非空。

### 8.4 设计器导出

| 方法 | 返回值 | 说明 |
|---|---|---|
| `exportDesignerProcess(ProcessDesignerExportRequest request)` | `Mono<ProcessDesignerExportResponse>` | 将前端设计器节点和连线导出为 BPMN 文件内容 |

### 8.5 部署管理

| 方法 | 返回值 | 说明 |
|---|---|---|
| `deployProcess(String deploymentName, String category, String clientId, String processBeanName, Path filePath)` | `Mono<DeploymentResponse>` | 上传本地 BPMN 文件并部署 |
| `deployProcess(String deploymentName, String category, String clientId, String processBeanName, String fileName, String contentType, byte[] fileContent)` | `Mono<DeploymentResponse>` | 上传字节数组并部署 |
| `listDeployment()` | `Mono<List<DeploymentResponse>>` | 查询全部部署 |
| `listDeployment(String deploymentName, String category, String clientId)` | `Mono<List<DeploymentResponse>>` | 按条件查询部署 |
| `listDeployClient(Long pageNum, Long pageSize, String clientId, String clientName)` | `Mono<PageResponse<WcdkProcessClientResponse>>` | 查询可用于部署绑定的客户端 |
| `listClientProcessBean(String clientId)` | `Mono<List<String>>` | 查询客户端注册的 `ProcessBean` 名称 |
| `listProcessDefinition()` | `Mono<List<ProcessDefinitionResponse>>` | 查询流程定义 |
| `getProcessDefinitionDetail(String processDefinitionId)` | `Mono<ProcessDefinitionDetailResponse>` | 查询流程定义详情、表单、按钮和流程图结构 |
| `updateDeploymentBinding(String deploymentId, DeploymentBindingUpdateRequest request)` | `Mono<Void>` | 更新部署与客户端处理器的绑定关系 |
| `deleteDeployment(String deploymentId, Boolean cascade)` | `Mono<Void>` | 删除部署 |

部署示例：

```java
import com.wcdk.process.dto.DeploymentResponse;
import reactor.core.publisher.Mono;

import java.nio.file.Path;

public Mono<DeploymentResponse> deploy() {
    return flowClient.deployProcess(
            "请假流程部署",
            "HR",
            "demo-client",
            "leaveHandler",
            Path.of("processes/leave.bpmn20.xml")
    );
}
```

### 8.6 客户端管理

| 方法 | 返回值 | 说明 |
|---|---|---|
| `listClient(Long pageNum, Long pageSize, String clientId, String clientName, String callbackUrl, String processBeanName, String sortProp, String sortOrder)` | `Mono<PageResponse<WcdkProcessClientResponse>>` | 分页查询已注册客户端 |
| `detectClient(String clientId)` | `Mono<Boolean>` | 探测客户端是否可用 |
| `removeClient(String clientId)` | `Mono<Void>` | 移除客户端 |

## 9. WcdkProcessClient 通用方法

当 `WcdkProcessFlowClient` 没有封装某个接口时，可以使用底层 `WcdkProcessClient` 调用自定义路径。

| 方法 | 返回值 | 说明 |
|---|---|---|
| `registerClient(Set<String> processBeanNames)` | `Mono<Void>` | 向流程中心注册客户端和本地处理器名称 |
| `callback(WcdkProcessConnectionEvent event)` | `Mono<Void>` | 向流程中心发送连接流程事件 |
| `postForVoid(String path, Object body)` | `Mono<Void>` | 发送 POST 请求，无返回数据 |
| `post(String path, Object body, Class<T> responseType)` | `Mono<T>` | 发送 POST 请求，按 Class 解析返回数据 |
| `post(String path, Object body, TypeReference<T> responseType)` | `Mono<T>` | 发送 POST 请求，支持泛型返回值 |
| `putForVoid(String path, Object body)` | `Mono<Void>` | 发送 PUT 请求，无返回数据 |
| `put(String path, Object body, Class<T> responseType)` | `Mono<T>` | 发送 PUT 请求，按 Class 解析返回数据 |
| `put(String path, Object body, TypeReference<T> responseType)` | `Mono<T>` | 发送 PUT 请求，支持泛型返回值 |
| `get(String path, Class<T> responseType)` | `Mono<T>` | 发送 GET 请求 |
| `get(String path, TypeReference<T> responseType)` | `Mono<T>` | 发送 GET 请求，支持泛型返回值 |
| `get(String path, Map<String, ?> queryParams, Class<T> responseType)` | `Mono<T>` | 发送带查询参数的 GET 请求 |
| `get(String path, Map<String, ?> queryParams, TypeReference<T> responseType)` | `Mono<T>` | 发送带查询参数的 GET 请求，支持泛型返回值 |
| `postMultipart(...)` | `Mono<T>` | 发送 `multipart/form-data` 文件上传请求 |
| `delete(String path, Map<String, ?> queryParams)` | `Mono<Void>` | 发送 DELETE 请求 |

通用客户端约定：

- `path` 使用流程中心接口路径，例如 `/process/request/list`。
- SDK 使用配置中的 `endpoint` 作为 `WebClient` 的 `baseUrl`。
- JSON 请求自动设置 `Accept: application/json` 和 `Content-Type: application/json`。
- 文件上传请求使用 `multipart/form-data`。
- 调用流程中心时自动设置 `Authorization: Basic base64(username:password)`。
- 响应必须符合 `ApiResponse<T>` 结构，SDK 返回其中的 `data`。

## 10. 推荐接入流程

1. 在 WebFlux 业务系统中引入 `wcdk-process-sdk-webflux`。
2. 配置 `wcdk.process` 连接信息。
3. 定义至少一个 `@ProcessBean` 处理流程回调。
4. 启动业务系统，确认 SDK 自动注册客户端成功。
5. 在流程中心部署流程，并绑定 `clientId` 与 `processBeanName`。
6. 使用 `WcdkProcessFlowClient` 创建申请、提交、审批或查询流程数据。

## 11. 常见问题

### 11.1 启动时报配置校验失败

检查 `client-id`、`client-name`、`endpoint`、`username`、`password` 是否为空。SDK 对这些配置启用了校验。

### 11.2 流程中心无法回调业务系统

检查：

- `callback-url` 是否能被流程中心访问。
- 业务系统是否暴露了 `/wcdk_process/{processBeanName}`。
- `@ProcessBean` 名称是否与流程部署绑定的 `processBeanName` 一致。
- 如果启用了 `auth-flg`，确认流程中心回调请求头 `WCDK_AUTH` 与业务系统配置一致。

### 11.3 `ProcessBean` 方法无法注册

检查：

- 方法是否位于 Spring 管理的 Bean 中。
- `@ProcessBean` 注解值是否为空。
- 同一应用内是否存在重复的 `@ProcessBean` 名称。
- 方法参数是否超过一个。

### 11.4 SDK 调用返回 `WcdkProcessClientException`

常见原因：

- 流程中心地址不可达。
- Basic Auth 用户名或密码错误。
- 流程中心接口返回非 2xx 状态码。
- 流程中心返回的 `ApiResponse.code` 不是 `200`。
- 请求参数与服务端接口要求不一致。

### 11.5 如何在非反应式代码中临时调用

WebFlux 业务链路中推荐继续返回 `Mono`，不要主动阻塞。如果确实在启动任务或一次性脚本中需要等待结果，可以显式调用：

```java
ProcessRequestResponse response = flowClient.getProcessRequest(1L).block();
```

业务接口、过滤器和回调方法中不建议使用 `block()`。
