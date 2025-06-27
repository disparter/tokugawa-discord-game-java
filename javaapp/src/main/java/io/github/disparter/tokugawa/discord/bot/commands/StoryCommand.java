package io.github.disparter.tokugawa.discord.bot.commands;

import discord4j.core.event.domain.interaction.SlashCommandInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import io.github.disparter.tokugawa.discord.core.models.Chapter;
import io.github.disparter.tokugawa.discord.core.models.Player;
import io.github.disparter.tokugawa.discord.core.models.Progress;
import io.github.disparter.tokugawa.discord.core.services.NarrativeService;
import io.github.disparter.tokugawa.discord.core.services.PlayerService;
import io.github.disparter.tokugawa.discord.core.services.ProgressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Optional;

/**
 * Command for interacting with the story system.
 * This command allows players to start chapters and progress through the story.
 */
@Component
public class StoryCommand implements SlashCommand {

    private final NarrativeService narrativeService;
    private final PlayerService playerService;
    private final ProgressService progressService;

    @Autowired
    public StoryCommand(NarrativeService narrativeService, PlayerService playerService, ProgressService progressService) {
        this.narrativeService = narrativeService;
        this.playerService = playerService;
        this.progressService = progressService;
    }

    @Override
    public String getName() {
        return "historia";
    }

    @Override
    public Mono<Void> execute(SlashCommandInteractionEvent event) {
        return Mono.justOrEmpty(event.getInteraction().getUser())
                .flatMap(user -> {
                    String userId = user.getId().asString();
                    
                    // Check if player exists
                    Player player = playerService.findByDiscordId(userId);
                    if (player == null) {
                        return event.reply()
                                .withContent("Você precisa se registrar primeiro usando o comando /register.")
                                .withEphemeral(true);
                    }
                    
                    // Get the subcommand
                    Optional<ApplicationCommandInteractionOption> subcommandOption = 
                            event.getOptions().stream().findFirst();
                    
                    if (subcommandOption.isEmpty()) {
                        return event.reply()
                                .withContent("Comando inválido. Use /historia iniciar [capitulo_id].")
                                .withEphemeral(true);
                    }
                    
                    String subcommand = subcommandOption.get().getName();
                    
                    if ("iniciar".equals(subcommand)) {
                        return handleStartChapter(event, player);
                    } else {
                        return event.reply()
                                .withContent("Subcomando desconhecido: " + subcommand)
                                .withEphemeral(true);
                    }
                });
    }
    
    private Mono<Void> handleStartChapter(SlashCommandInteractionEvent event, Player player) {
        // Get the chapter ID from the command options
        Optional<ApplicationCommandInteractionOption> chapterOption = 
                event.getOption("capitulo_id");
        
        if (chapterOption.isEmpty() || chapterOption.get().getValue().isEmpty()) {
            return event.reply()
                    .withContent("Você precisa especificar um ID de capítulo. Use /historia iniciar [capitulo_id].")
                    .withEphemeral(true);
        }
        
        String chapterId = chapterOption.get().getValue().get().asString();
        
        // Find the chapter by ID
        Chapter chapter = narrativeService.findChapterById(Long.parseLong(chapterId));
        if (chapter == null) {
            return event.reply()
                    .withContent("Capítulo não encontrado: " + chapterId)
                    .withEphemeral(true);
        }
        
        // Start the chapter
        chapter = narrativeService.startChapter(Long.parseLong(chapterId), player.getId());
        if (chapter == null) {
            return event.reply()
                    .withContent("Não foi possível iniciar o capítulo: " + chapterId)
                    .withEphemeral(true);
        }
        
        // Get the player's progress to display the first dialogue
        Progress progress = progressService.getProgressByPlayerId(player.getId());
        if (progress == null || progress.getCurrentChapterId() == null) {
            return event.reply()
                    .withContent("Erro ao iniciar o capítulo. Progresso não encontrado.")
                    .withEphemeral(true);
        }
        
        // Display the first dialogue
        String firstDialogue = "...";
        if (chapter.getDialogues() != null && !chapter.getDialogues().isEmpty()) {
            firstDialogue = chapter.getDialogues().get(0);
        }
        
        StringBuilder response = new StringBuilder("**")
                .append(chapter.getTitle())
                .append("**\n\n")
                .append(firstDialogue);
        
        return event.reply()
                .withContent(response.toString())
                .withEphemeral(false);
    }
}