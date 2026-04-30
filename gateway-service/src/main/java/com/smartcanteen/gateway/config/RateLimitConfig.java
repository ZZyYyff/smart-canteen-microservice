package com.smartcanteen.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

import org.springframework.context.annotation.Primary;

import java.util.Objects;

@Configuration
public class RateLimitConfig {

    /** IP 级限流 Key */
    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> Mono.just(
                Objects.requireNonNull(
                        exchange.getRequest().getRemoteAddress()).getAddress().getHostAddress());
    }

    /** 用户级限流 Key，优先取 X-User-Id，未认证时回退到 IP */
    @Bean
    @Primary
    public KeyResolver userKeyResolver() {
        return exchange -> {
            String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
            if (userId != null && !userId.isEmpty()) {
                return Mono.just(userId);
            }
            return Mono.just(
                    Objects.requireNonNull(
                            exchange.getRequest().getRemoteAddress()).getAddress().getHostAddress());
        };
    }
}
