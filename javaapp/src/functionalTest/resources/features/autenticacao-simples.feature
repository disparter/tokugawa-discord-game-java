# language: pt

@autenticacao
Funcionalidade: Autenticação de Usuários Simplificada
  Como um usuário do jogo Discord Tokugawa
  Eu quero fazer login e registro no sistema
  Para que eu possa acessar todas as funcionalidades do jogo

  @login-bem-sucedido
  Cenário: Login bem-sucedido de usuário existente
    Dado um usuário com Discord ID "123456789012345678"
    E o usuário já está registrado no sistema
    Quando o usuário tenta fazer login
    Então o login deve ser bem-sucedido
    E o usuário deve receber um token de autenticação

  @login-falha
  Cenário: Tentativa de login com usuário não registrado
    Dado um usuário com Discord ID "999999999999999999"
    E o usuário não está registrado no sistema
    Quando o usuário tenta fazer login
    Então deve retornar erro de usuário não encontrado

  @registro-sucesso
  Cenário: Registro bem-sucedido de novo usuário
    Dado um usuário com Discord ID "111222333444555666"
    E o usuário não está registrado no sistema
    Quando o usuário tenta se registrar
    Então o registro deve ser bem-sucedido

  @registro-falha
  Cenário: Tentativa de registro de usuário já existente
    Dado um usuário com Discord ID "987654321098765432"
    E o usuário já está registrado no sistema
    Quando o usuário tenta se registrar
    Então deve retornar erro de usuário já existe