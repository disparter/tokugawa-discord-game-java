package io.github.disparter.tokugawa.discord.steps;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import io.cucumber.java.en.Given;
import io.github.disparter.tokugawa.discord.context.TestContext;
import io.github.disparter.tokugawa.discord.util.JsonTemplateParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

/**
 * Steps para configuração de mocks da API do Discord usando WireMock.
 * Utiliza templates JSON dinâmicos para criar respostas realistas.
 */
@RequiredArgsConstructor
@Slf4j
public class DiscordMockSteps {

    @Autowired
    private WireMockServer wireMockServer;

    @Autowired
    private JsonTemplateParser jsonTemplateParser;

    @Autowired
    private TestContext testContext;

    @Given("o Discord está esperando uma requisição para criar um canal")
    public void mockDiscordCreateChannel() {
        String channelId = testContext.generateUniqueId("channel");
        String guildId = testContext.getStringValue("guild_id").orElse("123456789012345678");

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("channelId", channelId);
        placeholders.put("channelName", "test-channel");
        placeholders.put("guildId", guildId);
        placeholders.put("channelType", "0"); // Text channel

        String responseBody = jsonTemplateParser.parseTemplate(
                "wiremock/__files/discord-channel-create-response.json", 
                placeholders
        );

        wireMockServer.stubFor(post(urlPathMatching("/guilds/.*/channels"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(responseBody)));

        testContext.setDiscordData(channelId, guildId);
        log.info("Configurado mock para criação de canal - ChannelId: {}", channelId);
    }

    @Given("o Discord está esperando uma requisição para buscar informações do usuário {string}")
    public void mockDiscordGetUser(String username) {
        String userId = testContext.generateUniqueId("user");
        
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("userId", userId);
        placeholders.put("username", username);
        placeholders.put("discriminator", "0001");
        placeholders.put("avatar", "a1b2c3d4e5f6g7h8i9j0");

        String responseBody = jsonTemplateParser.parseTemplate(
                "wiremock/__files/discord-user-response.json", 
                placeholders
        );

        wireMockServer.stubFor(get(urlPathMatching("/users/.*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(responseBody)));

        testContext.setValue("target_user_id", userId);
        testContext.setValue("target_username", username);
        log.info("Configurado mock para buscar usuário - UserId: {}, Username: {}", userId, username);
    }

    @Given("o Discord está esperando uma requisição para enviar mensagem")
    public void mockDiscordSendMessage() {
        String messageId = testContext.generateUniqueId("message");
        String channelId = testContext.getCurrentChannelId();
        
        if (channelId == null) {
            channelId = testContext.generateUniqueId("channel");
            testContext.setCurrentChannelId(channelId);
        }

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("messageId", messageId);
        placeholders.put("channelId", channelId);
        placeholders.put("content", "Test message");
        placeholders.put("timestamp", "2024-01-01T12:00:00.000000+00:00");

        String responseBody = jsonTemplateParser.parseTemplate(
                "wiremock/__files/discord-message-response.json", 
                placeholders
        );

        wireMockServer.stubFor(post(urlPathMatching("/channels/.*/messages"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(responseBody)));

        testContext.setValue("last_message_id", messageId);
        log.info("Configurado mock para enviar mensagem - MessageId: {}", messageId);
    }

    @Given("o Discord está esperando uma requisição para buscar informações da guild")
    public void mockDiscordGetGuild() {
        String guildId = testContext.getCurrentGuildId();
        
        if (guildId == null) {
            guildId = testContext.generateUniqueId("guild");
            testContext.setCurrentGuildId(guildId);
        }

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("guildId", guildId);
        placeholders.put("guildName", "Test Guild");
        placeholders.put("ownerId", "987654321098765432");
        placeholders.put("memberCount", "100");

        String responseBody = jsonTemplateParser.parseTemplate(
                "wiremock/__files/discord-guild-response.json", 
                placeholders
        );

        wireMockServer.stubFor(get(urlPathMatching("/guilds/.*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(responseBody)));

        log.info("Configurado mock para buscar guild - GuildId: {}", guildId);
    }

    @Given("o Discord retornará erro {int} para requisições de criação de canal")
    public void mockDiscordCreateChannelError(int statusCode) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("errorCode", String.valueOf(statusCode));
        placeholders.put("errorMessage", getErrorMessage(statusCode));

        String responseBody = jsonTemplateParser.parseTemplate(
                "wiremock/__files/discord-error-response.json", 
                placeholders
        );

        wireMockServer.stubFor(post(urlPathMatching("/guilds/.*/channels"))
                .willReturn(aResponse()
                        .withStatus(statusCode)
                        .withHeader("Content-Type", "application/json")
                        .withBody(responseBody)));

        log.info("Configurado mock de erro para criação de canal - Status: {}", statusCode);
    }

    @Given("o Discord está configurado para rate limiting")
    public void mockDiscordRateLimit() {
        wireMockServer.stubFor(any(urlMatching(".*"))
                .willReturn(aResponse()
                        .withStatus(429)
                        .withHeader("Content-Type", "application/json")
                        .withHeader("X-RateLimit-Limit", "5")
                        .withHeader("X-RateLimit-Remaining", "0")
                        .withHeader("X-RateLimit-Reset-After", "2")
                        .withBody("{\"message\": \"You are being rate limited.\", \"retry_after\": 2.0}")));

        log.info("Configurado mock para rate limiting do Discord");
    }

    private String getErrorMessage(int statusCode) {
        return switch (statusCode) {
            case 400 -> "Bad Request";
            case 401 -> "Unauthorized";
            case 403 -> "Forbidden";
            case 404 -> "Not Found";
            case 429 -> "Too Many Requests";
            case 500 -> "Internal Server Error";
            default -> "Unknown Error";
        };
    }
}