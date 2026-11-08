# OficinaFácil

Sistema web de gestão de oficina mecânica, feito com **Spring Boot** para a disciplina de Backend.
Permite cadastrar clientes, veículos e serviços, abrir ordens de serviço, lançar serviços nelas,
acompanhar o andamento e ver um painel com os números da oficina.

É uma **aplicação web** (não um site estático): todas as telas leem e/ou gravam dados no banco, o layout é
adaptável (computador e celular) e a persistência é feita com JPA/Hibernate.

## Histórias de usuário implementadas

| Código | História | O que faz no banco |
|--------|----------|--------------------|
| HU01 | Como atendente, quero cadastrar, editar e excluir clientes, para manter a base de clientes da oficina. | grava e lê `clientes` |
| HU02 | Como atendente, quero buscar clientes pelo nome, para achar rápido quem chegou na oficina. | lê `clientes` |
| HU03 | Como atendente, quero cadastrar os veículos de cada cliente, para saber qual carro está sendo atendido. | grava e lê `veiculos` |
| HU04 | Como gerente, quero manter um catálogo de serviços com preço e situação (ativo/inativo), para padronizar os orçamentos. | grava e lê `servicos` |
| HU05 | Como atendente, quero abrir uma ordem de serviço para um veículo, informando o problema relatado. | grava `ordens_servico` |
| HU06 | Como atendente, quero lançar e remover serviços de uma ordem, com o total calculado sozinho. | grava e lê `itens_ordem` |
| HU07 | Como mecânico, quero mudar o status da ordem (aberta, em andamento, concluída ou cancelada), para todos saberem em que pé está. | atualiza `ordens_servico` |
| HU08 | Como atendente, quero filtrar as ordens por status e ver o histórico de ordens de um veículo. | lê `ordens_servico` |
| HU09 | Como gerente, quero um painel com ordens por status, faturamento e serviços mais pedidos, para acompanhar a oficina. | lê todas as tabelas |
| HU10 | Como funcionário, quero entrar com usuário e senha, para que só quem trabalha na oficina acesse os dados e cada um veja só o que o seu perfil permite. | lê `usuarios` |

## Regras de negócio

- CPF validado pelos dígitos verificadores e único por cliente.
- Placa única, aceita o padrão antigo (ABC-1234) e o Mercosul (ABC1D23).
- Cliente com veículos não pode ser excluído; veículo com ordens não pode ser excluído.
- Serviço já usado em alguma ordem não pode ser excluído (dá para desativar).
- O preço do serviço é copiado para a ordem na hora do lançamento: mudar o catálogo depois não altera ordens antigas.
- Fluxo de status: `Aberta → Em andamento → Concluída`, e `Cancelada` antes de concluir. Ordem concluída ou cancelada não aceita mais alterações.
- Uma ordem só pode ser concluída se tiver pelo menos um serviço.
- O faturamento do painel soma apenas ordens concluídas e só aparece para o gerente.
- Um veículo só pode ter uma ordem aberta ou em andamento por vez.
- Cada serviço aceita de 1 a 99 unidades por ordem (somando os lançamentos) e a ordem aceita no máximo 50 serviços diferentes.
- O preço lançado na ordem sempre vem do catálogo no banco, nunca do formulário.
- Duas pessoas alterando a mesma ordem ao mesmo tempo: a segunda recebe um aviso em vez de sobrescrever a primeira (controle de versão do Hibernate).

## Tecnologias

- Java 17+
- Spring Boot 3.5.16 (Spring MVC, Spring Data JPA, Spring Security, Bean Validation)
- Thymeleaf (páginas HTML renderizadas no servidor)
- H2 em arquivo (padrão, não precisa instalar nada) ou PostgreSQL (opcional)
- HTML + CSS puro, responsivo, sem framework de front-end
- JUnit 5 para os testes
- Maven (já vem o Maven Wrapper, não precisa instalar Maven)

## Como rodar no VS Code

### 1. Instale o que precisa (uma vez só)

- **JDK 17 ou mais novo** (Temurin 17 ou 21 funcionam bem). Confira no terminal: `java -version`.
- **VS Code** e a extensão **Extension Pack for Java** (da Microsoft). Opcional, mas útil: **Spring Boot Extension Pack**.
- **Git** (`git --version`).

