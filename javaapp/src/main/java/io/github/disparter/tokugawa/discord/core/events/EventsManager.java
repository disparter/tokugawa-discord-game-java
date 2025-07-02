package io.github.disparter.tokugawa.discord.core.events;

import lombok.extern.slf4j.Slf4j;
import io.github.disparter.tokugawa.discord.bot.DiscordBot;
import io.github.disparter.tokugawa.discord.core.services.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

/**
 * Manages and coordinates all types of events (daily, weekly, special).
 */
@Service
@Slf4j
public class EventsManager {
    
    
    private final DiscordBot discordBot;
    private final PlayerService playerService;
    private final TaskScheduler taskScheduler;
    
    private final DailyEvents dailyEvents;
    private final WeeklyEvents weeklyEvents;
    private final SpecialEvents specialEvents;
    
    @Value("${discord.events.channel.id:0}")
    private Long eventsChannelId;
    
    private boolean isRunning = false;
    private ScheduledFuture<?> scheduledTask;
    
    /**
     * Constructor for EventsManager.
     *
     * @param discordBot The Discord bot instance
     * @param playerService The player service
     * @param taskScheduler The task scheduler
     */
    @Autowired
    public EventsManager(DiscordBot discordBot, 
                         PlayerService playerService,
                         TaskScheduler taskScheduler) {
        this.discordBot = discordBot;
        this.playerService = playerService;
        this.taskScheduler = taskScheduler;
        
        // Initialize event handlers
        this.dailyEvents = new DailyEvents(discordBot, playerService, eventsChannelId);
        this.weeklyEvents = new WeeklyEvents(discordBot, playerService, eventsChannelId);
        this.specialEvents = new SpecialEvents(discordBot, playerService, eventsChannelId);
    }
    
    /**
     * Initialize the events manager.
     * This method is called automatically by Spring after the bean is created.
     */
    @PostConstruct
    public void init() {
        start();
    }
    
    /**
     * Clean up resources when the application is shutting down.
     * This method is called automatically by Spring before the bean is destroyed.
     */
    @PreDestroy
    public void destroy() {
        stop();
    }
    
    /**
     * Start the events manager.
     */
    public void start() {
        if (isRunning) {
            log.info("Events manager is already running");
            return;
        }
        
        try {
            isRunning = true;
            log.info("Started events manager");
        } catch (Exception e) {
            log.error("Error starting events manager: {}", e.getMessage(), e);
            isRunning = false;
        }
    }
    
