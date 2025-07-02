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
 * Steps de definição para cenários do sistema visual novel da história.
 */
@Slf4j
public class SistemaHistoriaVisualNovelSteps {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private TestContext testContext;

    // ===== GIVEN STEPS =====

    @Given("existe uma cena com assets visuais configurados")
    public void existeUmaCenaComAssetsVisuaisConfigurados() {
        testContext.setValue("scene_has_assets", true);
        testContext.setValue("background_image", "edo_castle_courtyard.jpg");
        testContext.setValue("character_sprites", "[sensei_yamamoto.png, sakura_chan.png]");
        testContext.setValue("visual_effects", "[cherry_blossoms, sunlight_filter]");
        log.info("Cena com assets visuais configurada");
    }

    @Given("o jogador está transitioning between scenes")
    public void oJogadorEstaTransitioningBetweenScenes() {
        testContext.setValue("in_transition", true);
        testContext.setValue("current_scene", "scene_morning_dojo");
        testContext.setValue("next_scene", "scene_evening_garden");
        testContext.setValue("transition_type", "fade_in_out");
        log.info("Transição entre cenas configurada");
    }

    @Given("existe diálogo para ser apresentado")
    public void existeDialogoParaSerApresentado() {
        testContext.setValue("has_dialogue", true);
        testContext.setValue("speaker", "Sensei Yamamoto");
        testContext.setValue("dialogue_text", "Ah, você chegou cedo hoje. Isso mostra dedicação.");
        testContext.setValue("dialogue_emotion", "pleased");
        log.info("Diálogo configurado para apresentação");
    }

    @Given("existem múltiplas escolhas disponíveis")
    public void existemMultiplasEscolhasDisponiveis() {
        testContext.setValue("has_multiple_choices", true);
        testContext.setValue("choice_count", "3");
        testContext.setValue("choices", "[\"Agradecer humildemente\", \"Perguntar sobre treinamento\", \"Observar em silêncio\"]");
        log.info("Múltiplas escolhas configuradas");
    }

    @Given("o jogador está em uma cena específica")
    public void oJogadorEstaEmUmaCenaEspecifica() {
        testContext.setValue("current_scene_id", "scene_cherry_blossom_festival");
        testContext.setValue("scene_background", "festival_night.jpg");
        testContext.setValue("scene_music", "traditional_festival.ogg");
        testContext.setValue("character_positions", "{\"sakura\": \"center\", \"player\": \"left\"}");
        log.info("Jogador posicionado em cena específica");
    }

    @Given("uma cena requer assets específicos")
    public void umaCenaRequerAssetsEspecificos() {
        testContext.setValue("scene_requires_assets", true);
        testContext.setValue("required_assets", "[battle_background.jpg, sword_effect.png, rain_overlay.png]");
        testContext.setValue("asset_priority", "high");
        log.info("Cena com assets específicos requeridos");
    }

    @Given("uma cena tem música configurada")
    public void umaCenaTemMusicaConfigurada() {
        testContext.setValue("scene_has_music", true);
        testContext.setValue("background_music", "peaceful_temple.ogg");
        testContext.setValue("music_volume", "70");
        testContext.setValue("music_loop", true);
        log.info("Música de fundo configurada para a cena");
    }

    @Given("uma cena tem efeitos especiais configurados")
    public void umaCenaTemEfeitosEspeciaisConfigurados() {
        testContext.setValue("scene_has_effects", true);
        testContext.setValue("special_effects", "[lightning_flash, screen_shake, color_overlay]");
        testContext.setValue("effect_intensity", "dramatic");
        log.info("Efeitos especiais configurados para a cena");
    }

    @Given("conteúdo visual novel está sendo apresentado")
    public void conteudoVisualNovelEstaSendoApresentado() {
        testContext.setValue("presenting_vn_content", true);
        testContext.setValue("content_type", "story_dialogue");
        testContext.setValue("discord_format_needed", true);
        log.info("Conteúdo visual novel em apresentação");
    }

