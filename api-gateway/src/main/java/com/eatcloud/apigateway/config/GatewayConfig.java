package com.eatcloud.apigateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class GatewayConfig {

	@Bean
	public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
		log.info("🚀 Configuring Gateway Routes...");

		return builder.routes()
			.route("auth-service", r -> r
				.path("/api/v1/auth/**")
				.filters(f -> f
					.stripPrefix(2)
					.addRequestHeader("X-Service-Name", "auth-service"))
				.uri("lb://user-service"))

			.route("user-service", r -> r
				.path("/api/v1/customers/**", "/api/v1/manager/**", "/api/v1/admin/**")
				.filters(f -> f
					.stripPrefix(2)
					.addRequestHeader("X-Service-Name", "user-service"))
				.uri("lb://user-service"))

			.route("order-service", r -> r
				.path("/api/orders/**")
				.filters(f -> f
					.stripPrefix(1)
					.addRequestHeader("X-Service-Name", "order-service"))
				.uri("lb://order-service"))

			.route("store-service", r -> r
				.path("/api/stores/**")
				.filters(f -> f
					.stripPrefix(1)
					.addRequestHeader("X-Service-Name", "store-service"))
				.uri("lb://store-service"))

			.route("payment-service", r -> r
				.path("/api/payments/**")
				.filters(f -> f
					.stripPrefix(1)
					.addRequestHeader("X-Service-Name", "payment-service"))
				.uri("lb://payment-service"))

			.build();
	}
}