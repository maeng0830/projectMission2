package com.maeng0830.account.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing // @EntityListeners(AuditingEntityListener.class)를 적용하기 위한 어노테이션
public class JpaAuditingConfiguration {
}
