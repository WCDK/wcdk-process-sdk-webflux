package com.wcdk.process;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wcdk.process.common.ApiResponse;
import com.wcdk.process.dto.WcdkProcessClientRegisterRequest;
import com.wcdk.process.dto.WcdkProcessConnectionEvent;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.Set;

/**
 * @auther WCDK
 * @date 2026/7/16
 * @version 1.0
 **/
public class WcdkProcessClient {

    private static final String REGISTER_PATH = "/sdk/wcdkprocess/clients/register";

    private static final String CALLBACK_PATH = "/sdk/wcdkprocess/callback";

    private final WebClient webClient;

    private final ObjectMapper objectMapper;

    private final WcdkProcessConnectionConfig connectionConfig;

    private final WcdkProcessServerConfig serverConfig;

    public WcdkProcessClient(WebClient webClient,
                             ObjectMapper objectMapper,
                             WcdkProcessConnectionConfig connectionConfig,
                             WcdkProcessServerConfig serverConfig) {
        this.webClient = webClient;
        this.objectMapper = objectMapper;
        this.connectionConfig = connectionConfig;
        this.serverConfig = serverConfig;
    }

    public Mono<Void> registerClient(Set<String> processBeanNames) {
        WcdkProcessClientRegisterRequest request = WcdkProcessClientRegisterRequest.builder()
                .clientId(connectionConfig.getClientId())
                .clientName(connectionConfig.getClientName())
                .username(connectionConfig.getUsername())
                .password(connectionConfig.getPassword())
                .callbackUrl(connectionConfig.getCallbackUrl())
                .authFlg(connectionConfig.getAuthFlg())
                .processBeanNames(processBeanNames)
                .build();
        return postForVoid(REGISTER_PATH, request);
    }

    public Mono<Void> callback(WcdkProcessConnectionEvent event) {
        return postForVoid(CALLBACK_PATH, event);
    }

    public Mono<Void> postForVoid(String path, Object body) {
        return executeJson("POST", path, body, objectMapper.getTypeFactory().constructType(Void.class)).then();
    }

    public <T> Mono<T> post(String path, Object body, Class<T> responseType) {
        return executeJson("POST", path, body, objectMapper.getTypeFactory().constructType(responseType));
    }

    public <T> Mono<T> post(String path, Object body, TypeReference<T> responseType) {
        return executeJson("POST", path, body, objectMapper.getTypeFactory().constructType(responseType));
    }

    public Mono<Void> putForVoid(String path, Object body) {
        return executeJson("PUT", path, body, objectMapper.getTypeFactory().constructType(Void.class)).then();
    }

    public <T> Mono<T> put(String path, Object body, Class<T> responseType) {
        return executeJson("PUT", path, body, objectMapper.getTypeFactory().constructType(responseType));
    }

    public <T> Mono<T> put(String path, Object body, TypeReference<T> responseType) {
        return executeJson("PUT", path, body, objectMapper.getTypeFactory().constructType(responseType));
    }

    public <T> Mono<T> get(String path, Class<T> responseType) {
        return executeJson("GET", path, null, objectMapper.getTypeFactory().constructType(responseType));
    }

    public <T> Mono<T> get(String path, TypeReference<T> responseType) {
        return executeJson("GET", path, null, objectMapper.getTypeFactory().constructType(responseType));
    }

    public <T> Mono<T> get(String path, Map<String, ?> queryParams, Class<T> responseType) {
        return executeJson("GET", appendQuery(path, queryParams), null,
                objectMapper.getTypeFactory().constructType(responseType));
    }

    public <T> Mono<T> get(String path, Map<String, ?> queryParams, TypeReference<T> responseType) {
        return executeJson("GET", appendQuery(path, queryParams), null,
                objectMapper.getTypeFactory().constructType(responseType));
    }

    public <T> Mono<T> postMultipart(String path,
                                     Map<String, ?> textParts,
                                     String filePartName,
                                     String fileName,
                                     String contentType,
                                     byte[] fileContent,
                                     Class<T> responseType) {
        return executeMultipart(path, textParts, filePartName, fileName, contentType, fileContent,
                objectMapper.getTypeFactory().constructType(responseType));
    }

    public Mono<Void> delete(String path, Map<String, ?> queryParams) {
        return executeJson("DELETE", appendQuery(path, queryParams), null,
                objectMapper.getTypeFactory().constructType(Void.class)).then();
    }

