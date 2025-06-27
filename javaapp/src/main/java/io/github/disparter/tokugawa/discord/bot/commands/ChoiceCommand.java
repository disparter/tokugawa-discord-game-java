package io.github.disparter.tokugawa.discord.bot.commands;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
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

import java.util.Map;
import java.util.Optional;
import java.util.List;

/**
 * Command for making choices in the narrative.
 * This command allows players to select options during dialogues and progress through the story.
 */
@Component
public class ChoiceCommand implements SlashCommand {

    private final NarrativeService narrativeService;
    private final PlayerService playerService;
    private final ProgressService progressService;

    @Autowired
    public ChoiceCommand(NarrativeService narrativeService, PlayerService playerService, ProgressService progressService) {
        this.narrativeService = narrativeService;
        this.playerService = playerService;
        this.progressService = progressService;
    }

    @Override
    public String getName() {
        return "escolha";
    }

    @Override
    public Mono<Void> execute(ChatInputInteractionEvent event) {
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

                    // Get the choice index from the command options
                    Optional<ApplicationCommandInteractionOption> indexOption = 
                            event.getOption("indice");

                    if (indexOption.isEmpty() || indexOption.get().getValue().isEmpty()) {
                        return event.reply()
                                .withContent("Você precisa especificar um índice de escolha. Use /escolha [indice].")
                                .withEphemeral(true);
                    }

                    // Parse the choice index
                    int choiceIndex;
                    try {
                        choiceIndex = Integer.parseInt(indexOption.get().getValue().get().asString());
                    } catch (NumberFormatException e) {
                        return event.reply()
                                .withContent("Índice de escolha inválido. Use um número inteiro.")
                                .withEphemeral(true);
                    }

                    // Get the player's progress
                    Progress progress = progressService.getProgressByPlayerId(player.getId());
                    if (progress == null || progress.getCurrentChapterId() == null) {
                        return event.reply()
                                .withContent("Você não está em nenhum capítulo atualmente. Use /historia iniciar [capitulo_id] para começar um capítulo.")
                                .withEphemeral(true);
                    }

                    // Process the choice
                    Map<String, Object> result = narrativeService.processChoice(player.getId(), choiceIndex);

                    if (!(Boolean) result.getOrDefault("success", false)) {
                        return event.reply()
                                .withContent("Erro ao processar escolha: " + result.getOrDefault("error", "Erro desconhecido"))
                                .withEphemeral(true);
                    }

                    // Get the updated progress
                    Progress updatedProgress = (Progress) result.get("progress");

                    // Get the current chapter
                    Chapter chapter = narrativeService.findChapterById(Long.valueOf(updatedProgress.getCurrentChapterId()));
                    if (chapter == null) {
                        return event.reply()
                                .withContent("Erro ao obter o capítulo atual.")
                                .withEphemeral(true);
                    }

                    // Get the next dialogue
                    int nextDialogueIndex = updatedProgress.getCurrentDialogueIndex();
                    String nextDialogue = "...";
                    List<String> dialogues = chapter.getDialogues();

                    if (dialogues != null && !dialogues.isEmpty() && nextDialogueIndex < dialogues.size()) {
                        nextDialogue = dialogues.get(nextDialogueIndex);
                    }

                    // Check if we need to move to the next chapter
                    if (result.containsKey("next_chapter")) {
                        Chapter nextChapter = (Chapter) result.get("next_chapter");

                        StringBuilder response = new StringBuilder("**Escolha processada!**\n\n")
                                .append("Você avançou para o próximo capítulo: **")
                                .append(nextChapter.getTitle())
                                .append("**\n\n");

                        // Get the first dialogue of the next chapter
                        if (nextChapter.getDialogues() != null && !nextChapter.getDialogues().isEmpty()) {
                            response.append(nextChapter.getDialogues().get(0));
                        }

                        return event.reply()
                                .withContent(response.toString())
                                .withEphemeral(false);
                    }

                    // Display the next dialogue
                    StringBuilder response = new StringBuilder("**Escolha processada!**\n\n")
                            .append(nextDialogue);

                    return event.reply()
                            .withContent(response.toString())
                            .withEphemeral(false);
                });
    }
}
