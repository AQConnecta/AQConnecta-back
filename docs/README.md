# AQConnecta — Documentação

Plataforma web de **extensão universitária e vagas**: conecta alunos, professores e projetos.

## Por onde começar

Se você está entrando no projeto, leia nesta ordem:

| # | Documento | O que cobre |
|---|---|---|
| 1 | [Estrutura e convenções](01-estrutura-e-convencoes.md) | Como o repositório é organizado, a stack, as convenções de código e um mapa de pastas. **Leia antes de mexer em qualquer coisa.** |
| 2 | [Rodar localmente](02-rodar-localmente.md) | Subir o projeto na sua máquina (Docker Compose ou cada app separado). |
| 3 | [Produção](03-producao.md) | Como a aplicação roda em produção (Kubernetes, registry, TLS, observabilidade) e como fazer deploy manual. |
| 4 | [CI/CD (GitHub Actions)](04-cicd.md) | As pipelines automatizadas de front e back, secrets necessários e o fluxo de entrega. |

## O que é o projeto

Principais domínios:

- **Usuários / Perfil** — cadastro (com confirmação por e-mail), login (JWT), foto de perfil,
  formação acadêmica (com diploma), experiências, endereço, competências e currículos.
- **Vagas** — publicação, busca, candidatura (com currículo) e vínculo opcional a um projeto.
- **Projetos** — CRUD com papéis (Dono/Editor/Visualizador), membros e convites, seguidores,
  posts (rich text), comentários, denúncias e vinculação de vagas.
- **Competências** — base categorizada por área de atuação (importada do ESCO), sugestão de
  novas competências com aprovação de admin e "competências quentes".
- **Administração** — moderação de denúncias, aprovação de competências, gestão de usuários,
  projetos, vagas e universidades.

Em uma frase: **frontend React + Vite (TypeScript)** consumindo uma **API Spring Boot
(Java 21)** com **MariaDB**, containerizado e publicado em **Kubernetes**.
