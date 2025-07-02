# language: pt
@decisoes
Funcionalidade: Sistema de Decisões
  Como um jogador do Tokugawa Discord Game
  Eu quero tomar decisões importantes que afetem o mundo
  Para influenciar o curso dos eventos e história do jogo

  Contexto:
    Dado o Discord está esperando uma requisição para buscar informações do usuário "DecisionMaker"
    E o Discord está esperando uma requisição para buscar informações da guild
    E o Discord está esperando uma requisição para enviar mensagem

  Cenário: Visualizar decisões pendentes
    Dado um usuário com Discord ID "123456789012345678"
    E o usuário já está registrado no sistema
    Quando o usuário solicita ver decisões pendentes
    Então deve exibir lista de decisões disponíveis
    E deve mostrar prazo para cada decisão
    E deve indicar impacto potencial

  Cenário: Tomar decisão simples
    Dado um usuário com Discord ID "123456789012345678"
    E o usuário já está registrado no sistema
    E existe uma decisão simples disponível
    Quando o usuário faz sua escolha
    Então deve registrar a decisão
    E deve aplicar consequências imediatas
    E deve notificar sobre resultados

  Cenário: Decisão que afeta múltiplos aspectos
    Dado um usuário com Discord ID "123456789012345678"
    E o usuário já está registrado no sistema
    E existe uma decisão complexa
    Quando o usuário toma a decisão
    Então deve afetar múltiplos sistemas
    E deve atualizar relacionamentos
    E pode alterar oportunidades futuras

  Cenário: Decisão comunitária
    Dado múltiplos jogadores podem votar
    E existe uma decisão que afeta toda a comunidade
    Quando os jogadores votam
    Então deve contabilizar todos os votos
    E deve aplicar resultado da maioria
    E deve notificar toda a comunidade

  Cenário: Perder prazo de decisão
    Dado um usuário com Discord ID "123456789012345678"
    E o usuário já está registrado no sistema
    E existe uma decisão com prazo
    Quando o prazo expira sem escolha
    Então deve aplicar decisão padrão
    E pode haver consequências negativas
    E deve notificar sobre prazo perdido

  Cenário: Decisão com pré-requisitos
    Dado um usuário com Discord ID "123456789012345678"
    E o usuário já está registrado no sistema
    E existe uma decisão com requisitos específicos
    Quando o jogador não atende requisitos
    Então deve impedir a participação
    E deve informar requisitos faltantes
    E pode sugerir como obtê-los

  @integration
  Cenário: Decisão que muda o mundo permanentemente
    Dado uma decisão de impacto global está ativa
    E múltiplos jogadores podem influenciar
    Quando a decisão é tomada coletivamente
    Então deve alterar o estado do mundo
    E deve afetar todos os jogadores
    E deve criar novo conteúdo baseado na escolha

  Esquema do Cenário: Diferentes tipos de decisão
    Dado um usuário com Discord ID "123456789012345678"
    E o usuário já está registrado no sistema
    Quando enfrenta uma decisão do tipo "<tipoDecisao>"
    Então deve aplicar mecânicas específicas

    Exemplos:
      | tipoDecisao |
      | moral       |
      | strategic   |
      | economic    |
      | diplomatic  |