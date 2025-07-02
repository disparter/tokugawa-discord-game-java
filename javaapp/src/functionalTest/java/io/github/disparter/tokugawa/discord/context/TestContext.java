package io.github.disparter.tokugawa.discord.context;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Contexto compartilhado entre os steps de teste de um mesmo cenário.
 * Mantém estado e dados temporários necessários durante a execução dos testes funcionais.
 */
@Component
@RequestScope
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
}