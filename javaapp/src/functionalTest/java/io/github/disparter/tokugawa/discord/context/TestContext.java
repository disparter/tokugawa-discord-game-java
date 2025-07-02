package io.github.disparter.tokugawa.discord.context;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Contexto compartilhado entre os steps de teste de um mesmo cenário.
 * Mantém estado e dados temporários necessários durante a execução dos testes funcionais.
 */
@Component
@Scope("prototype")
@Data
@Slf4j
public class TestContext {

    private final Map<String, Object> testData = new HashMap<>();
    private String currentScenarioName;
    private String lastHttpResponse;
    private int lastHttpStatusCode;
    private String authToken;
    private Long currentUserId;
    private String currentChannelId;
    private String currentGuildId;

    /**
     * Armazena um valor no contexto do teste
     */
    public void setValue(String key, Object value) {
        log.debug("Storing test data - Key: {}, Value: {}", key, value);
        testData.put(key, value);
    }

    /**
     * Recupera um valor do contexto do teste
     */
    @SuppressWarnings("unchecked")
    public <T> Optional<T> getValue(String key, Class<T> type) {
        Object value = testData.get(key);
        if (value != null && type.isAssignableFrom(value.getClass())) {
            return Optional.of((T) value);
        }
        return Optional.empty();
    }

    /**
     * Recupera um valor do contexto como String
     */
    public Optional<String> getStringValue(String key) {
        return getValue(key, String.class);
    }

    /**
     * Recupera um valor do contexto como Long
     */
    public Optional<Long> getLongValue(String key) {
        return getValue(key, Long.class);
    }

    /**
     * Recupera um valor do contexto como Integer
     */
    public Optional<Integer> getIntegerValue(String key) {
        return getValue(key, Integer.class);
    }

    /**
     * Verifica se uma chave existe no contexto
     */
    public boolean hasValue(String key) {
        return testData.containsKey(key);
    }

    /**
     * Remove um valor do contexto
     */
    public void removeValue(String key) {
        log.debug("Removing test data - Key: {}", key);
        testData.remove(key);
    }

    /**
     * Limpa todos os dados do contexto
     */
    public void clearAll() {
        log.debug("Clearing all test context data");
        testData.clear();
        lastHttpResponse = null;
        lastHttpStatusCode = 0;
        authToken = null;
        currentUserId = null;
        currentChannelId = null;
        currentGuildId = null;
    }

    /**
     * Gera um ID único para uso nos testes
     */
    public String generateUniqueId(String prefix) {
        String uniqueId = prefix + "_" + System.currentTimeMillis();
        setValue("last_generated_id", uniqueId);
        return uniqueId;
    }

    /**
     * Configura dados de autenticação
     */
    public void setAuthenticationData(String token, Long userId) {
        this.authToken = token;
        this.currentUserId = userId;
        setValue("auth_token", token);
        setValue("current_user_id", userId);
        log.debug("Authentication data set - UserId: {}", userId);
    }

    /**
     * Configura dados do Discord
     */
    public void setDiscordData(String channelId, String guildId) {
        this.currentChannelId = channelId;
        this.currentGuildId = guildId;
        setValue("current_channel_id", channelId);
        setValue("current_guild_id", guildId);
        log.debug("Discord data set - ChannelId: {}, GuildId: {}", channelId, guildId);
    }

    // ===== MÉTODOS HTTP =====
    
    /**
     * Define o código de status HTTP da última resposta
     */
    public void setLastHttpStatusCode(int statusCode) {
        this.lastHttpStatusCode = statusCode;
        setValue("last_http_status_code", statusCode);
        log.debug("HTTP Status Code set: {}", statusCode);
    }

    /**
     * Obtém o código de status HTTP da última resposta
     */
    public int getLastHttpStatusCode() {
        return this.lastHttpStatusCode;
    }

    /**
     * Define o corpo da última resposta HTTP
     */
    public void setLastHttpResponse(String response) {
        this.lastHttpResponse = response;
        setValue("last_http_response", response);
        log.debug("HTTP Response set: {}", response != null ? response.substring(0, Math.min(response.length(), 100)) + "..." : "null");
    }

    /**
     * Obtém o corpo da última resposta HTTP
     */
    public String getLastHttpResponse() {
        return this.lastHttpResponse != null ? this.lastHttpResponse : "";
    }

    // ===== MÉTODOS DE AUTENTICAÇÃO =====
    
    /**
     * Define o token de autenticação
     */
    public void setAuthToken(String token) {
        this.authToken = token;
        setValue("auth_token", token);
        log.debug("Auth token set: {}", token != null ? "***TOKEN***" : "null");
    }

    /**
     * Obtém o token de autenticação
     */
    public String getAuthToken() {
        return this.authToken;
    }

    // ===== MÉTODOS DISCORD =====
    
    /**
     * Define o ID do canal atual do Discord
     */
    public void setCurrentChannelId(String channelId) {
        this.currentChannelId = channelId;
        setValue("current_channel_id", channelId);
        log.debug("Current Channel ID set: {}", channelId);
    }

    /**
     * Obtém o ID do canal atual do Discord
     */
    public String getCurrentChannelId() {
        return this.currentChannelId;
    }

    /**
     * Define o ID da guild atual do Discord
     */
    public void setCurrentGuildId(String guildId) {
        this.currentGuildId = guildId;
        setValue("current_guild_id", guildId);
        log.debug("Current Guild ID set: {}", guildId);
    }

    /**
     * Obtém o ID da guild atual do Discord
     */
    public String getCurrentGuildId() {
        return this.currentGuildId;
    }
}