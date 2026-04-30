package com.smartcanteen.gateway.config;

import com.smartcanteen.common.jwt.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class JwtAuthFilterTest {

    private final JwtAuthFilter filter = new JwtAuthFilter();

    // ==================== 放行测试 ====================

    @Test
    @DisplayName("1. 登录接口放行 — 无需 Token")
    void testLoginEndpointExcluded() {
        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/api/users/login").build());
        GatewayFilterChain chain = mock(GatewayFilterChain.class);
        when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        filter.filter(exchange, chain).block();

        verify(chain).filter(any(ServerWebExchange.class));
    }

    @Test
    @DisplayName("2. 注册接口放行 — 无需 Token")
    void testRegisterEndpointExcluded() {
        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/api/users/register").build());
        GatewayFilterChain chain = mock(GatewayFilterChain.class);
        when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        filter.filter(exchange, chain).block();

        verify(chain).filter(any(ServerWebExchange.class));
    }

    @Test
    @DisplayName("3. 刷新 Token 接口放行 — 无需 Token")
    void testRefreshTokenEndpointExcluded() {
        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/api/users/refresh-token").build());
        GatewayFilterChain chain = mock(GatewayFilterChain.class);
        when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        filter.filter(exchange, chain).block();

        verify(chain).filter(any(ServerWebExchange.class));
    }

    // ==================== 拒绝测试 ====================

    @Test
    @DisplayName("4. 无 Token 访问业务接口返回 401")
    void testNoTokenReturns401() {
        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/orders/123").build());
        GatewayFilterChain chain = mock(GatewayFilterChain.class);
        when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        filter.filter(exchange, chain).block();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        verify(chain, never()).filter(any(ServerWebExchange.class));
    }

    @Test
    @DisplayName("5. Authorization 头缺少 Bearer 前缀返回 401")
    void testAuthHeaderWithoutBearerReturns401() {
        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/orders/123")
                        .header(HttpHeaders.AUTHORIZATION, "invalid-format-token")
                        .build());
        GatewayFilterChain chain = mock(GatewayFilterChain.class);
        when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        filter.filter(exchange, chain).block();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
    }

    @Test
    @DisplayName("6. 无效 Token 返回 401")
    void testInvalidTokenReturns401() {
        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/orders/123")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer invalid-token")
                        .build());
        GatewayFilterChain chain = mock(GatewayFilterChain.class);
        when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        filter.filter(exchange, chain).block();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
    }

    // ==================== 通过测试 ====================

    @Test
    @DisplayName("7. 有效 Token 访问业务接口通过，并注入 X-User-Id 和 X-User-Role 头")
    void testValidTokenPassesAndInjectsHeaders() {
        String token = JwtUtil.generateToken(1L, "STUDENT");

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/orders/123")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);
        GatewayFilterChain chain = mock(GatewayFilterChain.class);
        when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        filter.filter(exchange, chain).block();

        verify(chain).filter(any(ServerWebExchange.class));
    }

    @Test
    @DisplayName("8. 有效 Token — 链式调用传递了正确的请求头")
    void testValidTokenHeaderInjection() {
        String token = JwtUtil.generateToken(2L, "MERCHANT");

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/menus/dishes")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);
        GatewayFilterChain chain = mock(GatewayFilterChain.class);
        when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        filter.filter(exchange, chain).block();

        // 验证 chain 被调用且传递的 exchange 包含注入的 header
        verify(chain).filter(argThat(e -> {
            ServerHttpRequest mutatedRequest = e.getRequest();
            return "2".equals(mutatedRequest.getHeaders().getFirst("X-User-Id"))
                    && "MERCHANT".equals(mutatedRequest.getHeaders().getFirst("X-User-Role"));
        }));
    }
}
