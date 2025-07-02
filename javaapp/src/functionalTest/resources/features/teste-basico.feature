# language: pt
@teste-basico
Funcionalidade: Teste Básico dos Steps
  Como desenvolvedor
  Eu quero verificar se os steps básicos estão funcionando
  Para garantir que o sistema de testes está operacional

  Cenário: Teste de autenticação básica
    Dado um usuário com Discord ID "123456789012345678"
    E o usuário já está registrado no sistema
    Quando o usuário tenta fazer login
    Então o login deve ser bem-sucedido

  Cenário: Teste de ação genérica
    Dado um jogador autenticado no sistema
    E que o sistema está funcionando corretamente
    Quando o jogador executa uma ação válida
    Então a ação deve ser executada com sucesso

  Cenário: Teste de clube básico
    Dado um usuário com Discord ID "123456789012345678"
    E o usuário já está registrado no sistema
    Quando o usuário cria um clube chamado "Teste Club"
    Então o clube deve ser criado com sucesso

  Cenário: Teste de perfil de jogador
    Dado um usuário com Discord ID "123456789012345678"
    E o usuário já está registrado no sistema
    Quando o usuário solicita visualizar seu perfil
    Então o perfil deve ser exibido com sucesso