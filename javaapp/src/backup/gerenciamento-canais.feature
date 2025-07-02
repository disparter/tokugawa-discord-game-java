# language: pt
Funcionalidade: Gerenciamento de Canais Discord
  Como um administrador do bot Tokugawa
  Eu quero poder gerenciar canais do Discord
  Para organizar as atividades do jogo

  Contexto:
    Dado o Discord está esperando uma requisição para buscar informações da guild
    E o Discord está esperando uma requisição para criar um canal

  Cenário: Criação bem-sucedida de canal de texto
    Dado um usuário com Discord ID "123456789012345678"
    E o usuário já está registrado no sistema
    Quando o usuário solicita a criação de um canal chamado "teste-canal"
    Então o canal deve ser criado com sucesso
    E o canal deve estar visível na guild

  Cenário: Tentativa de criação de canal com nome inválido
    Dado um usuário com Discord ID "123456789012345678"
    E o usuário já está registrado no sistema
    Quando o usuário solicita a criação de um canal com nome ""
    Então deve retornar erro de nome inválido

  Cenário: Criação de canal com Discord indisponível
    Dado o Discord retornará erro 503 para requisições de criação de canal
    E um usuário com Discord ID "123456789012345678"
    E o usuário já está registrado no sistema
    Quando o usuário solicita a criação de um canal chamado "canal-teste"
    Então deve retornar erro de serviço indisponível

  Cenário: Rate limiting durante criação de canal
    Dado o Discord está configurado para rate limiting
    E um usuário com Discord ID "123456789012345678"
    E o usuário já está registrado no sistema
    Quando o usuário solicita a criação de um canal chamado "canal-rapido"
    Então deve retornar erro de rate limit
    E o sistema deve aguardar antes de tentar novamente