# language: pt
@eventos
Funcionalidade: Sistema de Eventos
  Como um jogador do Tokugawa Discord Game
  Eu quero participar de eventos especiais
  Para ter experiências únicas e ganhar recompensas exclusivas

  Contexto:
    Dado o Discord está esperando uma requisição para buscar informações do usuário "EventParticipant"
    E o Discord está esperando uma requisição para buscar informações da guild
    E o Discord está esperando uma requisição para enviar mensagem

  Cenário: Visualizar eventos ativos
    Dado um usuário com Discord ID "123456789012345678"
    E o usuário já está registrado no sistema
    Quando o usuário solicita ver eventos ativos
    Então deve exibir lista de eventos disponíveis
    E deve mostrar requisitos de participação
    E deve mostrar prazos e recompensas

  Cenário: Participar de evento comunitário
    Dado um usuário com Discord ID "123456789012345678"
    E o usuário já está registrado no sistema
    E existe um evento comunitário ativo
    E o jogador atende os requisitos
    Quando o usuário se inscreve no evento
    Então deve confirmar participação
    E deve adicionar às atividades do jogador
    E deve notificar sobre próximas etapas

  Cenário: Completar tarefa de evento
    Dado um usuário com Discord ID "123456789012345678"
    E o usuário já está registrado no sistema
    E o jogador está participando de um evento
    Quando o usuário completa uma tarefa do evento
    Então deve registrar progresso
    E deve atualizar status do evento
    E pode desbloquear próximas fases

  Cenário: Evento sazonal com tempo limitado
    Dado um usuário com Discord ID "123456789012345678"
    E o usuário já está registrado no sistema
    E existe um evento sazonal ativo
    Quando o jogador participa do evento sazonal
    Então deve ter acesso a conteúdo exclusivo
    E deve poder colecionar itens especiais
    E deve receber recompensas temáticas

  Cenário: Falhar em evento por tempo
    Dado um usuário com Discord ID "123456789012345678"
    E o usuário já está registrado no sistema
    E o jogador está em um evento com prazo
    Quando o prazo do evento expira
    Então deve finalizar participação automaticamente
    E deve calcular recompensas parciais se houver
    E deve notificar sobre encerramento

  Cenário: Evento colaborativo entre jogadores
    Dado múltiplos jogadores estão participando
    E o evento requer colaboração
    Quando os jogadores trabalham juntos
    Então deve somar contribuições individuais
    E deve distribuir recompensas proporcionalmente
    E pode desbloquear conquistas de grupo

  @integration
  Cenário: Boss raid comunitário
    Dado está ativo um evento de boss raid
    E múltiplos jogadores se juntaram ao raid
    Quando o raid é iniciado
    Então deve coordenar ações de todos
    E deve aplicar mecânicas de raid
    E deve distribuir recompensas raras

  Esquema do Cenário: Diferentes tipos de evento
    Dado um usuário com Discord ID "123456789012345678"
    E o usuário já está registrado no sistema
    Quando participa de um evento do tipo "<tipoEvento>"
    Então deve aplicar mecânicas específicas

    Exemplos:
      | tipoEvento |
      | festival   |
      | competition|
      | raid       |
      | seasonal   |