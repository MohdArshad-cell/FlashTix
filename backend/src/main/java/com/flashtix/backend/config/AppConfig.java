package com.flashtix.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.retry.annotation.EnableRetry;

@Configuration
@EnableRetry
@EnableJpaAuditing
public class AppConfig {
    
    // @EnableRetry: Activates the @Retryable annotation in TicketService
    // This allows automatic retry of methods that fail with OptimisticLockingException
    
    // @EnableJpaAuditing: Activates @CreatedDate and @LastModifiedDate in Ticket entity
    // This automatically manages createdAt and updatedAt timestamps
}
