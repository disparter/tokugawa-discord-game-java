package io.github.disparter.tokugawa.discord;

import io.cucumber.junit.platform.engine.Constants;
import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

/**
 * Runner principal para os testes funcionais usando Cucumber e JUnit 5.
 * Configura e executa todos os cenários de teste definidos nos arquivos .feature.
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = "cucumber.glue", value = "io.github.disparter.tokugawa.discord.steps")

@ConfigurationParameter(key = "cucumber.plugin", value = "pretty,junit:build/test-results/functionalTest/cucumber.xml,json:build/test-results/functionalTest/cucumber.json,html:build/reports/cucumber-html")
public class FunctionalTestRunner {
    // Esta classe serve apenas como configuração para o JUnit Platform
    // A execução real dos testes é feita pelo engine do Cucumber
}