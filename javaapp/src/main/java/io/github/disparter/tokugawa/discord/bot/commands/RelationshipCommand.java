package io.github.disparter.tokugawa.discord.bot.commands;

import discord4j.core.event.domain.interaction.SlashCommandInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.object.entity.User;
import io.github.disparter.tokugawa.discord.core.models.NPC;
import io.github.disparter.tokugawa.discord.core.models.Player;
import io.github.disparter.tokugawa.discord.core.models.Relationship;
import io.github.disparter.tokugawa.discord.core.models.Relationship.RelationshipStatus;
import io.github.disparter.tokugawa.discord.core.services.NPCService;
import io.github.disparter.tokugawa.discord.core.services.PlayerService;
import io.github.disparter.tokugawa.discord.core.services.RelationshipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A command that handles player relationships with NPCs.
 * This implementation integrates with RelationshipService to manage player-NPC relationships.
 */
@Component
public class RelationshipCommand implements SlashCommand {

    private final PlayerService playerService;
    private final NPCService npcService;
    private final RelationshipService relationshipService;

    @Autowired
    public RelationshipCommand(PlayerService playerService, 
                              NPCService npcService,
                              RelationshipService relationshipService) {
        this.playerService = playerService;
        this.npcService = npcService;
        this.relationshipService = relationshipService;
    }

    @Override
    public String getName() {
        return "relacionamento";
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
                                .withContent("Comando inválido. Use /relacionamento listar, /relacionamento ver, ou /relacionamento interagir.")
                                .withEphemeral(true);
                    }
                    
                    String subcommand = subcommandOption.get().getName();
                    
                    switch (subcommand) {
                        case "listar":
                            return handleListRelationships(event, player);
                        case "ver":
                            return handleViewRelationship(event, player);
                        case "interagir":
                            return handleInteractWithNPC(event, player);
                        default:
                            return event.reply()
                                    .withContent("Subcomando desconhecido: " + subcommand)
                                    .withEphemeral(true);
                    }
                });
    }
    
    private Mono<Void> handleListRelationships(SlashCommandInteractionEvent event, Player player) {
        List<Relationship> relationships = relationshipService.getRelationshipsForPlayer(player.getId());
        
        if (relationships.isEmpty()) {
            return event.reply()
                    .withContent("Você ainda não tem relacionamentos com NPCs.")
                    .withEphemeral(true);
        }
        
        StringBuilder response = new StringBuilder("Seus relacionamentos:\n\n");
        
        for (Relationship relationship : relationships) {
            NPC npc = relationship.getNpc();
            response.append("**").append(npc.getName()).append("**: ")
                   .append(formatStatus(relationship.getStatus()))
                   .append(" (Afinidade: ").append(relationship.getAffinity()).append(")\n");
        }
        
        return event.reply()
                .withContent(response.toString())
                .withEphemeral(true);
    }
    
    private Mono<Void> handleViewRelationship(SlashCommandInteractionEvent event, Player player) {
        Optional<ApplicationCommandInteractionOption> npcOption = 
                event.getOption("npc");
        
        if (npcOption.isEmpty() || npcOption.get().getValue().isEmpty()) {
            return event.reply()
                    .withContent("Você precisa especificar um NPC.")
                    .withEphemeral(true);
        }
        
        String npcName = npcOption.get().getValue().get().asString();
        NPC npc = npcService.findByName(npcName);
        
        if (npc == null) {
            return event.reply()
                    .withContent("NPC não encontrado: " + npcName)
                    .withEphemeral(true);
        }
        
        Relationship relationship = relationshipService.getRelationship(player.getId(), npc.getId());
        
        StringBuilder response = new StringBuilder("Seu relacionamento com **")
                .append(npc.getName()).append("**:\n\n")
                .append("Status: ").append(formatStatus(relationship.getStatus())).append("\n")
                .append("Afinidade: ").append(relationship.getAffinity()).append("\n");
        
        // Add triggered events if any
        if (!relationship.getTriggeredEvents().isEmpty()) {
            response.append("\nEventos desbloqueados:\n");
            for (String eventId : relationship.getTriggeredEvents()) {
                response.append("- ").append(formatEventName(eventId)).append("\n");
            }
        }
        
        // Check if there's a potential romance event
        String potentialEvent = relationshipService.triggerRomanceEvent(player.getId(), npc.getId());
        if (potentialEvent != null) {
            response.append("\n**Novo evento disponível!** Use /evento iniciar para participar.");
        }
        
        return event.reply()
                .withContent(response.toString())
                .withEphemeral(true);
    }
    
    private Mono<Void> handleInteractWithNPC(SlashCommandInteractionEvent event, Player player) {
        Optional<ApplicationCommandInteractionOption> npcOption = 
                event.getOption("npc");
        Optional<ApplicationCommandInteractionOption> interactionOption = 
                event.getOption("interacao");
        
        if (npcOption.isEmpty() || npcOption.get().getValue().isEmpty() ||
            interactionOption.isEmpty() || interactionOption.get().getValue().isEmpty()) {
            return event.reply()
                    .withContent("Você precisa especificar um NPC e um tipo de interação.")
                    .withEphemeral(true);
        }
        
        String npcName = npcOption.get().getValue().get().asString();
        String interactionType = interactionOption.get().getValue().get().asString();
        
        NPC npc = npcService.findByName(npcName);
        
        if (npc == null) {
            return event.reply()
                    .withContent("NPC não encontrado: " + npcName)
                    .withEphemeral(true);
        }
        
        try {
            Relationship updatedRelationship = 
                    relationshipService.updateAffinityByInteraction(player.getId(), npc.getId(), interactionType);
            
            String response = "Você " + formatInteraction(interactionType) + " com **" + npc.getName() + "**.\n" +
                    "Afinidade atual: " + updatedRelationship.getAffinity() + "\n" +
                    "Status: " + formatStatus(updatedRelationship.getStatus());
            
            return event.reply()
                    .withContent(response)
                    .withEphemeral(true);
        } catch (IllegalArgumentException e) {
            return event.reply()
                    .withContent("Erro: " + e.getMessage())
                    .withEphemeral(true);
        }
    }
    
    private String formatStatus(RelationshipStatus status) {
        switch (status) {
            case STRANGER: return "Desconhecido";
            case ACQUAINTANCE: return "Conhecido";
            case FRIEND: return "Amigo";
            case CLOSE_FRIEND: return "Bom Amigo";
            case BEST_FRIEND: return "Melhor Amigo";
            case CRUSH: return "Interesse Romântico";
            case DATING: return "Namorando";
            case COMMITTED: return "Comprometido";
            case RIVAL: return "Rival";
            case ENEMY: return "Inimigo";
            default: return status.name();
        }
    }
    
    private String formatInteraction(String interactionType) {
        switch (interactionType) {
            case "GREET": return "cumprimentou";
            case "CHAT": return "conversou";
            case "COMPLIMENT": return "elogiou";
            case "GIFT": return "deu um presente";
            case "HELP": return "ajudou";
            case "SPECIAL_FAVOR": return "fez um favor especial";
            case "INSULT": return "insultou";
            case "ARGUE": return "discutiu";
            case "BETRAY": return "traiu";
            default: return "interagiu";
        }
    }
    
    private String formatEventName(String eventId) {
        // Convert event_id to a readable format
        return eventId.replace("_", " ")
                .substring(0, 1).toUpperCase() + 
                eventId.replace("_", " ").substring(1);
    }
}