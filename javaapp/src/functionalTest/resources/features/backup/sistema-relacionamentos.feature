# language: pt
@relacionamentos
Funcionalidade: Sistema de Relacionamentos
  Como um jogador do Tokugawa Discord Game
  Eu quero desenvolver relacionamentos com NPCs
  Para desbloquear novas oportunidades e rotas românticas

  Contexto:
    Dado o Discord está esperando uma requisição para buscar informações do usuário "Romantic"
    E o Discord está esperando uma requisição para buscar informações da guild

  Cenário: Visualizar relacionamentos atuais
    Dado um usuário com Discord ID "123456789012345678"
    E o usuário já está registrado no sistema
    Quando o usuário solicita ver seus relacionamentos
    Então deve exibir lista de relacionamentos
    E deve mostrar nível de cada relacionamento
    E deve mostrar NPCs disponíveis para interação

  Cenário: Interagir com NPC para melhorar relacionamento
    Dado um usuário com Discord ID "123456789012345678"
    E o usuário já está registrado no sistema
    E existe um NPC disponível para interação
    Quando o usuário interage positivamente com o NPC
    Então deve aumentar nível do relacionamento
    E deve mostrar progresso alcançado
    E deve desbloquear novas opções se aplicável

  Cenário: Dar presente para NPC
    Dado um usuário com Discord ID "123456789012345678"
    E o usuário já está registrado no sistema
    E o jogador possui um item que o NPC aprecia
    Quando o usuário dá o presente ao NPC
    Então deve aceitar o presente
    E deve aumentar significativamente o relacionamento
    E deve mostrar reação positiva do NPC

  Cenário: Tentar dar presente inadequado
    Dado um usuário com Discord ID "123456789012345678"
    E o usuário já está registrado no sistema
    E o jogador oferece um item que o NPC não gosta
    Quando o usuário tenta dar o presente
    Então o NPC deve recusar educadamente
    E pode diminuir ligeiramente o relacionamento
    E deve dar dicas sobre preferências

  Cenário: Desbloquear rota romântica
    Dado um usuário com Discord ID "123456789012345678"
    E o usuário já está registrado no sistema
    E o relacionamento com um NPC atingiu nível alto
    Quando são atendidos os requisitos para romance
    Então deve desbloquear opções românticas
    E deve notificar sobre nova rota disponível
    E deve abrir eventos especiais

  Cenário: Progressão em rota romântica
    Dado um usuário com Discord ID "123456789012345678"
    E o usuário já está registrado no sistema
    E o jogador está em uma rota romântica ativa
    Quando o usuário faz escolhas românticas apropriadas
    Então deve progredir na rota
    E deve desbloquear novas cenas
    E deve mostrar evolução do relacionamento

  Cenário: Conflito entre relacionamentos
    Dado um usuário com Discord ID "123456789012345678"
    E o usuário já está registrado no sistema
    E o jogador desenvolveu múltiplos relacionamentos
    Quando surge um conflito entre NPCs
    Então deve forçar escolha entre relacionamentos
    E deve aplicar consequências adequadas
    E deve alterar dinâmica dos relacionamentos

  @integration
  Cenário: Evento especial de relacionamento
    Dado um jogador com relacionamento de alto nível
    E é uma data especial no calendário do jogo
    Quando é ativado um evento de relacionamento
    Então deve criar experiência única
    E deve oferecer recompensas especiais
    E pode alterar permanentemente o relacionamento