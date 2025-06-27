package io.github.disparter.tokugawa.discord.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * Configuration class for scheduling tasks.
 * This enables the @Scheduled annotations in the application.
 */
@Configuration
@EnableScheduling
@Slf4j
public class SchedulingConfig {


    /**
     * Creates a task scheduler for scheduled tasks.
     *
     * @return The task scheduler
     */
    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(5); // Set an appropriate pool size
        scheduler.setThreadNamePrefix("event-scheduler-");
        scheduler.setErrorHandler(throwable -> {
            log.error("Error in scheduled task: {}", throwable.getMessage(), throwable);
        });
        return scheduler;
    }
}
