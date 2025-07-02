# language: pt
@trading
Funcionalidade: Sistema de Trading
  Como um jogador do Tokugawa Discord Game
  Eu quero negociar com NPCs e outros jogadores
  Para obter itens e recursos necessários

  Contexto:
    Dado o Discord está esperando uma requisição para buscar informações do usuário "Trader"
    E o Discord está esperando uma requisição para buscar informações da guild

  Cenário: Visualizar itens disponíveis para troca
    Dado um usuário com Discord ID "123456789012345678"
    E o usuário já está registrado no sistema
    E o jogador está em uma localização com NPCs comerciantes
    Quando o usuário solicita ver itens disponíveis para troca
    Então deve exibir lista de itens disponíveis
    E deve mostrar preços e requisitos de cada item
    E deve mostrar se o jogador tem recursos suficientes

  Cenário: Realizar troca bem-sucedida com NPC
    Dado um usuário com Discord ID "123456789012345678"
    E o usuário já está registrado no sistema
    E o jogador possui recursos suficientes
    Quando o usuário realiza uma troca com um NPC
    Então a troca deve ser processada com sucesso
    E os itens devem ser adicionados ao inventário
    E os recursos devem ser deduzidos da conta

  Cenário: Tentar troca sem recursos suficientes
    Dado um usuário com Discord ID "123456789012345678"
    E o usuário já está registrado no sistema
    E o jogador não possui recursos suficientes
    Quando o usuário tenta realizar uma troca custosa
    Então deve retornar erro de recursos insuficientes
    E deve mostrar quanto o jogador precisa para completar a troca

  Cenário: Verificar histórico de trocas
    Dado um usuário com Discord ID "123456789012345678"
    E o usuário já está registrado no sistema
    E o jogador já realizou algumas trocas
    Quando o usuário solicita ver seu histórico de trocas
    Então deve exibir histórico detalhado
    E deve mostrar data, itens e valores de cada troca
    E deve calcular estatísticas de trading

  Cenário: NPC com preferências específicas
    Dado um usuário com Discord ID "123456789012345678"
    E o usuário já está registrado no sistema
    E existe um NPC com preferências específicas
    Quando o usuário oferece um item que o NPC prefere
    Então deve receber um bônus na negociação
    E deve ver aumento na relação com o NPC

  Esquema do Cenário: Diferentes tipos de troca
    Dado um usuário com Discord ID "123456789012345678"
    E o usuário já está registrado no sistema
    Quando o usuário realiza uma troca do tipo "<tipoTroca>"
    Então deve processar a troca adequadamente

    Exemplos:
      | tipoTroca |
      | item      |
      | gold      |
      | service   |