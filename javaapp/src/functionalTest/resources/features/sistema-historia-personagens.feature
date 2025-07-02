# language: pt
@historia @story-characters
Funcionalidade: Sistema de Personagens da História
  Como um jogador do Tokugawa Discord Game
  Eu quero interagir com personagens únicos e memoráveis
  Para construir relacionamentos e influenciar o rumo da narrativa

  Contexto:
    Dado o Discord está esperando uma requisição para buscar informações do usuário "CharacterFan"
    E o Discord está esperando uma requisição para buscar informações da guild

  @character-introduction
  Cenário: Conhecer novo personagem
    Dado um usuário com Discord ID "123456789012345678"
    E o usuário já está registrado no sistema
    E um novo personagem aparece na história
    Quando o jogador encontra o personagem pela primeira vez
    Então deve exibir a apresentação do personagem
    E deve mostrar background e personalidade
    E deve adicionar o personagem ao registro do jogador

  @character-dialogue
  Cenário: Conversar com personagem conhecido
    Dado um usuário com Discord ID "123456789012345678"
    E o usuário já está registrado no sistema
    E o jogador conhece um personagem específico
    Quando o jogador inicia diálogo com o personagem
    Então deve exibir opções de conversa apropriadas
    E deve considerar relacionamento atual com o personagem
    E deve apresentar diálogos contextuais

  @character-relationship
  Cenário: Melhorar relacionamento com personagem
    Dado um usuário com Discord ID "123456789012345678"
    E o usuário já está registrado no sistema
    E o jogador tem um relacionamento neutro com um personagem
    Quando o jogador faz escolhas que agradam o personagem
    Então deve aumentar o nível de relacionamento
    E deve desbloquear novas opções de diálogo
    E pode influenciar eventos futuros da história

  @character-relationship
  Cenário: Deteriorar relacionamento com personagem
    Dado um usuário com Discord ID "123456789012345678"
    E o usuário já está registrado no sistema
    E o jogador tem um bom relacionamento com um personagem
    Quando o jogador faz escolhas que desagradam o personagem
    Então deve diminuir o nível de relacionamento
    E deve alterar o tom dos diálogos
    E pode bloquear certas rotas narrativas

  @character-memory
  Cenário: Personagem lembra de interações passadas
    Dado um usuário com Discord ID "123456789012345678"
    E o usuário já está registrado no sistema
    E o jogador teve interações anteriores com um personagem
    Quando o jogador encontra o personagem novamente
    Então o personagem deve referenciar eventos passados
    E deve adaptar comportamento baseado no histórico
    E deve manter consistência narrativa

  @character-development
  Cenário: Evolução do personagem durante a história
    Dado um usuário com Discord ID "123456789012345678"
    E o usuário já está registrado no sistema
    E um personagem está presente ao longo da narrativa
    Quando eventos significativos acontecem na história
    Então o personagem deve mostrar desenvolvimento
    E deve reagir aos eventos de forma coerente
    E deve influenciar futuras interações

  @character-conflict
  Cenário: Conflito entre personagens
    Dado um usuário com Discord ID "123456789012345678"
    E o usuário já está registrado no sistema
    E existem personagens com interesses conflitantes
    Quando surge tensão entre os personagens
    Então deve forçar o jogador a escolher lados
    E deve afetar relacionamentos com ambos
    E deve impactar desenvolvimento da história

  @character-special-events
  Cenário: Evento especial com personagem favorito
    Dado um usuário com Discord ID "123456789012345678"
    E o usuário já está registrado no sistema
    E o jogador tem relacionamento máximo com um personagem
    Quando condições especiais são atendidas
    Então deve desbloquear evento exclusivo
    E deve oferecer recompensas únicas
    E pode alterar permanentemente a história