### 2. Abra o projeto

1. Extraia o `.zip`.
2. No VS Code: **File > Open Folder** e escolha a pasta `oficina` (a que tem o arquivo `pom.xml` dentro).
3. Espere o canto inferior direito terminar o "Java: Importing projects" na primeira vez.

### 3. Execute

Pelo terminal do VS Code (**Terminal > New Terminal**), dentro da pasta do projeto:

```bash
# Linux / macOS
./mvnw spring-boot:run

# Windows (PowerShell ou CMD)
.\mvnw.cmd spring-boot:run
```

Na primeira vez ele baixa o Maven e as dependências (leva alguns minutos). Quando aparecer
`Started OficinaApplication`, abra **http://localhost:8080** no navegador.

Outra opção: abrir `src/main/java/br/com/oficina/OficinaApplication.java` e clicar em **Run** acima do método `main`.

Para parar: `Ctrl + C` no terminal.

Na primeira execução o sistema cria as tabelas e já cadastra usuários, clientes, veículos, serviços e ordens de exemplo.

**Login inicial** (tela de entrada em http://localhost:8080):

| Usuário | Senha | Perfil |
|---------|-------|--------|
| `gerente` | `Gerente-2026` | tudo: catálogo de serviços, exclusões, cancelar ordens, faturamento, console do banco |
| `atendente` | `Atendente-2026` | clientes, veículos e ordens (sem catálogo, sem excluir, sem cancelar, sem faturamento) |

Para trocar as senhas antes de apresentar ou publicar, defina as variáveis de ambiente `OFICINA_SENHA_GERENTE` e
`OFICINA_SENHA_ATENDENTE` **antes da primeira execução** (as senhas são gravadas com hash BCrypt na criação do banco).

> **Já rodou uma versão anterior do projeto?** Pare a aplicação e apague a pasta `data/` antes de rodar esta versão.
> Ela adiciona tabelas e colunas novas (usuários, controle de versão das ordens) e o banco de exemplo é recriado do zero.

### 4. Ver o banco de dados

Com a aplicação rodando e logado como `gerente`, abra **http://localhost:8080/h2-console** e use:

- JDBC URL: `jdbc:h2:file:./data/oficina`
- User: `sa`
- Password: (vazio)

Os dados ficam na pasta `data/`. Para zerar o banco, pare a aplicação e apague essa pasta.

### 5. Rodar os testes

```bash
./mvnw test
```

Os testes cobrem a lógica (CPF, fluxo de status, total da ordem, bloqueio de login), as regras de negócio com banco
em memória e a aplicação "por fora" (login, permissões, CSRF, validação dos formulários e abertura de todas as páginas).

### Problemas comuns

| Problema | Solução |
|----------|---------|
| `Port 8080 was already in use` | Rode com outra porta: `./mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=8081` |
| `release version 17 not supported` ou `invalid target release` | O JDK instalado é mais antigo que 17. Instale o JDK 17 ou 21. |
| `JAVA_HOME is not defined correctly` | Aponte a variável `JAVA_HOME` para a pasta do JDK e abra um terminal novo. |
| `Permission denied` ao rodar `./mvnw` (Linux/macOS) | `chmod +x mvnw` |
| VS Code não reconhece o projeto | Confirme que abriu a pasta que contém o `pom.xml` e rode **Java: Clean Java Language Server Workspace**. |

### Usando PostgreSQL em vez do H2 (opcional)

```bash
docker compose up -d
./mvnw spring-boot:run -Dspring-boot.run.profiles=postgres
```

A configuração está em `src/main/resources/application-postgres.properties` (usuário, senha e banco: `oficina`).

## Como subir no GitHub

1. Em https://github.com/new crie um repositório (por exemplo `oficina-springboot`). **Não** marque as opções de criar README, .gitignore ou licença, porque o projeto já tem.
2. No terminal do VS Code, dentro da pasta do projeto:

```bash
# só na primeira vez no computador
git config --global user.name "Seu Nome"
git config --global user.email "seu-email@exemplo.com"

git init
git add .
git commit -m "feat: sistema de gestão de oficina com Spring Boot"
git branch -M main
git remote add origin https://github.com/SEU-USUARIO/oficina-springboot.git
git push -u origin main
```

3. Se o GitHub pedir senha, use um **Personal Access Token** (GitHub > Settings > Developer settings > Personal access tokens) ou entre pelo navegador quando o VS Code oferecer.
4. Para os próximos commits: `git add .`, `git commit -m "mensagem"` e `git push`. No VS Code também dá para usar o painel **Source Control** (ícone de ramificação na barra lateral).

O arquivo `.gitignore` já impede que as pastas `target/` (compilação) e `data/` (banco local) sejam enviadas.

## Estrutura do projeto

```
oficina/
├── pom.xml                      dependências e versão do Java
├── mvnw, mvnw.cmd, .mvn/        Maven Wrapper
├── docker-compose.yml           PostgreSQL opcional
└── src/
    ├── main/
    │   ├── java/br/com/oficina/
    │   │   ├── OficinaApplication.java   ponto de entrada
    │   │   ├── model/                    entidades JPA e o enum StatusOrdem
    │   │   ├── repository/               acesso ao banco (Spring Data)
    │   │   ├── service/                  regras de negócio
    │   │   ├── controller/               recebem as requisições e escolhem a página
    │   │   ├── dto/                      objetos dos formulários e do ranking
    │   │   ├── exception/                exceções do sistema
    │   │   ├── seguranca/                login, perfis, CSRF e bloqueio por tentativas
    │   │   ├── config/DadosIniciais.java usuários e dados de exemplo
    │   │   └── util/Documentos.java      validação de CPF
    │   └── resources/
    │       ├── application.properties    configuração (porta, banco, JPA, segurança)
    │       ├── messages.properties       mensagens de erro de conversão em português
    │       ├── static/css/estilo.css     estilos responsivos
    │       ├── static/js/app.js          confirmações e proteção contra clique duplo
    │       └── templates/                páginas Thymeleaf
    └── test/                             testes automatizados
```

## Principais rotas

| Rota | O que mostra |
|------|--------------|
| `/login` | Entrar no sistema |
| `/` | Painel |
| `/clientes`, `/clientes/novo`, `/clientes/{id}`, `/clientes/{id}/editar` | Clientes |
| `/veiculos`, `/veiculos/novo`, `/veiculos/{id}`, `/veiculos/{id}/editar` | Veículos |
| `/servicos`, `/servicos/novo`, `/servicos/{id}/editar` | Catálogo de serviços |
| `/ordens`, `/ordens?status=ABERTA`, `/ordens/nova`, `/ordens/{id}` | Ordens de serviço |

## Segurança

O que foi feito para o sistema não poder ser burlado, mesmo por quem mexe no navegador ou monta requisições à mão:

- **Login obrigatório** (Spring Security) com senhas em hash BCrypt e dois perfis: gerente e atendente.
- **Permissões conferidas no servidor**: esconder o botão na tela não basta, o endereço também é bloqueado (excluir, catálogo, cancelar ordem, console do banco).
- **Proteção CSRF**: todo formulário POST leva um token; requisição sem token é recusada.
- **Bloqueio de força bruta**: 5 senhas erradas seguidas bloqueiam o usuário por 10 minutos.
- **Validação em três camadas**: formulário (Bean Validation), services (regras de negócio) e banco (unique, not null, chaves estrangeiras). O service valida de novo tudo o que o formulário valida.
- **Lista de campos permitidos** em cada formulário (`@InitBinder`): campo extra enviado à mão é ignorado.
- **Preço, total e datas nunca vêm do navegador**: são lidos do catálogo ou calculados no servidor.
- **Controle de concorrência** nas ordens (`@Version`) e tratamento de conflito com mensagem amigável.
- **Sem vazamento de informação**: páginas de erro não mostram stack trace, SQL nem nomes de tabela.
- **Cookie de sessão** HttpOnly e SameSite, sessão expira em 30 minutos, upload de arquivos desligado.
- Thymeleaf escapa todo texto exibido (proteção contra XSS) e o JPA usa parâmetros (sem SQL injection).
- Botões de envio são desativados após o primeiro clique, evitando registro duplicado.

## Ideias para evoluir

Tela para o gerente criar e desativar usuários e trocar senha, peças e estoque nas ordens, desconto e formas de
pagamento, impressão da ordem em PDF, API REST para um aplicativo mobile.
