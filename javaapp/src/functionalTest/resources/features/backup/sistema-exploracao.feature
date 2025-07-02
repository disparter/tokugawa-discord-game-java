# language: pt
@exploracao
Funcionalidade: Sistema de Exploração
  Como um jogador do Tokugawa Discord Game
  Eu quero explorar diferentes localizações
  Para descobrir novos lugares e oportunidades

  Contexto:
    Dado o Discord está esperando uma requisição para buscar informações do usuário "Explorer"
    E o Discord está esperando uma requisição para buscar informações da guild

  Cenário: Visualizar localização atual
    Dado um usuário com Discord ID "123456789012345678"
    E o usuário já está registrado no sistema
    Quando o usuário solicita ver sua localização atual
    Então deve exibir informações da localização
    E deve mostrar NPCs disponíveis na área
    E deve mostrar ações possíveis no local

  Cenário: Mover para localização adjacente
    Dado um usuário com Discord ID "123456789012345678"
    E o usuário já está registrado no sistema
    E existe uma localização adjacente acessível
    Quando o usuário se move para a nova localização
    Então deve atualizar a localização do jogador
    E deve exibir descrição da nova área
    E deve mostrar tempo gasto na viagem

  Cenário: Descobrir nova localização
    Dado um usuário com Discord ID "123456789012345678"
    E o usuário já está registrado no sistema
    E o jogador atende requisitos para descobrir uma área
    Quando o usuário explora uma região inexplorada
    Então deve descobrir a nova localização
    E deve adicionar ao mapa do jogador
    E deve receber recompensa de descoberta

  Cenário: Tentar acessar área bloqueada
    Dado um usuário com Discord ID "123456789012345678"
    E o usuário já está registrado no sistema
    E existe uma área com requisitos específicos
    Quando o usuário tenta acessar sem os requisitos
    Então deve ser impedido de entrar
    E deve mostrar quais requisitos faltam
    E deve sugerir como obter os requisitos

  Cenário: Exploração com consumo de energia
    Dado um usuário com Discord ID "123456789012345678"
    E o usuário já está registrado no sistema
    E o jogador possui energia suficiente
    Quando o usuário realiza uma exploração longa
    Então deve consumir energia apropriadamente
    E deve mostrar energia restante
    E deve calcular tempo para regeneração

  @slow
  Cenário: Expedição em grupo
    Dado múltiplos jogadores estão na mesma localização
    E todos possuem energia suficiente
    Quando é iniciada uma expedição em grupo
    Então deve coordenar o movimento de todos
    E deve distribuir recompensas adequadamente
    E deve aplicar bônus de grupo

  Esquema do Cenário: Diferentes tipos de terreno
    Dado um usuário com Discord ID "123456789012345678"
    E o usuário já está registrado no sistema
    Quando o usuário explora um terreno do tipo "<tipoTerreno>"
    Então deve aplicar modificadores específicos do terreno

    Exemplos:
      | tipoTerreno |
      | mountain    |
      | forest      |
      | city        |
      | temple      |