# language: pt
@calendario
Funcionalidade: Sistema de Calendário
  Como um jogador do Tokugawa Discord Game
  Eu quero acompanhar o tempo no mundo do jogo
  Para participar de eventos sazonais e atividades temporais

  Contexto:
    Dado o Discord está esperando uma requisição para buscar informações do usuário "TimeKeeper"
    E o Discord está esperando uma requisição para buscar informações da guild

  Cenário: Visualizar data atual do jogo
    Dado um usuário com Discord ID "123456789012345678"
    E o usuário já está registrado no sistema
    Quando o usuário solicita ver a data atual
    Então deve exibir data do calendário do jogo
    E deve mostrar estação atual
    E deve indicar eventos próximos

  Cenário: Mudança de estação
    Dado o calendário do jogo está ativo
    E uma nova estação está chegando
    Quando a estação muda automaticamente
    Então deve atualizar ambiente do jogo
    E deve ativar eventos sazonais
    E deve notificar todos os jogadores

  Cenário: Evento especial em data específica
    Dado um usuário com Discord ID "123456789012345678"
    E o usuário já está registrado no sistema
    E existe um evento programado para hoje
    Quando a data do evento chega
    Então deve ativar o evento automaticamente
    E deve notificar jogadores elegíveis
    E deve disponibilizar atividades especiais

  Cenário: Verificar histórico de eventos
    Dado um usuário com Discord ID "123456789012345678"
    E o usuário já está registrado no sistema
    Quando o usuário solicita ver histórico
    Então deve exibir eventos passados
    E deve mostrar participação do jogador
    E deve indicar recompensas obtidas

  Cenário: Agendar evento futuro
    Dado um administrador está configurando eventos
    E existe uma data futura disponível
    Quando um novo evento é agendado
    Então deve ser adicionado ao calendário
    E deve notificar antecipadamente
    E deve preparar recursos necessários

  Cenário: Evento recorrente
    Dado existe um evento que se repete
    E o padrão de recorrência está definido
    Quando chega a próxima ocorrência
    Então deve ativar o evento novamente
    E deve aplicar variações se houver
    E deve manter histórico de ocorrências

  @slow
  Cenário: Sincronização com tempo real
    Dado o calendário do jogo avança com tempo real
    E existem eventos baseados em horários
    Quando um período específico é alcançado
    Então deve executar ações programadas
    E deve manter consistência temporal
    E deve lidar com fusos horários

  Esquema do Cenário: Diferentes estações
    Dado o calendário está na estação "<estacao>"
    Quando eventos sazonais são ativados
    Então deve aplicar modificadores da estação

    Exemplos:
      | estacao |
      | spring  |
      | summer  |
      | autumn  |
      | winter  |