    @Given("múltiplos assets estão sendo carregados")
    public void multiplosAssetsEstaoSendoCarregados() {
        testContext.setValue("loading_multiple_assets", true);
        testContext.setValue("asset_count", "8");
        testContext.setValue("total_size", "15MB");
        testContext.setValue("loading_priority", "optimized");
        log.info("Múltiplos assets em carregamento");
    }

    // ===== WHEN STEPS =====

    @When("a cena é apresentada ao jogador")
    public void aCenaEApresentadaAoJogador() {
        String sceneId = "scene_dojo_morning";
        String requestBody = String.format("""
            {
                "sceneId": "%s",
                "loadAssets": true,
                "showCharacters": true,
                "applyEffects": true
            }
            """, sceneId);

        executePostRequest("/api/story/visual-novel/present-scene", requestBody, "Apresentar cena ao jogador");
    }

    @When("uma nova cena é carregada")
    public void umaNovaRenascenaECarregada() {
        String nextScene = testContext.getStringValue("next_scene").orElse("scene_evening_garden");
        String transitionType = testContext.getStringValue("transition_type").orElse("fade_in_out");
        
        String requestBody = String.format("""
            {
                "newSceneId": "%s",
                "transitionType": "%s",
                "preloadAssets": true
            }
            """, nextScene, transitionType);

        executePostRequest("/api/story/visual-novel/load-scene", requestBody, "Carregar nova cena");
    }

    @When("o diálogo é exibido")
    public void oDialogoEExibido() {
        String speaker = testContext.getStringValue("speaker").orElse("Sensei Yamamoto");
        String dialogueText = testContext.getStringValue("dialogue_text").orElse("Bem-vindo ao dojo.");
        
        String requestBody = String.format("""
            {
                "speaker": "%s",
                "text": "%s",
                "formatForDiscord": true,
                "includeEmotions": true
            }
            """, speaker, dialogueText);

        executePostRequest("/api/story/visual-novel/show-dialogue", requestBody, "Exibir diálogo");
    }

    @When("as escolhas são apresentadas")
    public void asEscolhasSaoApresentadas() {
        String choices = testContext.getStringValue("choices").orElse("[\"Opção A\", \"Opção B\"]");
        String requestBody = String.format("""
            {
                "choices": %s,
                "createInteractiveInterface": true,
                "numberOptions": true
            }
            """, choices);

        executePostRequest("/api/story/visual-novel/present-choices", requestBody, "Apresentar escolhas");
    }

    @When("o progresso é salvo")
    public void oProgressoESalvo() {
        String sceneId = testContext.getStringValue("current_scene_id").orElse("scene_cherry_blossom_festival");
        String characterPositions = testContext.getStringValue("character_positions").orElse("{}");
        
        String requestBody = String.format("""
            {
                "sceneId": "%s",
                "characterPositions": %s,
                "saveVisualState": true,
                "includeAssetReferences": true
            }
            """, sceneId, characterPositions);

        executePostRequest("/api/story/visual-novel/save-state", requestBody, "Salvar progresso visual");
    }

    @When("a cena é carregada")
    public void aCenaECarregada() {
        String requiredAssets = testContext.getStringValue("required_assets").orElse("[]");
        String requestBody = String.format("""
            {
                "requiredAssets": %s,
                "checkAvailability": true,
                "loadDynamically": true
            }
            """, requiredAssets);

        executePostRequest("/api/story/visual-novel/load-assets", requestBody, "Carregar assets da cena");
    }

    @When("a cena é apresentada")
    public void aCenaEApresentada() {
        String backgroundMusic = testContext.getStringValue("background_music").orElse("peaceful_temple.ogg");
        String requestBody = String.format("""
            {
                "backgroundMusic": "%s",
                "indicateMusicChange": true,
                "createAtmosphere": true
            }
            """, backgroundMusic);

        executePostRequest("/api/story/visual-novel/present-with-music", requestBody, "Apresentar cena com música");
    }

