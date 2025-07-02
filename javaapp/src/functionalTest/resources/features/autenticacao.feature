# language: pt
Funcionalidade: Autenticação de Usuários
  Como um jogador do Tokugawa Discord Game
  Eu quero poder fazer login e registro no sistema
  Para que eu possa acessar as funcionalidades do jogo

  Contexto:
    Dado o Discord está esperando uma requisição para buscar informações do usuário "TestUser"

  Cenário: Login bem-sucedido de usuário existente
    Dado um usuário com Discord ID "123456789012345678"
    E o usuário já está registrado no sistema
    Quando o usuário tenta fazer login
    Então o login deve ser bem-sucedido
    E o usuário deve receber um token de autenticação

  Cenário: Tentativa de login com usuário não registrado
    Dado um usuário com Discord ID "999888777666555444"
    E o usuário não está registrado no sistema
    Quando o usuário tenta fazer login
    Então deve retornar erro de usuário não encontrado

  Cenário: Registro bem-sucedido de novo usuário
    Dado um usuário com Discord ID "111222333444555666"
    E o usuário não está registrado no sistema
    Quando o usuário tenta se registrar
    Então o registro deve ser bem-sucedido

  Cenário: Tentativa de registro de usuário já existente
    Dado um usuário com Discord ID "777888999000111222"
    E o usuário já está registrado no sistema
    Quando o usuário tenta se registrar
    Então deve retornar erro de usuário já existe

  Esquema do Cenário: Validação de diferentes Discord IDs
    Dado um usuário com Discord ID "<discordId>"
    E o usuário não está registrado no sistema
    Quando o usuário tenta se registrar
    Então o registro deve ser bem-sucedido

    Exemplos:
      | discordId          |
      | 100200300400500600 |
      | 200300400500600700 |
      | 300400500600700800 |