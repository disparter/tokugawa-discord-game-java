# language: pt
@tecnicas
Funcionalidade: Sistema de Técnicas
  Como um jogador do Tokugawa Discord Game
  Eu quero aprender e dominar diferentes técnicas
  Para melhorar minhas habilidades de combate e utilidade

  Contexto:
    Dado o Discord está esperando uma requisição para buscar informações do usuário "Student"
    E o Discord está esperando uma requisição para buscar informações da guild

  Cenário: Visualizar técnicas disponíveis
    Dado um usuário com Discord ID "123456789012345678"
    E o usuário já está registrado no sistema
    Quando o usuário solicita ver técnicas disponíveis
    Então deve exibir lista de técnicas
    E deve mostrar requisitos para cada técnica
    E deve indicar quais o jogador pode aprender

  Cenário: Aprender nova técnica
    Dado um usuário com Discord ID "123456789012345678"
    E o usuário já está registrado no sistema
    E o jogador atende requisitos para uma técnica
    Quando o usuário inicia aprendizado da técnica
    Então deve começar processo de treinamento
    E deve mostrar progresso do aprendizado
    E deve consumir recursos apropriados

  Cenário: Praticar técnica conhecida
    Dado um usuário com Discord ID "123456789012345678"
    E o usuário já está registrado no sistema
    E o jogador conhece uma técnica
    Quando o usuário pratica a técnica
    Então deve aumentar proficiência
    E deve mostrar melhorias nas estatísticas
    E pode desbloquear variações avançadas

  Cenário: Usar técnica em combate
    Dado um usuário com Discord ID "123456789012345678"
    E o usuário já está registrado no sistema
    E o jogador está em situação de combate
    E o jogador domina uma técnica
    Quando o usuário usa a técnica
    Então deve aplicar efeitos da técnica
    E deve calcular dano ou benefícios
    E deve considerar nível de proficiência

  Cenário: Técnica com pré-requisitos não atendidos
    Dado um usuário com Discord ID "123456789012345678"
    E o usuário já está registrado no sistema
    E existe uma técnica avançada
    Quando o usuário tenta aprender sem requisitos
    Então deve impedir o aprendizado
    E deve listar requisitos faltantes
    E deve sugerir caminho de progressão

  Cenário: Combinar múltiplas técnicas
    Dado um usuário com Discord ID "123456789012345678"
    E o usuário já está registrado no sistema
    E o jogador domina várias técnicas compatíveis
    Quando o usuário executa combinação de técnicas
    Então deve processar combo especial
    E deve aplicar bônus de combinação
    E deve consumir energia adicional

  @slow
  Cenário: Mestre ensinando técnica secreta
    Dado um jogador com relacionamento alto com um mestre
    E o jogador demonstrou dedicação ao treinamento
    Quando o mestre oferece ensinar técnica secreta
    Então deve iniciar questline especial
    E deve exigir desafios únicos
    E deve recompensar com técnica exclusiva

  Esquema do Cenário: Diferentes escolas de técnicas
    Dado um usuário com Discord ID "123456789012345678"
    E o usuário já está registrado no sistema
    Quando o usuário se especializa na escola "<escola>"
    Então deve ter acesso a técnicas específicas

    Exemplos:
      | escola   |
      | samurai  |
      | ninja    |
      | monk     |
      | archer   |