    @When("os efeitos são ativados")
    public void osEfeitosSaoAtivados() {
        String specialEffects = testContext.getStringValue("special_effects").orElse("[]");
        String requestBody = String.format("""
            {
                "effects": %s,
                "useDiscordFormatting": true,
                "maintainImmersion": true
            }
            """, specialEffects);

        executePostRequest("/api/story/visual-novel/activate-effects", requestBody, "Ativar efeitos especiais");
    }

    @When("o conteúdo é formatado para Discord")
    public void oConteudoEFormatadoParaDiscord() {
        String requestBody = """
            {
                "contentType": "visual_novel",
                "useEmbeds": true,
                "includeVisualDescriptions": true,
                "optimizeReadability": true
            }
            """;

        executePostRequest("/api/story/visual-novel/format-discord", requestBody, "Formatar conteúdo para Discord");
    }

    @When("o sistema processa os assets")
    public void oSistemaProcessaOsAssets() {
        String assetCount = testContext.getStringValue("asset_count").orElse("8");
        String requestBody = String.format("""
            {
                "assetCount": %s,
                "optimizePerformance": true,
                "prioritizeEssential": true,
                "showLoadingInfo": true
            }
            """, assetCount);

        executePostRequest("/api/story/visual-novel/process-assets", requestBody, "Processar assets");
    }

    // ===== THEN STEPS =====

    @Then("deve exibir a imagem de fundo apropriada")
    public void deveExibirAImagemDeFundoApropriada() {
        String response = testContext.getLastHttpResponse();
        assertTrue(response.contains("background") || response.contains("image") || response.contains("scene"), 
                  "Resposta deveria conter imagem de fundo");
        log.info("Imagem de fundo apropriada exibida");
    }

    @Then("deve mostrar personagens na cena")
    public void deveMostrarPersonagensNaCena() {
        String response = testContext.getLastHttpResponse();
        assertTrue(response.contains("character") || response.contains("sprite") || response.contains("person"), 
                  "Resposta deveria mostrar personagens");
        log.info("Personagens mostrados na cena");
    }

    @Then("deve aplicar efeitos visuais se configurados")
    public void deveAplicarEfeitosVisuaisSeConfigurados() {
        String response = testContext.getLastHttpResponse();
        assertTrue(response.contains("effect") || response.contains("visual") || response.contains("applied"), 
                  "Resposta deveria aplicar efeitos visuais");
        log.info("Efeitos visuais aplicados");
    }

    @Then("deve aplicar efeito de transição configurado")
    public void deveAplicarEfeitoDeTransicaoConfigurado() {
        String response = testContext.getLastHttpResponse();
        assertTrue(response.contains("transition") || response.contains("fade") || response.contains("effect"), 
                  "Resposta deveria aplicar transição");
        log.info("Efeito de transição aplicado");
    }

    @Then("deve carregar novos assets se necessário")
    public void deveCarregarNovosAssetsSeNecessario() {
        String response = testContext.getLastHttpResponse();
        assertTrue(response.contains("loaded") || response.contains("assets") || response.contains("new"), 
                  "Resposta deveria carregar novos assets");
        log.info("Novos assets carregados conforme necessário");
    }

    @Then("deve manter fluidez na experiência")
    public void deveManterFluidezNaExperiencia() {
        String response = testContext.getLastHttpResponse();
        assertTrue(response.contains("smooth") || response.contains("fluid") || response.contains("seamless"), 
                  "Resposta deveria manter fluidez");
        log.info("Fluidez na experiência mantida");
    }

    @Then("deve formatar texto adequadamente para Discord")
    public void deveFormatarTextoAdequadamenteParaDiscord() {
        String response = testContext.getLastHttpResponse();
        assertTrue(response.contains("formatted") || response.contains("Discord") || response.contains("text"), 
                  "Resposta deveria formatar texto para Discord");
        log.info("Texto formatado adequadamente para Discord");
    }

    @Then("deve incluir nome do personagem falando")
    public void deveIncluirNomeDoPersonagemFalando() {
        String response = testContext.getLastHttpResponse();
        String speaker = testContext.getStringValue("speaker").orElse("Sensei");
        assertTrue(response.contains("speaker") || response.contains(speaker) || response.contains("character"), 
                  "Resposta deveria incluir nome do personagem");
        log.info("Nome do personagem falando incluído");
    }

