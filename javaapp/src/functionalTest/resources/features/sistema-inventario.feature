# language: pt
@inventario
Funcionalidade: Sistema de Inventário
  Como um jogador do Tokugawa Discord Game
  Eu quero gerenciar meus itens e equipamentos
  Para organizar recursos e melhorar meu desempenho

  Contexto:
    Dado o Discord está esperando uma requisição para buscar informações do usuário "Collector"
    E o Discord está esperando uma requisição para buscar informações da guild

  Cenário: Visualizar inventário vazio
    Dado um usuário com Discord ID "123456789012345678"
    E o usuário já está registrado no sistema
    E o jogador tem um inventário vazio
    Quando o usuário solicita ver seu inventário
    Então deve exibir inventário vazio
    E deve mostrar capacidade total do inventário
    E deve sugerir formas de obter itens

  Cenário: Visualizar inventário com itens
    Dado um usuário com Discord ID "123456789012345678"
    E o usuário já está registrado no sistema
    E o jogador possui itens no inventário
    Quando o usuário solicita ver seu inventário
    Então deve exibir todos os itens organizadamente
    E deve mostrar quantidade de cada item
    E deve mostrar valor total dos itens

  Cenário: Usar um item consumível
    Dado um usuário com Discord ID "123456789012345678"
    E o usuário já está registrado no sistema
    E o jogador possui um item consumível
    Quando o usuário usa o item consumível
    Então o item deve ser consumido
    E deve aplicar o efeito do item
    E deve atualizar o inventário

  Cenário: Equipar um item
    Dado um usuário com Discord ID "123456789012345678"
    E o usuário já está registrado no sistema
    E o jogador possui um equipamento
    Quando o usuário equipa o item
    Então o item deve ser equipado
    E deve atualizar as estatísticas do jogador
    E deve mostrar item anterior se houver

  Cenário: Organizar inventário por categoria
    Dado um usuário com Discord ID "123456789012345678"
    E o usuário já está registrado no sistema
    E o jogador possui diversos tipos de itens
    Quando o usuário solicita organizar por categoria
    Então deve agrupar itens por tipo
    E deve mostrar contadores de cada categoria
    E deve facilitar navegação entre categorias

  Cenário: Inventário cheio
    Dado um usuário com Discord ID "123456789012345678"
    E o usuário já está registrado no sistema
    E o inventário do jogador está cheio
    Quando o usuário tenta adicionar um novo item
    Então deve retornar erro de inventário cheio
    E deve sugerir descartar ou usar itens existentes

  @integration
  Cenário: Transferir item entre jogadores
    Dado existem dois jogadores registrados
    E ambos estão na mesma localização
    E o primeiro jogador possui um item transferível
    Quando é iniciada uma transferência de item
    Então deve processar a transferência com segurança
    E deve atualizar ambos os inventários
    E deve registrar a transação