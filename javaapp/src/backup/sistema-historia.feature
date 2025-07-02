# language: pt
@historia @story-mode
Funcionalidade: Sistema de História - Story Mode
  Como um jogador do Tokugawa Discord Game
  Eu quero vivenciar narrativas imersivas e interativas
  Para explorar a história rica do período Edo e tomar decisões que moldam minha jornada

  Contexto:
    Dado o Discord está esperando uma requisição para buscar informações do usuário "StoryTeller"
    E o Discord está esperando uma requisição para buscar informações da guild
    E o Discord está esperando uma requisição para enviar mensagem

  @story-progression
  Cenário: Iniciar nova história
    Dado um usuário com Discord ID "123456789012345678"
    E o usuário já está registrado no sistema
    E não existe progresso de história para o jogador
    Quando o usuário inicia uma nova história
    Então deve ser criado um novo progresso de história
    E deve exibir a introdução da narrativa
    E deve apresentar as primeiras escolhas disponíveis

  @story-progression
  Cenário: Continuar história existente
    Dado um usuário com Discord ID "123456789012345678"
    E o usuário já está registrado no sistema
    E existe progresso de história salvo para o jogador
    Quando o usuário continua sua história
    Então deve carregar o progresso salvo
    E deve exibir o resumo dos eventos anteriores
    E deve apresentar as próximas escolhas disponíveis

  @story-choices
  Cenário: Fazer escolha na narrativa
    Dado um usuário com Discord ID "123456789012345678"
    E o usuário já está registrado no sistema
    E o jogador está em um ponto de decisão da história
    Quando o usuário faz uma escolha específica
    Então deve processar a escolha selecionada
    E deve atualizar o progresso da história
    E deve exibir as consequências da escolha
    E deve avançar para a próxima seção da narrativa

  @story-choices
  Cenário: Escolha com pré-requisitos não atendidos
    Dado um usuário com Discord ID "123456789012345678"
    E o usuário já está registrado no sistema
    E existe uma escolha que requer condições específicas
    E o jogador não atende os pré-requisitos
    Quando o usuário tenta fazer a escolha bloqueada
    Então deve ser impedido de fazer a escolha
    E deve mostrar quais requisitos não foram atendidos
    E deve sugerir como atender os requisitos

  @story-chapters
  Cenário: Completar capítulo da história
    Dado um usuário com Discord ID "123456789012345678"
    E o usuário já está registrado no sistema
    E o jogador está próximo ao fim de um capítulo
    Quando o usuário completa todas as seções do capítulo
    Então deve marcar o capítulo como completo
    E deve exibir resumo do capítulo
    E deve desbloquear o próximo capítulo
    E deve conceder recompensas apropriadas

  @story-save-load
  Cenário: Salvar progresso automaticamente
    Dado um usuário com Discord ID "123456789012345678"
    E o usuário já está registrado no sistema
    E o jogador está progredindo na história
    Quando uma escolha importante é feita
    Então deve salvar automaticamente o progresso
    E deve confirmar que o save foi realizado
    E deve manter backup do estado anterior

  @story-save-load
  Cenário: Recuperar de save point
    Dado um usuário com Discord ID "123456789012345678"
    E o usuário já está registrado no sistema
    E existe um save point anterior disponível
    Quando o usuário solicita voltar ao save point
    Então deve restaurar o estado salvo
    E deve confirmar a recuperação
    E deve atualizar o progresso atual

  @story-branches
  Cenário: Narrativa com múltiplas rotas
    Dado um usuário com Discord ID "123456789012345678"
    E o usuário já está registrado no sistema
    E a história possui múltiplas rotas possíveis
    Quando o jogador toma decisões que direcionam para uma rota específica
    Então deve seguir a rota narrativa correspondente
    E deve adaptar eventos futuros baseados nas escolhas
    E deve manter consistência com escolhas anteriores