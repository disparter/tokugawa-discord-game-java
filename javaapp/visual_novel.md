Plano de Projeto: Migração do Modo História para uma Visual Novel Engine Web
1. Visão Geral e Objetivos
O objetivo deste marco é refatorar a arquitetura do jogo para separar as responsabilidades, proporcionando uma experiência de narrativa mais rica e imersiva.

Objetivo 1 (Frontend): Criar uma engine de Visual Novel (VN) em HTML/JS que renderize os capítulos da história, personagens, diálogos e escolhas. Esta será a interface principal para o modo história.

Objetivo 2 (Backend): Aprimorar a API REST existente no Spring Boot para que ela sirva como um backend headless para a engine de VN, gerenciando todo o estado do jogo, a lógica da narrativa e a progressão do jogador.

Objetivo 3 (Discord Bot): Transformar os comandos de história do Discord (/historia, /escolha, etc.) em "portais". Em vez de exibir texto no chat, eles fornecerão um link seguro e único para o jogador acessar a VN no navegador.

Objetivo 4 (Foco do Bot): Liberar o bot para se concentrar em mecânicas de jogo mais adequadas ao ambiente do Discord: combate, exploração, duelos, apostas (betting), interações sociais e eventos em tempo real.

2. Arquitetura Proposta
O sistema será composto por 3 componentes principais:

Discord Bot (Gateway de Interação):

Recebe comandos como /historia iniciar.

Autentica o jogador.

Comunica-se com o Backend para gerar um token de sessão.

Responde com uma URL para o Frontend (http://seu.servidor.ip/story?token=...).

Backend (Spring Boot - O Cérebro do Jogo):

Mantém toda a lógica do jogo (NarrativeService, PlayerService, etc.).

Expõe uma API REST robusta para o Frontend.

Gerencia o estado do jogador, progresso, inventário e relacionamentos.

Valida os tokens de sessão para garantir que as requisições do Frontend são seguras.

Frontend (HTML/JS - A Visual Novel Engine):

Uma aplicação web leve (Single Page Application).

Recebe o token de sessão pela URL.

Usa o token para fazer chamadas à API do Backend e buscar o estado atual da história.

Renderiza a cena: background, sprites de personagens, caixa de diálogo, texto e botões de escolha.

Envia a escolha do jogador de volta para a API do Backend.

Atualiza a cena com base na resposta da API, continuando o ciclo.

3. Plano de Execução Detalhado (Fases)
Este plano é dividido em fases para permitir um desenvolvimento incremental e testes contínuos.

Fase 1: Fortalecimento do Backend - Preparando a API para a VN
(Foco: garantir que a API REST possa suportar todas as necessidades da engine de VN)

Mecanismo de Autenticação via Token:

Tarefa: Implementar um sistema de token de sessão de uso único e curta duração.

Detalhes: Crie um novo serviço, SessionTokenService, que possa gerar um token seguro associado a um playerId. Este token deve expirar após alguns minutos. A API deve ter um filtro ou interceptor para validar este token em todas as requisições do modo história.

Expansão dos Endpoints da API no StoryController:

Tarefa: Revisar e criar os endpoints necessários para o fluxo da VN.

Endpoints Necessários:

POST /api/story/session: Chamado pelo bot para gerar um token para um jogador. Retorna o token.

GET /api/story/state: O frontend usará este endpoint (com o token) para obter o estado atual da cena (diálogo atual, personagem, background, opções de escolha).

POST /api/story/choice: O frontend enviará a escolha do jogador para este endpoint. O backend processará a escolha, atualizará o estado do jogo e retornará o novo estado da cena.

Aprimoramento dos DTOs (Data Transfer Objects):

Tarefa: Modificar os DTOs (ChapterDto, ProgressDto, etc.) para incluir informações visuais.

Detalhes: O DTO que representa o estado da cena deve conter URLs para a imagem de fundo, sprites dos personagens na cena (e suas posições/expressões), o texto do diálogo, o nome de quem fala e uma lista de objetos de escolha. Essas URLs podem ser fornecidas pelo AssetService.

Fase 2: Desenvolvimento do Frontend - A Engine de Visual Novel
(Foco: criar a interface web que será a nova cara do modo história)

Estrutura do Projeto Frontend:

Tarefa: Criar a estrutura de arquivos base: index.html, style.css, main.js.

Tecnologia: Pode ser feito com HTML, CSS e JavaScript puros (vanilla) para manter a leveza, ou usando um framework simples como Vue.js ou Svelte.

Design da Interface (UI):

Tarefa: Estruturar o index.html e o style.css para ter os componentes clássicos de uma VN.

Componentes:

Uma div para a imagem de fundo (background).

divs ou img para os sprites dos personagens.

Uma div para a caixa de diálogo (textbox).

Uma área para o nome do personagem que está falando.

Uma div para conter os botões de escolha.

Lógica do Cliente (JavaScript):

Tarefa: Implementar o "cérebro" do frontend no main.js.

Funções Principais:

init(): Ao carregar a página, extrai o token da URL. Se não houver token, exibe uma mensagem de erro.

fetchGameState(): Faz uma chamada fetch para o endpoint GET /api/story/state do backend, enviando o token no cabeçalho de autorização.

renderScene(data): Recebe os dados da API e atualiza o DOM: troca a imagem de fundo, exibe os sprites dos personagens, preenche a caixa de diálogo e cria os botões de escolha.

handleChoiceClick(choiceIndex): Chamada quando um botão de escolha é clicado. Envia a escolha para POST /api/story/choice e, no retorno, chama renderScene com os novos dados.

Gerenciar o ciclo de vida: init -> fetchGameState -> renderScene -> (espera clique) -> handleChoiceClick -> renderScene -> ...

Fase 3: Refatoração do Bot do Discord
(Foco: adaptar os comandos existentes para o novo fluxo)

Modificar Comandos de História:

Tarefa: Alterar a implementação de todos os comandos relacionados à história (/historia, /iniciar, /continuar, etc.).

Nova Lógica:

Obter o ID do jogador do Discord.

Fazer uma chamada interna para o SessionTokenService do backend para gerar um token para aquele jogador.

Ler a URL base do servidor do arquivo application.properties.

Construir a URL completa: http://<ip_do_servidor>:<porta>/story?token=<token_gerado>.

Responder ao usuário no Discord com uma mensagem contendo um Embed e um botão que aponta para essa URL, com um texto como "Clique aqui para continuar sua aventura!".

Fase 4: Integração, Testes e Implantação
Testes End-to-End:

Tarefa: Realizar um teste completo do fluxo: usar o comando no Discord, receber o link, abrir no navegador, jogar um capítulo, fazer escolhas e verificar se o progresso é salvo corretamente no banco de dados.

Configuração de CORS:

Tarefa: Configurar o Cross-Origin Resource Sharing (CORS) no backend Spring Boot para permitir que o domínio do frontend faça requisições à API.

Implantação:

Backend: O plano de implantação existente com Docker e Docker Compose continua válido.

Frontend: Os arquivos estáticos (HTML/CSS/JS) precisam ser servidos por um servidor web. A maneira mais fácil é adicionar um novo serviço ao docker-compose.yml, como um nginx ou caddy, que sirva o diretório do frontend.

Este plano detalhado divide uma tarefa complexa em fases gerenciáveis, garantindo que cada parte do novo sistema seja construída e testada de forma robusta antes da integração final.