    /**
     * Stop the events manager.
     */
    public void stop() {
        if (!isRunning) {
            log.info("Events manager is not running");
            return;
        }
        
        try {
            isRunning = false;
            
            if (scheduledTask != null) {
                scheduledTask.cancel(false);
                scheduledTask = null;
            }
            
            // Clean up all events
            dailyEvents.cleanup();
            weeklyEvents.cleanup();
            specialEvents.cleanup();
            
            log.info("Stopped events manager");
        } catch (Exception e) {
            log.error("Error stopping events manager: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Check for special events.
     * This method is scheduled to run every hour.
     */
    @Scheduled(cron = "0 0 * * * *") // Run every hour
    public void checkForSpecialEvents() {
        if (!isRunning) {
            return;
        }
        
        try {
            specialEvents.checkForSpecialEvents()
                    .subscribe(
                            null,
                            e -> log.error("Error checking for special events: {}", e.getMessage(), e)
                    );
        } catch (Exception e) {
            log.error("Error checking for special events: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Handle daily events.
     * This method is scheduled to run at midnight every day.
     */
    @Scheduled(cron = "0 0 0 * * *") // Run at midnight every day
    public void handleDailyEvents() {
        if (!isRunning) {
            return;
        }
        
        try {
            // Send daily announcements
            dailyEvents.sendDailyAnnouncement()
                    .subscribe(
                            null,
                            e -> log.error("Error sending daily announcement: {}", e.getMessage(), e)
                    );
            
            // Select daily subject
            dailyEvents.selectDailySubject();
            
            // Announce daily subject
            dailyEvents.announceDailySubject()
                    .subscribe(
                            null,
                            e -> log.error("Error announcing daily subject: {}", e.getMessage(), e)
                    );
            
            // Reset daily progress
            dailyEvents.resetDailyProgress();
            
            log.info("Daily events processed");
        } catch (Exception e) {
            log.error("Error handling daily events: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Handle weekly events.
     * This method is scheduled to run at midnight on Monday every week.
     */
    @Scheduled(cron = "0 0 0 * * 1") // Run at midnight on Monday
    public void handleWeeklyEvents() {
        if (!isRunning) {
            return;
        }
        
        try {
            // Start weekly tournament
            weeklyEvents.startWeeklyTournament()
                    .subscribe(
                            null,
                            e -> log.error("Error starting weekly tournament: {}", e.getMessage(), e)
                    );
            
            log.info("Weekly events processed");
        } catch (Exception e) {
            log.error("Error handling weekly events: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Check for ending events.
     * This method is scheduled to run every hour.
     */
    @Scheduled(cron = "0 0 * * * *") // Run every hour
    public void checkForEndingEvents() {
        if (!isRunning) {
            return;
        }
        
        try {
            // Check for ending weekly tournament
            weeklyEvents.checkForEndingTournament()
                    .subscribe(
                            null,
                            e -> log.error("Error checking for ending tournament: {}", e.getMessage(), e)
                    );
            
            // Check for ending special event
            specialEvents.checkForEndingSpecialEvent()
                    .subscribe(
                            null,
                            e -> log.error("Error checking for ending special event: {}", e.getMessage(), e)
                    );
            
            log.info("Checked for ending events");
        } catch (Exception e) {
            log.error("Error checking for ending events: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Get information about current events.
     *
     * @return A map containing information about current events
     */
    public Map<String, Object> getCurrentEvents() {
        Map<String, Object> events = new HashMap<>();
        
        try {
            // Daily events
            Map<String, Object> dailyInfo = new HashMap<>();
            dailyInfo.put("subject", dailyEvents.selectSubjects());
            events.put("daily", dailyInfo);
            
            // Weekly events
            Map<String, Object> weeklyInfo = new HashMap<>();
            // TODO: Implement WeeklyEvents.getCurrentTournamentInfo() method
            // This should return current tournament name, participants, and end time
            weeklyInfo.put("tournament", "No tournament running");
            weeklyInfo.put("participants", 0);
            weeklyInfo.put("end_time", null);
            events.put("weekly", weeklyInfo);
            
            // Special events
            Map<String, Object> specialInfo = new HashMap<>();
            // TODO: Implement SpecialEvents.getCurrentEventInfo() method
            // This should return current special event name, participants, and end time
            specialInfo.put("event", "No special event running");
            specialInfo.put("participants", 0);
            specialInfo.put("end_time", null);
            events.put("special", specialInfo);
            
            log.info("Retrieved current events information");
        } catch (Exception e) {
            log.error("Error getting current events: {}", e.getMessage(), e);
        }
        
        return events;
    }
    
    /**
     * Start daily events manually.
     */
    public void startDailyEvents() {
        if (!isRunning) {
            log.warn("Events manager is not running");
            return;
        }
        
        dailyEvents.sendDailyAnnouncement()
                .subscribe(
                        null,
                        e -> log.error("Error starting daily events: {}", e.getMessage(), e)
                );
    }
    
    /**
     * Start weekly events manually.
     */
    public void startWeeklyEvents() {
        if (!isRunning) {
            log.warn("Events manager is not running");
            return;
        }
        
        weeklyEvents.startWeeklyTournament()
                .subscribe(
                        null,
                        e -> log.error("Error starting weekly events: {}", e.getMessage(), e)
                );
    }
    
    /**
     * Start special events manually.
     *
     * @param eventType The type of event to start
     */
    public void startSpecialEvents(String eventType) {
        if (!isRunning) {
            log.warn("Events manager is not running");
            return;
        }
        
        specialEvents.startSpecialEvent(eventType)
                .subscribe(
                        null,
                        e -> log.error("Error starting special events: {}", e.getMessage(), e)
                );
    }
    
    /**
     * Handle error during event processing.
     *
     * @param error The error that occurred
     */
    private void handleError(Throwable error) {
        log.error("Error in event processing: {}", error.getMessage(), error);
    }
}