    private <T> Mono<T> executeJson(String method, String path, Object body, JavaType dataType) {
        WebClient.RequestBodySpec request = webClient.method(org.springframework.http.HttpMethod.valueOf(method))
                .uri(path)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, buildAuthorization());
        WebClient.ResponseSpec responseSpec;
        if (body == null || "GET".equals(method) || "DELETE".equals(method)) {
            responseSpec = request.retrieve();
        } else {
            responseSpec = request.bodyValue(body).retrieve();
        }
        return readResponse(responseSpec, dataType);
    }

    private <T> Mono<T> executeMultipart(String path,
                                         Map<String, ?> textParts,
                                         String filePartName,
                                         String fileName,
                                         String contentType,
                                         byte[] fileContent,
                                         JavaType dataType) {
        validateMultipartRequest(filePartName, fileName, fileContent);
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        if (textParts != null) {
            for (Map.Entry<String, ?> entry : textParts.entrySet()) {
                if (entry.getValue() != null) {
                    builder.part(entry.getKey(), entry.getValue());
                }
            }
        }
        String actualContentType = StringUtils.hasText(contentType) ? contentType : MediaType.APPLICATION_OCTET_STREAM_VALUE;
        builder.part(filePartName, new ByteArrayResource(fileContent) {
                    @Override
                    public String getFilename() {
                        return fileName;
                    }
                })
                .filename(fileName)
                .contentType(MediaType.parseMediaType(actualContentType));
        MultiValueMap<String, org.springframework.http.HttpEntity<?>> multipartBody = builder.build();
        WebClient.ResponseSpec responseSpec = webClient.post()
                .uri(path)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .header(HttpHeaders.AUTHORIZATION, buildAuthorization())
                .body(BodyInserters.fromMultipartData(multipartBody))
                .retrieve();
        return readResponse(responseSpec, dataType);
    }

    private void validateMultipartRequest(String filePartName, String fileName, byte[] fileContent) {
        if (!StringUtils.hasText(filePartName)) {
            throw new IllegalArgumentException("文件参数名不能为空");
        }
        if (!StringUtils.hasText(fileName)) {
            throw new IllegalArgumentException("文件名不能为空");
        }
        if (fileContent == null || fileContent.length == 0) {
            throw new IllegalArgumentException("文件内容不能为空");
        }
    }

    private <T> Mono<T> readResponse(WebClient.ResponseSpec responseSpec, JavaType dataType) {
        return responseSpec
                .onStatus(HttpStatusCode::isError, response -> response.bodyToMono(String.class)
                        .defaultIfEmpty("")
                        .map(body -> new WcdkProcessClientException("服务端调用失败，状态码：" + response.statusCode().value()
                                + "，响应内容：" + body)))
                .bodyToMono(String.class)
                .timeout(serverConfig.getTimeout())
                .flatMap(body -> readApiResponse(body, dataType))
                .flatMap(apiResponse -> {
                    validateApiResponse(apiResponse);
                    if (dataType.getRawClass() == Void.class) {
                        return Mono.<T>empty();
                    }
                    return Mono.justOrEmpty((T) apiResponse.getData());
                });
    }

    private String appendQuery(String path, Map<String, ?> queryParams) {
        if (queryParams == null || queryParams.isEmpty()) {
            return path;
        }
        StringBuilder builder = new StringBuilder(path);
        builder.append(path.contains("?") ? "&" : "?");
        boolean first = true;
        for (Map.Entry<String, ?> entry : queryParams.entrySet()) {
            Object value = entry.getValue();
            if (value == null) {
                continue;
            }
            if (!first) {
                builder.append("&");
            }
            builder.append(encode(entry.getKey()))
                    .append("=")
                    .append(encode(String.valueOf(value)));
            first = false;
        }
        return builder.toString();
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String buildAuthorization() {
        String auth = serverConfig.getUsername() + ":" + serverConfig.getPassword();
        return "Basic " + Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
    }

    private <T> Mono<ApiResponse<T>> readApiResponse(String body, JavaType dataType) {
        try {
            JavaType responseType = objectMapper.getTypeFactory().constructParametricType(ApiResponse.class, dataType);
            return Mono.just(objectMapper.readValue(body, responseType));
        } catch (JsonProcessingException exception) {
            return Mono.error(new WcdkProcessClientException("读取服务端响应失败", exception));
        }
    }

    private void validateApiResponse(ApiResponse<?> response) {
        if (response == null) {
            throw new WcdkProcessClientException("服务端返回内容为空");
        }
        if (response.getCode() == null || response.getCode() != 200) {
            throw new WcdkProcessClientException("服务端返回失败：" + response.getMessage());
        }
    }
}
