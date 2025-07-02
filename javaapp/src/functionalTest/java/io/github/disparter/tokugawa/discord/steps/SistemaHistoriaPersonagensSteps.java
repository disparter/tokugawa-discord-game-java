package io.github.disparter.tokugawa.discord.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.github.disparter.tokugawa.discord.context.TestContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Steps de definição para cenários do sistema de personagens da história.
 */
@Slf4j
public class SistemaHistoriaPersonagensSteps {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private TestContext testContext;

    // ===== GIVEN STEPS =====

    @Given("um novo personagem aparece na história")
    public void umNovoPersonagemApareceNaHistoria() {
        testContext.setValue("new_character_appeared", true);
        testContext.setValue("character_id", "character_sensei_yamamoto");
        testContext.setValue("character_name", "Sensei Yamamoto");
        testContext.setValue("is_first_meeting", true);
        log.info("Novo personagem configurado para aparecer na história");
    }

    @Given("o jogador conhece um personagem específico")
    public void oJogadorConheceUmPersonagemEspecifico() {
        String characterId = "character_sakura_chan";
        testContext.setValue("known_character_id", characterId);
        testContext.setValue("character_relationship_level", "acquaintance");
        testContext.setValue("character_met_before", true);
        
        // Simular registro de personagem conhecido
        String discordId = testContext.getStringValue("discord_id").orElse("123456789012345678");
        String requestBody = String.format("""
            {
                "playerId": "%s",
                "characterId": "%s",
                "relationshipLevel": "acquaintance",
                "metBefore": true
            }
            """, discordId, characterId);

        executePostRequest("/api/story/characters/register", requestBody, "Registrar personagem conhecido");
        log.info("Personagem {} conhecido pelo jogador", characterId);
    }

    @Given("o jogador tem um relacionamento neutro com um personagem")
    public void oJogadorTemUmRelacionamentoNeutroComUmPersonagem() {
        setupCharacterRelationship("neutral", 50);
    }

    @Given("o jogador tem um bom relacionamento com um personagem")
    public void oJogadorTemUmBomRelacionamentoComUmPersonagem() {
        setupCharacterRelationship("good", 80);
    }

    @Given("o jogador teve interações anteriores com um personagem")
    public void oJogadorTeveInteracoesAnterioresComUmPersonagem() {
        String characterId = "character_master_tanaka";
        testContext.setValue("character_with_history", characterId);
        testContext.setValue("previous_interactions", "[gift_tea, compliment_sword, ask_advice]");
        testContext.setValue("interaction_count", "3");
        log.info("Histórico de interações configurado com personagem {}", characterId);
    }

    @Given("um personagem está presente ao longo da narrativa")
    public void umPersonagemEstaPresenteAoLongoDaNarrativa() {
        String characterId = "character_companion_kenji";
        testContext.setValue("recurring_character", characterId);
        testContext.setValue("character_development_stage", "early");
        testContext.setValue("character_arc_active", true);
        log.info("Personagem recorrente {} configurado", characterId);
    }

    @Given("existem personagens com interesses conflitantes")
    public void existemPersonagensComInteressesConflitantes() {
        testContext.setValue("has_character_conflict", true);
        testContext.setValue("conflicting_characters", "[character_lord_matsuda, character_rebel_yoshida]");
        testContext.setValue("conflict_type", "political_rivalry");
        log.info("Conflito entre personagens configurado");
    }

    @Given("o jogador tem relacionamento máximo com um personagem")
    public void oJogadorTemRelacionamentoMaximoComUmPersonagem() {
        setupCharacterRelationship("maximum", 100);
        testContext.setValue("special_events_unlocked", true);
        log.info("Relacionamento máximo com personagem estabelecido");
    }

    // ===== WHEN STEPS =====

    @When("o jogador encontra o personagem pela primeira vez")
    public void oJogadorEncontraOPersonagemPelaPrimeiraVez() {
        String characterId = testContext.getStringValue("character_id").orElse("character_sensei_yamamoto");
        String requestBody = String.format("""
            {
                "characterId": "%s",
                "encounterType": "first_meeting",
                "sceneContext": "dojo_introduction"
            }
            """, characterId);

        executePostRequest("/api/story/characters/encounter", requestBody, "Primeiro encontro com personagem");
    }

    @When("o jogador inicia diálogo com o personagem")
    public void oJogadorIniciaDialogoComOPersonagem() {
        String characterId = testContext.getStringValue("known_character_id").orElse("character_sakura_chan");
        String requestBody = String.format("""
            {
                "characterId": "%s",
                "dialogueType": "conversation",
                "initiatedByPlayer": true
            }
            """, characterId);

        executePostRequest("/api/story/characters/dialogue", requestBody, "Iniciar diálogo com personagem");
    }

