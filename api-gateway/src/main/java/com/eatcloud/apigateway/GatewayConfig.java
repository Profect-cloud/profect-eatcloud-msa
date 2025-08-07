package com.eatcloud.apigateway;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

	@Bean
	public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
		return builder.routes()
			// User Service - Eureka 서비스명 사용
			.route("user-service", r -> r.path("/api/users/**", "/api/customers/**", "/api/auth/**")
				.filters(f -> f.stripPrefix(1))
				.uri("lb://user-service"))

			// Store Service
			.route("store-service", r -> r.path("/api/stores/**", "/api/menus/**")
				.filters(f -> f.stripPrefix(1))
				.uri("lb://store-service"))

			// Order Service
			.route("order-service", r -> r.path("/api/orders/**", "/api/reviews/**")
				.filters(f -> f.stripPrefix(1))
				.uri("lb://order-service"))

			// Payment Service
			.route("payment-service", r -> r.path("/api/payments/**", "/api/points/**")
				.filters(f -> f.stripPrefix(1))
				.uri("lb://payment-service"))

			.build();
	}
}