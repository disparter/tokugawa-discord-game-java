# language: pt
@reputacao
Funcionalidade: Sistema de Reputação
  Como um jogador do Tokugawa Discord Game
  Eu quero construir minha reputação em diferentes facções
  Para desbloquear oportunidades exclusivas e conteúdo especial

  Contexto:
    Dado o Discord está esperando uma requisição para buscar informações do usuário "ReputationSeeker"
    E o Discord está esperando uma requisição para buscar informações da guild

  Cenário: Visualizar reputação atual
    Dado um usuário com Discord ID "123456789012345678"
    E o usuário já está registrado no sistema
    Quando o usuário solicita ver sua reputação
    Então deve exibir status com todas as facções
    E deve mostrar nível de reputação de cada uma
    E deve indicar benefícios desbloqueados

  Cenário: Ganhar reputação através de missões
    Dado um usuário com Discord ID "123456789012345678"
    E o usuário já está registrado no sistema
    E o jogador completa uma missão para uma facção
    Quando a missão é finalizada
    Então deve aumentar reputação com a facção
    E deve notificar sobre ganho de reputação
    E pode desbloquear novas oportunidades

  Cenário: Perder reputação por ações contrárias
    Dado um usuário com Discord ID "123456789012345678"
    E o usuário já está registrado no sistema
    E o jogador realiza ação contra uma facção
    Quando a ação é processada
    Então deve diminuir reputação adequadamente
    E deve notificar sobre perda de reputação
    E pode bloquear certas oportunidades

  Cenário: Conflito entre facções opostas
    Dado um usuário com Discord ID "123456789012345678"
    E o usuário já está registrado no sistema
    E existem facções com interesses opostos
    Quando o jogador ganha reputação com uma facção
    Então pode afetar reputação com facção rival
    E deve aplicar modificadores de conflito
    E deve notificar sobre consequências

  Cenário: Atingir nível máximo de reputação
    Dado um usuário com Discord ID "123456789012345678"
    E o usuário já está registrado no sistema
    E o jogador tem alta reputação com uma facção
    Quando atinge o nível máximo
    Então deve desbloquear conteúdo exclusivo
    E deve receber título especial
    E pode acessar missões únicas

  Cenário: Recuperar reputação perdida
    Dado um usuário com Discord ID "123456789012345678"
    E o usuário já está registrado no sistema
    E o jogador tem reputação negativa
    Quando realiza ações para recuperar reputação
    Então deve aumentar reputação gradualmente
    E deve ser mais difícil que ganho inicial
    E pode requerer ações especiais de reparação

  @slow
  Cenário: Evento especial de facção
    Dado um jogador com alta reputação
    E a facção está organizando evento especial
    Quando o evento é ativado
    Então deve permitir participação exclusiva
    E deve oferecer recompensas únicas
    E pode alterar permanentemente o status

  Esquema do Cenário: Diferentes facções
    Dado um usuário com Discord ID "123456789012345678"
    E o usuário já está registrado no sistema
    Quando interage com a facção "<faccao>"
    Então deve aplicar mecânicas específicas da facção

    Exemplos:
      | faccao    |
      | samurai   |
      | ninja     |
      | merchant  |
      | monk      |
      | imperial  |