package com.wcdk.process.config;

import com.wcdk.process.WcdkProcessConnectionConfig;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * @auther WCDK
 * @date 2026/7/21
 * @version 1.0
 **/
public class WcdkProcessAuthWebFilter implements WebFilter {

    private static final String AUTH_HEADER = "WCDK_AUTH";

    private static final String CALLBACK_PATH_PREFIX = "/wcdk_process/";

    private final WcdkProcessConnectionConfig connectionConfig;

    public WcdkProcessAuthWebFilter(WcdkProcessConnectionConfig connectionConfig) {
        this.connectionConfig = connectionConfig;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().pathWithinApplication().value();
        if (!path.startsWith(CALLBACK_PATH_PREFIX) || !StringUtils.hasText(connectionConfig.getAuthFlg())) {
            return chain.filter(exchange);
        }
        String authValue = exchange.getRequest().getHeaders().getFirst(AUTH_HEADER);
        if (connectionConfig.getAuthFlg().equals(authValue)) {
            return chain.filter(exchange);
        }
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        byte[] responseBytes = "{\"code\":401,\"message\":\"回调鉴权失败\",\"data\":null}".getBytes(StandardCharsets.UTF_8);
        DataBuffer dataBuffer = exchange.getResponse().bufferFactory().wrap(responseBytes);
        return exchange.getResponse().writeWith(Mono.just(dataBuffer));
    }
}
