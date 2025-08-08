package com.eatcloud.userservice.global.timeData;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(
	basePackages = "com.eatcloud.userservice",
	repositoryBaseClass = BaseTimeRepositoryImpl.class
)
public class JpaConfig {
}
