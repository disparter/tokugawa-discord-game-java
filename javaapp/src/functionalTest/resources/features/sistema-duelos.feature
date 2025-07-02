# language: pt
@duelos
Funcionalidade: Sistema de Duelos
  Como um jogador do Tokugawa Discord Game
  Eu quero desafiar outros jogadores para duelos
  Para testar minhas habilidades e ganhar recompensas

  Contexto:
    Dado o Discord está esperando uma requisição para buscar informações do usuário "Duelist"
    E o Discord está esperando uma requisição para buscar informações da guild
    E o Discord está esperando uma requisição para enviar mensagem

  Cenário: Desafiar outro jogador para duelo
    Dado um usuário com Discord ID "123456789012345678"
    E o usuário já está registrado no sistema
    E existe outro jogador disponível para duelo
    Quando o usuário desafia outro jogador para um duelo
    Então o desafio deve ser enviado com sucesso
    E o jogador adversário deve receber notificação
    E deve ser criado um registro de duelo pendente

  Cenário: Aceitar um desafio de duelo
    Dado um usuário com Discord ID "123456789012345678"
    E o usuário já está registrado no sistema
    E o jogador recebeu um desafio de duelo
    Quando o usuário aceita o desafio
    Então o duelo deve ser iniciado
    E ambos os jogadores devem ser notificados
    E deve começar a fase de combate

  Cenário: Recusar um desafio de duelo
    Dado um usuário com Discord ID "123456789012345678"
    E o usuário já está registrado no sistema
    E o jogador recebeu um desafio de duelo
    Quando o usuário recusa o desafio
    Então o desafio deve ser cancelado
    E o desafiante deve ser notificado da recusa
    E deve limpar o registro de duelo pendente

  Cenário: Executar ações durante duelo
    Dado um usuário com Discord ID "123456789012345678"
    E o usuário já está registrado no sistema
    E o jogador está em um duelo ativo
    Quando o usuário executa uma ação de combate
    Então a ação deve ser processada
    E deve calcular dano e efeitos
    E deve atualizar estado do duelo

  Cenário: Finalizar duelo com vitória
    Dado um usuário com Discord ID "123456789012345678"
    E o usuário já está registrado no sistema
    E o jogador está vencendo um duelo
    Quando o duelo é finalizado
    Então deve declarar o vencedor
    E deve distribuir recompensas adequadamente
    E deve atualizar estatísticas dos jogadores

  Cenário: Duelo com timeout
    Dado um usuário com Discord ID "123456789012345678"
    E o usuário já está registrado no sistema
    E o jogador está em um duelo que excedeu o tempo limite
    Quando o tempo do duelo expira
    Então deve finalizar o duelo automaticamente
    E deve determinar resultado por pontos
    E deve aplicar penalidades por timeout

  @integration
  Cenário: Torneio de duelos
    Dado múltiplos jogadores estão inscritos em um torneio
    E todos atendem os requisitos de participação
    Quando o torneio de duelos é iniciado
    Então deve criar brackets de eliminação
    E deve coordenar múltiplos duelos simultâneos
    E deve acompanhar progresso até a final