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
@ConfigurationParameter(key = Constants.GLUE_PROPERTY_NAME, value = "io.github.disparter.tokugawa.discord.steps")
@ConfigurationParameter(key = Constants.FILTER_TAGS_PROPERTY_NAME, value = "not @ignore")
@ConfigurationParameter(key = Constants.PLUGIN_PROPERTY_NAME, value = "pretty,html:target/cucumber-reports,json:target/cucumber-reports/Cucumber.json,junit:target/cucumber-reports/Cucumber.xml")
@ConfigurationParameter(key = Constants.EXECUTION_DRY_RUN_PROPERTY_NAME, value = "false")
@ConfigurationParameter(key = Constants.EXECUTION_STRICT_PROPERTY_NAME, value = "true")
public class FunctionalTestRunner {
    // Esta classe serve apenas como configuração para o JUnit Platform
    // A execução real dos testes é feita pelo engine do Cucumber
}