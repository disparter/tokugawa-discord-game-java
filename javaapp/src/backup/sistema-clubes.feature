# language: pt
@clubes
Funcionalidade: Sistema de Clubes
  Como um jogador do Tokugawa Discord Game
  Eu quero gerenciar clubes e participar de atividades
  Para interagir com outros jogadores e formar alianças

  Contexto:
    Dado o Discord está esperando uma requisição para buscar informações do usuário "ClubPlayer"
    E o Discord está esperando uma requisição para buscar informações da guild
    E o Discord está esperando uma requisição para criar um canal

  Cenário: Criar um novo clube
    Dado um usuário com Discord ID "123456789012345678"
    E o usuário já está registrado no sistema
    Quando o usuário cria um clube chamado "Samurai Warriors"
    Então o clube deve ser criado com sucesso
    E o jogador deve ser definido como líder do clube
    E deve ser criado um canal no Discord para o clube

  Cenário: Entrar em um clube existente
    Dado um usuário com Discord ID "123456789012345678"
    E o usuário já está registrado no sistema
    E existe um clube chamado "Ninja Masters"
    Quando o usuário solicita entrar no clube "Ninja Masters"
    Então o pedido deve ser processado com sucesso
    E o jogador deve receber confirmação de entrada

  Cenário: Listar membros do clube
    Dado um usuário com Discord ID "123456789012345678"
    E o usuário já está registrado no sistema
    E o usuário é membro do clube "Test Club"
    Quando o usuário solicita ver os membros do clube
    Então deve exibir a lista de membros
    E deve mostrar informações básicas de cada membro
    E deve mostrar os cargos de cada membro

  Cenário: Sair de um clube
    Dado um usuário com Discord ID "123456789012345678"
    E o usuário já está registrado no sistema
    E o usuário é membro do clube "Old Club"
    Quando o usuário decide sair do clube
    Então deve processar a saída com sucesso
    E deve remover o jogador da lista de membros

  Cenário: Tentar criar clube com nome já existente
    Dado um usuário com Discord ID "123456789012345678"
    E o usuário já está registrado no sistema
    E existe um clube chamado "Elite Warriors"
    Quando o usuário tenta criar um clube chamado "Elite Warriors"
    Então deve retornar erro de nome já utilizado
    E deve sugerir um nome alternativo

  @slow
  Cenário: Competição entre clubes
    Dado existem dois clubes "Red Dragons" e "Blue Phoenix"
    E ambos os clubes têm membros ativos
    Quando é iniciada uma competição entre os clubes
    Então deve criar um evento de competição
    E deve notificar todos os membros dos clubes
    E deve configurar sistema de pontuação