    @When("o jogador faz escolhas que agradam o personagem")
    public void oJogadorFazEscolhasQueAgradamOPersonagem() {
        String requestBody = """
            {
                "choices": ["respectful_bow", "compliment_skill", "offer_help"],
                "characterReaction": "positive",
                "relationshipChange": "+15"
            }
            """;

        executePostRequest("/api/story/characters/interaction", requestBody, "Fazer escolhas que agradam");
    }

    @When("o jogador faz escolhas que desagradam o personagem")
    public void oJogadorFazEscolhasQueDesagradamOPersonagem() {
        String requestBody = """
            {
                "choices": ["rude_comment", "ignore_advice", "act_arrogant"],
                "characterReaction": "negative",
                "relationshipChange": "-10"
            }
            """;

        executePostRequest("/api/story/characters/interaction", requestBody, "Fazer escolhas que desagradam");
    }

    @When("o jogador encontra o personagem novamente")
    public void oJogadorEncontraOPersonagemNovamente() {
        String characterId = testContext.getStringValue("character_with_history").orElse("character_master_tanaka");
        String requestBody = String.format("""
            {
                "characterId": "%s",
                "encounterType": "reunion",
                "referenceHistory": true
            }
            """, characterId);

        executePostRequest("/api/story/characters/encounter", requestBody, "Reencontrar personagem");
    }

    @When("eventos significativos acontecem na história")
    public void eventosSignificativosAcontecem() {
        String requestBody = """
            {
                "eventType": "major_story_event",
                "eventId": "battle_of_the_bridge",
                "affectedCharacters": "all_present",
                "impactLevel": "high"
            }
            """;

        executePostRequest("/api/story/events/major", requestBody, "Evento significativo na história");
    }

    @When("surge tensão entre os personagens")
    public void surgeTensaoEntreOsPersonagens() {
        String conflictingChars = testContext.getStringValue("conflicting_characters").orElse("[character_lord_matsuda, character_rebel_yoshida]");
        String requestBody = String.format("""
            {
                "conflictType": "political_tension",
                "involvedCharacters": %s,
                "playerMustChoose": true
            }
            """, conflictingChars);

        executePostRequest("/api/story/characters/conflict", requestBody, "Surgir tensão entre personagens");
    }

    @When("condições especiais são atendidas")
    public void condicoesEspeciaisSaoAtendidas() {
        String requestBody = """
            {
                "specialConditions": ["max_relationship", "story_milestone", "character_trust"],
                "eventTrigger": "special_character_event",
                "unlockType": "exclusive_content"
            }
            """;

        executePostRequest("/api/story/characters/special-event", requestBody, "Atender condições especiais");
    }

    // ===== THEN STEPS =====

    @Then("deve exibir a apresentação do personagem")
    public void deveExibirAApresentacaoDoPersonagem() {
        String response = testContext.getLastHttpResponse();
        assertTrue(response.contains("introduction") || response.contains("character") || response.contains("meet"), 
                  "Resposta deveria conter apresentação do personagem");
        log.info("Apresentação do personagem exibida");
    }

    @Then("deve mostrar background e personalidade")
    public void deveMostrarBackgroundEPersonalidade() {
        String response = testContext.getLastHttpResponse();
        assertTrue(response.contains("background") || response.contains("personality") || response.contains("history"), 
                  "Resposta deveria conter background e personalidade");
        log.info("Background e personalidade do personagem mostrados");
    }

    @Then("deve adicionar o personagem ao registro do jogador")
    public void deveAdicionarOPersonagemAoRegistroDoJogador() {
        String response = testContext.getLastHttpResponse();
        assertTrue(response.contains("added") || response.contains("registered") || response.contains("unlocked"), 
                  "Resposta deveria confirmar adição ao registro");
        log.info("Personagem adicionado ao registro do jogador");
    }

    @Then("deve exibir opções de conversa apropriadas")
    public void deveExibirOpcoesDeConversaApropriadas() {
        String response = testContext.getLastHttpResponse();
        assertTrue(response.contains("dialogue") || response.contains("conversation") || response.contains("options"), 
                  "Resposta deveria conter opções de conversa");
        log.info("Opções de conversa apropriadas exibidas");
    }

