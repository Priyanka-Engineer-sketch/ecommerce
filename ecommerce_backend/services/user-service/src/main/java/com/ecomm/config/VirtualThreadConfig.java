package com.ecomm.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class VirtualThreadConfig {

    @Bean(destroyMethod = "close")
    public ExecutorService virtualThreadExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }

    @Bean
    public TaskExecutor taskExecutor(ExecutorService virtualThreadExecutor) {
        return new TaskExecutor() {
            @Override
            public void execute(Runnable task) {
                virtualThreadExecutor.submit(task);
            }
        };
    }
}

