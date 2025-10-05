package com.ayungi.cms.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Конфигурация асинхронных операций
 */
@Configuration
@EnableAsync
@Slf4j
public class AsyncConfig {

    /**
     * Executor для асинхронных операций (например, аудит)
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        log.info("Создание Async Task Executor");
        
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // Количество потоков
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        
        // Префикс имени потока
        executor.setThreadNamePrefix("Async-");
        
        // Ожидание завершения задач при shutdown
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        
        executor.initialize();
        return executor;
    }
}
