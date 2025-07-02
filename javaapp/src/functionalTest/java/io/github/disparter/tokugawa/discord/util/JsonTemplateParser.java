package io.github.disparter.tokugawa.discord.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utilitário para carregar e processar templates JSON com substituição de placeholders.
 * Usado para gerar mocks dinâmicos do WireMock a partir de templates estáticos.
 */
@Component
@Slf4j
public class JsonTemplateParser {

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\$\\{([^}]+)}");
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    /**
     * Carrega um template JSON do classpath e substitui os placeholders pelos valores fornecidos
     *
     * @param templatePath Caminho do template no classpath (ex: "wiremock/__files/discord-user-template.json")
     * @param placeholders Map com os valores para substituição dos placeholders
     * @return JSON processado como String
     */
    public String parseTemplate(String templatePath, Map<String, String> placeholders) {
        try {
            String template = loadTemplate(templatePath);
            return replacePlaceholders(template, placeholders);
        } catch (Exception e) {
            log.error("Erro ao processar template JSON: {}", templatePath, e);
            throw new RuntimeException("Falha ao processar template JSON: " + templatePath, e);
        }
    }

    /**
     * Carrega um template JSON e retorna como JsonElement do Gson
     *
     * @param templatePath Caminho do template
     * @param placeholders Placeholders para substituição
     * @return JsonElement processado
     */
    public JsonElement parseTemplateAsJsonElement(String templatePath, Map<String, String> placeholders) {
        String processedJson = parseTemplate(templatePath, placeholders);
        return JsonParser.parseString(processedJson);
    }

    /**
     * Carrega um template JSON e converte para um objeto Java
     *
     * @param templatePath Caminho do template
     * @param placeholders Placeholders para substituição
     * @param targetClass Classe do objeto de destino
     * @return Objeto convertido
     */
    public <T> T parseTemplateAsObject(String templatePath, Map<String, String> placeholders, Class<T> targetClass) {
        String processedJson = parseTemplate(templatePath, placeholders);
        return gson.fromJson(processedJson, targetClass);
    }

    /**
     * Carrega o conteúdo de um template JSON do classpath
     */
    private String loadTemplate(String templatePath) throws IOException {
        ClassPathResource resource = new ClassPathResource(templatePath);
        
        if (!resource.exists()) {
            throw new IllegalArgumentException("Template não encontrado: " + templatePath);
        }

        Path path = resource.getFile().toPath();
        String content = Files.readString(path, StandardCharsets.UTF_8);
        
        log.debug("Template carregado: {} ({} caracteres)", templatePath, content.length());
        return content;
    }

    /**
     * Substitui todos os placeholders no formato ${key} pelos valores fornecidos
     */
    private String replacePlaceholders(String template, Map<String, String> placeholders) {
        if (placeholders == null || placeholders.isEmpty()) {
            log.debug("Nenhum placeholder fornecido, retornando template original");
            return template;
        }

        String result = template;
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(template);
        
        while (matcher.find()) {
            String placeholder = matcher.group(0); // ${key}
            String key = matcher.group(1); // key
            
            String value = placeholders.get(key);
            if (value != null) {
                result = result.replace(placeholder, value);
                log.debug("Placeholder substituído: {} -> {}", placeholder, value);
            } else {
                log.warn("Valor não encontrado para placeholder: {}", placeholder);
            }
        }

        return result;
    }

    /**
     * Valida se um JSON é válido
     */
    public boolean isValidJson(String json) {
        try {
            JsonParser.parseString(json);
            return true;
        } catch (Exception e) {
            log.debug("JSON inválido: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Formata um JSON string com indentação
     */
    public String formatJson(String json) {
        try {
            JsonElement element = JsonParser.parseString(json);
            return gson.toJson(element);
        } catch (Exception e) {
            log.warn("Erro ao formatar JSON, retornando original", e);
            return json;
        }
    }
}