# Event System for Tokugawa Discord Game

This directory contains the event system for the Tokugawa Discord Game. The event system is responsible for managing and coordinating various types of events, including daily events, weekly tournaments, and special events.

## Components

### BaseEvent

`BaseEvent` is the base class for all event types. It provides common functionality for event handling, including:

- Finding channels
- Sending announcements
- Creating embeds
- Getting users and guilds
- Handling errors
- Cleaning up resources

### DailyEvents

`DailyEvents` handles daily events and announcements, including:

- Subject selection
- Creating embeds and buttons
- Handling subject selection
- Sending daily announcements
- Cleaning up daily events

### WeeklyEvents

`WeeklyEvents` handles weekly tournaments and events, including:

- Starting tournaments
- Sending announcements
- Creating embeds and buttons
- Adding participants
- Updating scores
- Ending tournaments
- Cleaning up resources

### SpecialEvents

`SpecialEvents` handles special events like seasonal events and competitions, including:

- Starting events
- Sending announcements
- Creating embeds and buttons
- Adding participants
- Updating scores
- Ending events
- Checking for events that should be started
- Cleaning up resources

### EventsManager

`EventsManager` coordinates all event types and handles scheduling, including:

- Starting and stopping the events manager
- Checking for special events
- Handling daily and weekly events
- Checking for ending events
- Getting information about current events
- Manually starting events

## Configuration

The event system is configured through the following properties in `application.properties`:

```properties
# Events channel ID (use environment variable or default to 0)
discord.events.channel.id=${DISCORD_EVENTS_CHANNEL_ID:0}
```

## Scheduling

The event system uses Spring's `@Scheduled` annotation to schedule events. The scheduling is configured in `SchedulingConfig.java`.

## Integration with Discord Bot

The event system is integrated with the Discord bot through the `DiscordBot` class, which injects the `EventsManager` and starts it when the bot is initialized.