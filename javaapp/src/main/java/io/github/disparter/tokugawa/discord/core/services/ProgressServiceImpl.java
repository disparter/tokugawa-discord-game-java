package io.github.disparter.tokugawa.discord.core.services;

import io.github.disparter.tokugawa.discord.core.models.Progress;
import io.github.disparter.tokugawa.discord.core.models.Player;
import io.github.disparter.tokugawa.discord.core.repositories.ChapterRepository;
import io.github.disparter.tokugawa.discord.core.repositories.EventRepository;
import io.github.disparter.tokugawa.discord.core.repositories.PlayerRepository;
import io.github.disparter.tokugawa.discord.core.repositories.ProgressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of the ProgressService interface.
 */
@Service
public class ProgressServiceImpl implements ProgressService {

    private final ProgressRepository progressRepository;
    private final PlayerRepository playerRepository;
    private final ChapterRepository chapterRepository;
    private final EventRepository eventRepository;

    @Autowired
    public ProgressServiceImpl(ProgressRepository progressRepository,
                              PlayerRepository playerRepository,
                              ChapterRepository chapterRepository,
                              EventRepository eventRepository) {
        this.progressRepository = progressRepository;
        this.playerRepository = playerRepository;
        this.chapterRepository = chapterRepository;
        this.eventRepository = eventRepository;
    }

    @Override
    public Progress findById(Long id) {
        // Placeholder implementation
        return progressRepository.findById(id).orElse(null);
    }

    @Override
    public List<Progress> getPlayerProgress(Long playerId) {
        List<Progress> progressList = new ArrayList<>();
        Optional<Progress> progressOpt = progressRepository.findByPlayerId(playerId);
        progressOpt.ifPresent(progressList::add);
        return progressList;
    }

    @Override
    public Progress getSpecificProgress(Long playerId, Long contentId, String contentType) {
        Optional<Progress> progressOpt = progressRepository.findByPlayerId(playerId);
        if (progressOpt.isPresent()) {
            Progress progress = progressOpt.get();

            // Check if the content is already in the progress record
            if ("CHAPTER".equals(contentType)) {
                if (contentId.toString().equals(progress.getCurrentChapterId())) {
                    return progress;
                }
                if (progress.getCompletedChapters().contains(contentId.toString())) {
                    return progress;
                }
            } else if ("EVENT".equals(contentType)) {
                if (progress.getTriggeredEvents().containsKey(contentId.toString())) {
                    return progress;
                }
            }
        }
        return null;
    }

    @Override
    public Progress updateProgress(Long playerId, Long contentId, String contentType, String status) {
        // Get existing progress or create a new one
        Progress progress = getSpecificProgress(playerId, contentId, contentType);
        if (progress == null) {
            // Create new progress record
            progress = new Progress();

            // Get player by ID
            Player player = playerRepository.findById(playerId).orElse(null);
            if (player == null) {
                return null; // Player not found
            }

            progress.setPlayer(player);

            // Initialize collections if needed
            if (progress.getCompletedChapters() == null) {
                progress.setCompletedChapters(new ArrayList<>());
            }
            if (progress.getCompletedArcs() == null) {
                progress.setCompletedArcs(new ArrayList<>());
            }
            if (progress.getTriggeredEvents() == null) {
                progress.setTriggeredEvents(new HashMap<>());
            }
        }

        // Update progress based on content type and status
        if ("CHAPTER".equals(contentType)) {
            if ("STARTED".equals(status)) {
                progress.setCurrentChapterId(contentId.toString());
                progress.setCurrentDialogueIndex(0); // Reset dialogue index for new chapter
            } else if ("COMPLETED".equals(status)) {
                // Add to completed chapters if not already there
                if (!progress.getCompletedChapters().contains(contentId.toString())) {
                    progress.getCompletedChapters().add(contentId.toString());
                }

                // Check if this completes an arc
                String arcId = chapterRepository.findById(contentId)
                    .map(chapter -> chapter.getArcId())
                    .orElse(null);

                if (arcId != null) {
                    // Check if all chapters in this arc are completed
                    List<Long> arcChapterIds = chapterRepository.findByArcId(arcId)
                        .stream()
                        .map(chapter -> chapter.getId())
                        .toList();

                    boolean allCompleted = true;
                    for (Long chapterId : arcChapterIds) {
                        if (!progress.getCompletedChapters().contains(chapterId.toString())) {
                            allCompleted = false;
                            break;
                        }
                    }

                    if (allCompleted && !progress.getCompletedArcs().contains(arcId)) {
                        progress.getCompletedArcs().add(arcId);
                        progress.setCurrentArc(null); // Clear current arc when completed
                    }
                }
            }
        } else if ("EVENT".equals(contentType)) {
            if (status != null) {
                // Store event status in triggered events map
                progress.getTriggeredEvents().put(contentId.toString(), status);
            }
        }

        return save(progress);
    }

    @Override
    public double getCompletionPercentage(Long playerId) {
        // Get player progress
        Optional<Progress> progressOpt = progressRepository.findByPlayerId(playerId);
        if (!progressOpt.isPresent()) {
            return 0.0; // No progress found
        }

        Progress progress = progressOpt.get();

        // Get total number of chapters, arcs, and events in the game
        long totalChapters = chapterRepository.count();
        long totalEvents = eventRepository.count();

        if (totalChapters == 0 && totalEvents == 0) {
            return 0.0; // No content to complete
        }

        // Calculate completion percentage based on completed chapters and triggered events
        int completedChapters = progress.getCompletedChapters().size();
        int triggeredEvents = progress.getTriggeredEvents().size();

        // Calculate weighted completion percentage
        // Chapters are weighted more heavily than events (70% chapters, 30% events)
        double chapterWeight = 0.7;
        double eventWeight = 0.3;

        double chapterCompletion = totalChapters > 0 ? (double) completedChapters / totalChapters : 0.0;
        double eventCompletion = totalEvents > 0 ? (double) triggeredEvents / totalEvents : 0.0;

        double completionPercentage = (chapterCompletion * chapterWeight + eventCompletion * eventWeight) * 100.0;

        // Round to 2 decimal places
        return Math.round(completionPercentage * 100.0) / 100.0;
    }

    @Override
    public Progress save(Progress progress) {
        // Placeholder implementation
        return progressRepository.save(progress);
    }
}
