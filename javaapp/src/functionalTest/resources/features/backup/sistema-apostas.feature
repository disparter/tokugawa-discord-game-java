# language: pt
@apostas
Funcionalidade: Sistema de Apostas
  Como um jogador do Tokugawa Discord Game
  Eu quero fazer apostas em eventos e duelos
  Para ganhar recursos adicionais através de previsões

  Contexto:
    Dado o Discord está esperando uma requisição para buscar informações do usuário "Bettor"
    E o Discord está esperando uma requisição para buscar informações da guild

  Cenário: Visualizar eventos disponíveis para apostas
    Dado um usuário com Discord ID "123456789012345678"
    E o usuário já está registrado no sistema
    Quando o usuário solicita ver eventos para apostas
    Então deve exibir lista de eventos ativos
    E deve mostrar odds de cada evento
    E deve mostrar prazos para apostas

  Cenário: Realizar aposta em evento
    Dado um usuário com Discord ID "123456789012345678"
    E o usuário já está registrado no sistema
    E o jogador possui recursos suficientes
    E existe um evento ativo para apostas
    Quando o usuário faz uma aposta no evento
    Então a aposta deve ser registrada
    E os recursos devem ser deduzidos
    E deve confirmar detalhes da aposta

  Cenário: Tentar apostar sem recursos suficientes
    Dado um usuário com Discord ID "123456789012345678"
    E o usuário já está registrado no sistema
    E o jogador não possui recursos suficientes
    Quando o usuário tenta fazer uma aposta alta
    Então deve retornar erro de recursos insuficientes
    E deve mostrar saldo atual
    E deve sugerir valor máximo possível

  Cenário: Verificar minhas apostas ativas
    Dado um usuário com Discord ID "123456789012345678"
    E o usuário já está registrado no sistema
    E o jogador possui apostas ativas
    Quando o usuário solicita ver suas apostas
    Então deve listar todas as apostas ativas
    E deve mostrar status de cada aposta
    E deve calcular ganhos potenciais

  Cenário: Receber ganhos de aposta vencedora
    Dado um usuário com Discord ID "123456789012345678"
    E o usuário já está registrado no sistema
    E o jogador tem uma aposta vencedora
    Quando o evento é finalizado
    Então deve calcular ganhos corretamente
    E deve creditar recursos na conta
    E deve enviar notificação de vitória

  Cenário: Perder aposta
    Dado um usuário com Discord ID "123456789012345678"
    E o usuário já está registrado no sistema
    E o jogador tem uma aposta perdedora
    Quando o evento é finalizado
    Então deve confirmar perda da aposta
    E deve atualizar estatísticas de apostas
    E deve enviar notificação adequada

  Cenário: Verificar ranking de apostadores
    Dado um usuário com Discord ID "123456789012345678"
    E o usuário já está registrado no sistema
    Quando o usuário solicita ver ranking de apostas
    Então deve exibir ranking de apostadores
    E deve mostrar estatísticas de cada jogador
    E deve destacar posição do jogador atual

  @mock-heavy
  Cenário: Aposta em duelo entre jogadores
    Dado existem dois jogadores em duelo
    E múltiplos espectadores querem apostar
    Quando são abertas apostas para o duelo
    Então deve permitir apostas em qualquer jogador
    E deve atualizar odds dinamicamente
    E deve processar resultados ao fim do duelo