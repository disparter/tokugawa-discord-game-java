# language: pt
@jogadores
Funcionalidade: Sistema de Jogadores
  Como um jogador do Tokugawa Discord Game
  Eu quero gerenciar meu perfil e progresso
  Para acompanhar minha evolução no jogo

  Contexto:
    Dado o Discord está esperando uma requisição para buscar informações do usuário "Player"
    E o Discord está esperando uma requisição para buscar informações da guild

  Cenário: Visualizar perfil de jogador
    Dado um usuário com Discord ID "123456789012345678"
    E o usuário já está registrado no sistema
    Quando o usuário solicita visualizar seu perfil
    Então o perfil deve ser exibido com sucesso
    E deve mostrar informações básicas do jogador
    E deve mostrar estatísticas de progresso

  Cenário: Atualizar informações do jogador
    Dado um usuário com Discord ID "123456789012345678"
    E o usuário já está registrado no sistema
    Quando o usuário atualiza suas informações pessoais
    Então as informações devem ser atualizadas com sucesso
    E deve retornar confirmação da atualização

  Cenário: Verificar progresso do jogador
    Dado um usuário com Discord ID "123456789012345678"
    E o usuário já está registrado no sistema
    E o jogador possui progresso no jogo
    Quando o usuário solicita ver seu progresso
    Então deve exibir o progresso detalhado
    E deve mostrar conquistas desbloqueadas
    E deve mostrar próximos objetivos

  Cenário: Jogador inexistente
    Dado um usuário com Discord ID "999999999999999999"
    E o usuário não está registrado no sistema
    Quando o usuário tenta visualizar seu perfil
    Então deve retornar erro de jogador não encontrado
    E deve sugerir fazer o registro

  Esquema do Cenário: Validação de diferentes IDs de Discord
    Dado um usuário com Discord ID "<discordId>"
    E o usuário já está registrado no sistema
    Quando o usuário solicita visualizar seu perfil
    Então o perfil deve ser exibido com sucesso

    Exemplos:
      | discordId          |
      | 100200300400500600 |
      | 200300400500600700 |
      | 300400500600700800 |