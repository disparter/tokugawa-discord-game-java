package io.github.disparter.tokugawa.discord.steps;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.github.disparter.tokugawa.discord.context.TestContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Hooks para configuração e limpeza antes e depois de cada cenário de teste.
 * Gerencia o estado do WireMock e TestContext entre cenários.
 */
@RequiredArgsConstructor
@Slf4j
public class TestHooks {

    @Autowired
    private WireMockServer wireMockServer;

    @Autowired
    private TestContext testContext;

    @Before
    public void beforeScenario(Scenario scenario) {
        log.info("=== INICIANDO CENÁRIO: {} ===", scenario.getName());
        
        // Configurar contexto do cenário
        testContext.setCurrentScenarioName(scenario.getName());
        testContext.clearAll();
        
        // Reset WireMock server
        if (wireMockServer != null && wireMockServer.isRunning()) {
            wireMockServer.resetAll();
            log.debug("WireMock server resetado para o cenário: {}", scenario.getName());
        }
        
        // Configurações específicas por tags
        configurarPorTags(scenario);
        
        log.debug("Setup do cenário concluído");
    }

    @After
    public void afterScenario(Scenario scenario) {
        log.info("=== FINALIZANDO CENÁRIO: {} - STATUS: {} ===", 
                scenario.getName(), 
                scenario.getStatus());
        
        // Capturar informações para debug em caso de falha
        if (scenario.isFailed()) {
            capturarInformacoesDeDebug(scenario);
        }
        
        // Verificar requisições do WireMock se configurado
        if (isVerifyWireMockRequests(scenario)) {
            verificarRequisicoesWireMock(scenario);
        }
        
        // Limpar contexto
        testContext.clearAll();
        
        // Reset final do WireMock
        if (wireMockServer != null && wireMockServer.isRunning()) {
            wireMockServer.resetAll();
        }
        
        log.debug("Teardown do cenário concluído");
    }

    private void configurarPorTags(Scenario scenario) {
        scenario.getSourceTagNames().forEach(tag -> {
            switch (tag) {
                case "@slow":
                    testContext.setValue("timeout_multiplier", 3);
                    log.debug("Configurado timeout aumentado para cenário lento");
                    break;
                case "@integration":
                    testContext.setValue("integration_test", true);
                    log.debug("Configurado modo de teste de integração");
                    break;
                case "@mock-heavy":
                    testContext.setValue("verify_wiremock", true);
                    log.debug("Configurado verificação rigorosa do WireMock");
                    break;
                default:
                    break;
            }
        });
    }

    private void capturarInformacoesDeDebug(Scenario scenario) {
        try {
            StringBuilder debugInfo = new StringBuilder();
            
            // Informações do contexto de teste
            debugInfo.append("=== INFORMAÇÕES DE DEBUG ===\n");
            debugInfo.append("Cenário: ").append(scenario.getName()).append("\n");
            debugInfo.append("Status HTTP: ").append(testContext.getLastHttpStatusCode()).append("\n");
            debugInfo.append("Resposta HTTP: ").append(testContext.getLastHttpResponse()).append("\n");
            
            // Informações do WireMock
            if (wireMockServer != null && wireMockServer.isRunning()) {
                debugInfo.append("WireMock Requests: ").append(wireMockServer.getAllServeEvents().size()).append("\n");
                wireMockServer.getAllServeEvents().forEach(event -> {
                    debugInfo.append("  - ").append(event.getRequest().getMethod())
                             .append(" ").append(event.getRequest().getUrl())
                             .append(" -> ").append(event.getResponse().getStatus()).append("\n");
                });
            }
            
            // Anexar informações ao cenário
            scenario.attach(debugInfo.toString().getBytes(), "text/plain", "Debug Information");
            
            log.error("Informações de debug capturadas para cenário falhado: {}", scenario.getName());
            
        } catch (Exception e) {
            log.warn("Erro ao capturar informações de debug: {}", e.getMessage());
        }
    }

    private boolean isVerifyWireMockRequests(Scenario scenario) {
        return testContext.getValue("verify_wiremock", Boolean.class).orElse(false) ||
               scenario.getSourceTagNames().contains("@mock-heavy");
    }

    private void verificarRequisicoesWireMock(Scenario scenario) {
        if (wireMockServer == null || !wireMockServer.isRunning()) {
            return;
        }
        
        try {
            int requestCount = wireMockServer.getAllServeEvents().size();
            log.info("Cenário '{}' fez {} requisições para o WireMock", scenario.getName(), requestCount);
            
            // Verificar se há requisições não atendidas
            long unmatchedRequests = wireMockServer.getAllServeEvents().stream()
                    .filter(event -> !event.getWasMatched())
                    .count();
            
            if (unmatchedRequests > 0) {
                log.warn("Cenário '{}' teve {} requisições não atendidas pelo WireMock", 
                        scenario.getName(), unmatchedRequests);
            }
            
        } catch (Exception e) {
            log.warn("Erro ao verificar requisições do WireMock: {}", e.getMessage());
        }
    }
}