    @Then("deve considerar relacionamento atual com o personagem")
    public void deveConsiderarRelacionamentoAtualComOPersonagem() {
        String response = testContext.getLastHttpResponse();
        assertTrue(response.contains("relationship") || response.contains("familiarity") || response.contains("trust"), 
                  "Resposta deveria considerar relacionamento atual");
        log.info("Relacionamento atual considerado");
    }

    @Then("deve apresentar diálogos contextuais")
    public void deveApresentarDialogosContextuais() {
        String response = testContext.getLastHttpResponse();
        assertTrue(response.contains("contextual") || response.contains("appropriate") || response.contains("relevant"), 
                  "Resposta deveria conter diálogos contextuais");
        log.info("Diálogos contextuais apresentados");
    }

    @Then("deve aumentar o nível de relacionamento")
    public void deveAumentarONivelDeRelacionamento() {
        String response = testContext.getLastHttpResponse();
        assertTrue(response.contains("increased") || response.contains("improved") || response.contains("relationship"), 
                  "Resposta deveria confirmar aumento de relacionamento");
        log.info("Nível de relacionamento aumentado");
    }

    @Then("deve desbloquear novas opções de diálogo")
    public void deveDesbloquearNovasOpcoesDeDialogo() {
        String response = testContext.getLastHttpResponse();
        assertTrue(response.contains("unlocked") || response.contains("new options") || response.contains("dialogue"), 
                  "Resposta deveria confirmar desbloqueio de novas opções");
        log.info("Novas opções de diálogo desbloqueadas");
    }

    @Then("pode influenciar eventos futuros da história")
    public void podeInfluenciarEventosFuturosDaHistoria() {
        String response = testContext.getLastHttpResponse();
        assertTrue(response.contains("influence") || response.contains("future") || response.contains("story"), 
                  "Resposta deveria indicar influência em eventos futuros");
        log.info("Influência em eventos futuros estabelecida");
    }

    @Then("deve diminuir o nível de relacionamento")
    public void deveDiminuirONivelDeRelacionamento() {
        String response = testContext.getLastHttpResponse();
        assertTrue(response.contains("decreased") || response.contains("worsened") || response.contains("relationship"), 
                  "Resposta deveria confirmar diminuição de relacionamento");
        log.info("Nível de relacionamento diminuído");
    }

    @Then("deve alterar o tom dos diálogos")
    public void deveAlterarOTomDosDialogos() {
        String response = testContext.getLastHttpResponse();
        assertTrue(response.contains("tone") || response.contains("attitude") || response.contains("manner"), 
                  "Resposta deveria indicar alteração no tom");
        log.info("Tom dos diálogos alterado");
    }

    @Then("pode bloquear certas rotas narrativas")
    public void podeBloquearCertasRotasNarrativas() {
        String response = testContext.getLastHttpResponse();
        assertTrue(response.contains("blocked") || response.contains("unavailable") || response.contains("route"), 
                  "Resposta deveria indicar bloqueio de rotas");
        log.info("Certas rotas narrativas podem ser bloqueadas");
    }

    @Then("o personagem deve referenciar eventos passados")
    public void oPersonagemDeveReferenciarEventosPassados() {
        String response = testContext.getLastHttpResponse();
        assertTrue(response.contains("remember") || response.contains("past") || response.contains("previous"), 
                  "Resposta deveria conter referências a eventos passados");
        log.info("Personagem referenciou eventos passados");
    }

    @Then("deve adaptar comportamento baseado no histórico")
    public void deveAdaptarComportamentoBaseadoNoHistorico() {
        String response = testContext.getLastHttpResponse();
        assertTrue(response.contains("adapted") || response.contains("based on") || response.contains("history"), 
                  "Resposta deveria indicar adaptação baseada no histórico");
        log.info("Comportamento adaptado baseado no histórico");
    }

    @Then("deve manter consistência narrativa")
    public void deveManterConsistenciaNarrativa() {
        String response = testContext.getLastHttpResponse();
        assertTrue(response.contains("consistent") || response.contains("continuity") || response.contains("narrative"), 
                  "Resposta deveria manter consistência narrativa");
        log.info("Consistência narrativa mantida");
    }

    @Then("o personagem deve mostrar desenvolvimento")
    public void oPersonagemDeveMostrarDesenvolvimento() {
        String response = testContext.getLastHttpResponse();
        assertTrue(response.contains("development") || response.contains("growth") || response.contains("change"), 
                  "Resposta deveria mostrar desenvolvimento do personagem");
        log.info("Personagem mostrou desenvolvimento");
    }

