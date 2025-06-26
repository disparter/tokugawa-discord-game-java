package io.github.disparter.tokugawa.discord.core.services;

import io.github.disparter.tokugawa.discord.core.models.GameCalendar;
import io.github.disparter.tokugawa.discord.core.models.GameCalendar.Season;
import io.github.disparter.tokugawa.discord.core.repositories.GameCalendarRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Month;

/**
 * Implementation of the GameCalendarService interface.
 */
@Service
public class GameCalendarServiceImpl implements GameCalendarService {

    private final GameCalendarRepository gameCalendarRepository;

    @Autowired
    public GameCalendarServiceImpl(GameCalendarRepository gameCalendarRepository) {
        this.gameCalendarRepository = gameCalendarRepository;
    }

    @Override
    public GameCalendar getCurrentCalendar() {
        GameCalendar calendar = gameCalendarRepository.findTopByOrderByIdDesc();
        if (calendar == null) {
            calendar = initializeCalendar();
        }
        return calendar;
    }

    @Override
    public Season getCurrentSeason() {
        return getCurrentCalendar().getCurrentSeason();
    }

    @Override
    public LocalDateTime getCurrentDate() {
        return getCurrentCalendar().getCurrentDate();
    }

    @Override
    @Transactional
    public GameCalendar advanceCalendar(int days) {
        GameCalendar calendar = getCurrentCalendar();
        LocalDateTime newDate = calendar.getCurrentDate().plusDays(days);
        
        // Update the season if necessary
        Season newSeason = determineSeason(newDate);
        if (newSeason != calendar.getCurrentSeason()) {
            calendar.setCurrentSeason(newSeason);
            calendar.setDayOfSeason(1);
        } else {
            calendar.setDayOfSeason(calendar.getDayOfSeason() + days);
        }
        
        // Update the year if necessary
        if (newDate.getYear() != calendar.getYear()) {
            calendar.setYear(newDate.getYear());
        }
        
        calendar.setCurrentDate(newDate);
        return gameCalendarRepository.save(calendar);
    }

    @Override
    public boolean isDateInRange(int startMonth, int startDay, int endMonth, int endDay) {
        LocalDateTime currentDate = getCurrentDate();
        int currentMonth = currentDate.getMonthValue();
        int currentDay = currentDate.getDayOfMonth();
        
        // Check if the date range spans across years (e.g., winter events)
        if (startMonth > endMonth) {
            // Current date is after start month or before end month
            return (currentMonth >= startMonth && currentDay >= startDay) || 
                   (currentMonth <= endMonth && currentDay <= endDay);
        } else {
            // Normal date range within the same year
            if (currentMonth > startMonth && currentMonth < endMonth) {
                return true;
            } else if (currentMonth == startMonth && currentMonth == endMonth) {
                return currentDay >= startDay && currentDay <= endDay;
            } else if (currentMonth == startMonth) {
                return currentDay >= startDay;
            } else if (currentMonth == endMonth) {
                return currentDay <= endDay;
            } else {
                return false;
            }
        }
    }

    @Override
    @Transactional
    public GameCalendar initializeCalendar() {
        LocalDateTime now = LocalDateTime.now();
        Season season = determineSeason(now);
        
        GameCalendar calendar = new GameCalendar();
        calendar.setCurrentDate(now);
        calendar.setCurrentSeason(season);
        calendar.setDayOfSeason(calculateDayOfSeason(now, season));
        calendar.setYear(now.getYear());
        
        return gameCalendarRepository.save(calendar);
    }
    
    /**
     * Determines the season based on the given date.
     * 
     * @param date the date
     * @return the season
     */
    private Season determineSeason(LocalDateTime date) {
        int month = date.getMonthValue();
        
        if (month >= 3 && month <= 5) {
            return Season.SPRING;
        } else if (month >= 6 && month <= 8) {
            return Season.SUMMER;
        } else if (month >= 9 && month <= 11) {
            return Season.AUTUMN;
        } else {
            return Season.WINTER;
        }
    }
    
    /**
     * Calculates the day of the season based on the given date and season.
     * 
     * @param date the date
     * @param season the season
     * @return the day of the season
     */
    private int calculateDayOfSeason(LocalDateTime date, Season season) {
        int month = date.getMonthValue();
        int day = date.getDayOfMonth();
        
        switch (season) {
            case SPRING:
                if (month == 3) return day;
                if (month == 4) return day + 31;
                if (month == 5) return day + 31 + 30;
                break;
            case SUMMER:
                if (month == 6) return day;
                if (month == 7) return day + 30;
                if (month == 8) return day + 30 + 31;
                break;
            case AUTUMN:
                if (month == 9) return day;
                if (month == 10) return day + 30;
                if (month == 11) return day + 30 + 31;
                break;
            case WINTER:
                if (month == 12) return day;
                if (month == 1) return day + 31;
                if (month == 2) return day + 31 + 31;
                break;
        }
        
        return 1; // Default to first day if something goes wrong
    }
}