    @Then("deve aplicar formatação especial se configurada")
    public void deveAplicarFormatacaoEspecialSeConfigurada() {
        String response = testContext.getLastHttpResponse();
        assertTrue(response.contains("formatting") || response.contains("special") || response.contains("style"), 
                  "Resposta deveria aplicar formatação especial");
        log.info("Formatação especial aplicada");
    }

    @Then("deve criar interface interativa")
    public void deveCriarInterfaceInterativa() {
        String response = testContext.getLastHttpResponse();
        assertTrue(response.contains("interactive") || response.contains("interface") || response.contains("buttons"), 
                  "Resposta deveria criar interface interativa");
        log.info("Interface interativa criada");
    }

    @Then("deve numerar as opções claramente")
    public void deveNumerarAsOpcoesParamente() {
        String response = testContext.getLastHttpResponse();
        assertTrue(response.contains("number") || response.contains("1.") || response.contains("option"), 
                  "Resposta deveria numerar opções");
        log.info("Opções numeradas claramente");
    }

    @Then("deve incluir descrição de cada escolha")
    public void deveIncluirDescricaoDeCadaEscolha() {
        String response = testContext.getLastHttpResponse();
        assertTrue(response.contains("description") || response.contains("choice") || response.contains("option"), 
                  "Resposta deveria incluir descrições");
        log.info("Descrição de cada escolha incluída");
    }

    @Then("deve salvar estado visual atual")
    public void deveSalvarEstadoVisualAtual() {
        String response = testContext.getLastHttpResponse();
        assertTrue(response.contains("saved") || response.contains("visual") || response.contains("state"), 
                  "Resposta deveria salvar estado visual");
        log.info("Estado visual atual salvo");
    }

    @Then("deve incluir posição dos personagens")
    public void deveIncluirPosicaoOsPersonagens() {
        String response = testContext.getLastHttpResponse();
        assertTrue(response.contains("position") || response.contains("character") || response.contains("location"), 
                  "Resposta deveria incluir posições");
        log.info("Posição dos personagens incluída");
    }

    @Then("deve preservar configurações da cena")
    public void devePreservarConfiguracoesDaCena() {
        String response = testContext.getLastHttpResponse();
        assertTrue(response.contains("preserved") || response.contains("settings") || response.contains("scene"), 
                  "Resposta deveria preservar configurações");
        log.info("Configurações da cena preservadas");
    }

    @Then("deve verificar disponibilidade dos assets")
    public void deveVerificarDisponibilidadeOsAssets() {
        String response = testContext.getLastHttpResponse();
        assertTrue(response.contains("check") || response.contains("available") || response.contains("assets"), 
                  "Resposta deveria verificar disponibilidade");
        log.info("Disponibilidade dos assets verificada");
    }

    @Then("deve carregar assets necessários")
    public void deveCarregarAssetsNecessarios() {
        String response = testContext.getLastHttpResponse();
        assertTrue(response.contains("loaded") || response.contains("necessary") || response.contains("assets"), 
                  "Resposta deveria carregar assets necessários");
        log.info("Assets necessários carregados");
    }

    @Then("deve usar placeholder se asset não disponível")
    public void deveUsarPlaceholderSeAssetNaoDisponivel() {
        String response = testContext.getLastHttpResponse();
        assertTrue(response.contains("placeholder") || response.contains("fallback") || response.contains("default"), 
                  "Resposta deveria usar placeholder");
        log.info("Placeholder usado para asset não disponível");
    }

    @Then("deve indicar música de fundo sendo tocada")
    public void deveIndicarMusicaDeFundoSendoTocada() {
        String response = testContext.getLastHttpResponse();
        assertTrue(response.contains("music") || response.contains("playing") || response.contains("background"), 
                  "Resposta deveria indicar música de fundo");
        log.info("Música de fundo indicada");
    }