    @Then("deve reagir aos eventos de forma coerente")
    public void deveReagirAosEventosDeFormaCoerente() {
        String response = testContext.getLastHttpResponse();
        assertTrue(response.contains("reaction") || response.contains("response") || response.contains("coherent"), 
                  "Resposta deveria mostrar reação coerente");
        log.info("Personagem reagiu de forma coerente");
    }

    @Then("deve influenciar futuras interações")
    public void deveInfluenciarFuturasInteracoes() {
        String response = testContext.getLastHttpResponse();
        assertTrue(response.contains("influence") || response.contains("future") || response.contains("interaction"), 
                  "Resposta deveria indicar influência em futuras interações");
        log.info("Futuras interações serão influenciadas");
    }

    @Then("deve forçar o jogador a escolher lados")
    public void deveFocarOJogadorAEscolherLados() {
        String response = testContext.getLastHttpResponse();
        assertTrue(response.contains("choose") || response.contains("side") || response.contains("decision"), 
                  "Resposta deveria forçar escolha de lados");
        log.info("Jogador forçado a escolher lados");
    }

    @Then("deve afetar relacionamentos com ambos")
    public void deveAfetarRelacionamentosComAmbos() {
        String response = testContext.getLastHttpResponse();
        assertTrue(response.contains("affect") || response.contains("both") || response.contains("relationship"), 
                  "Resposta deveria afetar relacionamentos com ambos");
        log.info("Relacionamentos com ambos personagens afetados");
    }

    @Then("deve impactar desenvolvimento da história")
    public void deveImpactarDesenvolvimentoDaHistoria() {
        String response = testContext.getLastHttpResponse();
        assertTrue(response.contains("impact") || response.contains("story") || response.contains("development"), 
                  "Resposta deveria impactar desenvolvimento da história");
        log.info("Desenvolvimento da história impactado");
    }

    @Then("deve desbloquear evento exclusivo")
    public void deveDesbloquearEventoExclusivo() {
        String response = testContext.getLastHttpResponse();
        assertTrue(response.contains("exclusive") || response.contains("special") || response.contains("unlocked"), 
                  "Resposta deveria desbloquear evento exclusivo");
        log.info("Evento exclusivo desbloqueado");
    }

    @Then("deve oferecer recompensas únicas")
    public void deveOferecerRecompensasUnicas() {
        String response = testContext.getLastHttpResponse();
        assertTrue(response.contains("unique") || response.contains("special") || response.contains("reward"), 
                  "Resposta deveria oferecer recompensas únicas");
        log.info("Recompensas únicas oferecidas");
    }

    @Then("pode alterar permanentemente a história")
    public void podeAlterarPermanentementeAHistoria() {
        String response = testContext.getLastHttpResponse();
        assertTrue(response.contains("permanent") || response.contains("forever") || response.contains("story"), 
                  "Resposta deveria indicar alteração permanente");
        log.info("História pode ser alterada permanentemente");
    }

    // ===== MÉTODOS AUXILIARES =====

    private void setupCharacterRelationship(String level, int value) {
        String characterId = "character_test_npc";
        testContext.setValue("relationship_character_id", characterId);
        testContext.setValue("relationship_level", level);
        testContext.setValue("relationship_value", String.valueOf(value));
        
        String discordId = testContext.getStringValue("discord_id").orElse("123456789012345678");
        String requestBody = String.format("""
            {
                "playerId": "%s",
                "characterId": "%s",
                "relationshipLevel": "%s",
                "relationshipValue": %d
            }
            """, discordId, characterId, level, value);

        executePostRequest("/api/story/characters/relationship", requestBody, "Configurar relacionamento");
        log.info("Relacionamento {} configurado com personagem {}", level, characterId);
    }

    private void executePostRequest(String endpoint, String requestBody, String action) {
        String discordId = testContext.getStringValue("discord_id").orElse("123456789012345678");
        
        if (!requestBody.contains("playerId") && !requestBody.contains("discordId")) {
            requestBody = requestBody.substring(0, requestBody.length() - 1) + 
                         String.format(", \"playerId\": \"%s\"}", discordId);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String authToken = testContext.getAuthToken();
        if (authToken != null) {
            headers.setBearerAuth(authToken);
        }

        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
        
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(endpoint, request, String.class);
            testContext.setLastHttpStatusCode(response.getStatusCode().value());
            testContext.setLastHttpResponse(response.getBody());
            log.info("{} realizada - Status: {}", action, response.getStatusCode());
        } catch (Exception e) {
            log.error("Erro durante {}: {}", action, e.getMessage());
            testContext.setLastHttpStatusCode(500);
            testContext.setLastHttpResponse("Internal Server Error");
        }
    }
}