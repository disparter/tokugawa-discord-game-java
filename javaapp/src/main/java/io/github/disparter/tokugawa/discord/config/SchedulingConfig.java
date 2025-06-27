package io.github.disparter.tokugawa.discord.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class SchedulingConfig {

    private static final Logger logger = LoggerFactory.getLogger(SchedulingConfig.class);

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
            logger.error("Error in scheduled task: {}", throwable.getMessage(), throwable);
        });
        return scheduler;
    }
}