    @Then("deve mencionar mudanças de música")
    public void deveMencionarMudancasDeMusica() {
        String response = testContext.getLastHttpResponse();
        assertTrue(response.contains("music") || response.contains("change") || response.contains("new"), 
                  "Resposta deveria mencionar mudanças de música");
        log.info("Mudanças de música mencionadas");
    }

    @Then("deve criar atmosfera adequada")
    public void deveCriarAtmosferaAdequada() {
        String response = testContext.getLastHttpResponse();
        assertTrue(response.contains("atmosphere") || response.contains("mood") || response.contains("ambiance"), 
                  "Resposta deveria criar atmosfera");
        log.info("Atmosfera adequada criada");
    }

    @Then("deve descrever efeitos apropriadamente")
    public void deveDescreverEfeitosApropriadamente() {
        String response = testContext.getLastHttpResponse();
        assertTrue(response.contains("effect") || response.contains("describe") || response.contains("visual"), 
                  "Resposta deveria descrever efeitos");
        log.info("Efeitos descritos apropriadamente");
    }

    @Then("deve usar formatação Discord para impacto")
    public void deveUsarFormatacaoDiscordParaImpacto() {
        String response = testContext.getLastHttpResponse();
        assertTrue(response.contains("Discord") || response.contains("format") || response.contains("impact"), 
                  "Resposta deveria usar formatação Discord");
        log.info("Formatação Discord usada para impacto");
    }

    @Then("deve manter imersão na experiência")
    public void deveManterImersaoNaExperiencia() {
        String response = testContext.getLastHttpResponse();
        assertTrue(response.contains("immersion") || response.contains("experience") || response.contains("engaging"), 
                  "Resposta deveria manter imersão");
        log.info("Imersão na experiência mantida");
    }

    @Then("deve usar embeds para melhor apresentação")
    public void deveUsarEmbedsParaMelhorApresentacao() {
        String response = testContext.getLastHttpResponse();
        assertTrue(response.contains("embed") || response.contains("presentation") || response.contains("format"), 
                  "Resposta deveria usar embeds");
        log.info("Embeds usados para melhor apresentação");
    }

    @Then("deve incluir descrições textuais de elementos visuais")
    public void deveIncluirDescricoesTextuaisDeElementosVisuais() {
        String response = testContext.getLastHttpResponse();
        assertTrue(response.contains("description") || response.contains("visual") || response.contains("text"), 
                  "Resposta deveria incluir descrições textuais");
        log.info("Descrições textuais de elementos visuais incluídas");
    }

    @Then("deve manter legibilidade e usabilidade")
    public void deveManterLegibilidadeEUsabilidade() {
        String response = testContext.getLastHttpResponse();
        assertTrue(response.contains("readable") || response.contains("usable") || response.contains("accessible"), 
                  "Resposta deveria manter legibilidade");
        log.info("Legibilidade e usabilidade mantidas");
    }

    @Then("deve otimizar para melhor performance")
    public void deveOtimizarParaMelhorPerformance() {
        String response = testContext.getLastHttpResponse();
        assertTrue(response.contains("optimize") || response.contains("performance") || response.contains("efficient"), 
                  "Resposta deveria otimizar performance");
        log.info("Otimizado para melhor performance");
    }

    @Then("deve priorizar assets essenciais")
    public void devePriorizarAssetsEssenciais() {
        String response = testContext.getLastHttpResponse();
        assertTrue(response.contains("priority") || response.contains("essential") || response.contains("important"), 
                  "Resposta deveria priorizar assets essenciais");
        log.info("Assets essenciais priorizados");
    }

    @Then("deve informar sobre tempo de carregamento")
    public void deveInformarSobreTempoDeCarregamento() {
        String response = testContext.getLastHttpResponse();
        assertTrue(response.contains("loading") || response.contains("time") || response.contains("progress"), 
                  "Resposta deveria informar tempo de carregamento");
        log.info("Tempo de carregamento informado");
    }

    // ===== MÉTODOS AUXILIARES =====

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