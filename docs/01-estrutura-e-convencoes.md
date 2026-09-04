# 1. Estrutura e convenções

[← Voltar ao índice](README.md)

## 1.1 Estrutura do repositório

O repositório-raiz é o **orquestrador**. Os dois apps são **submódulos git** (repositórios
próprios):

```
aqconnecta/                     # repo-raiz (orquestração)
├── docker-compose.yaml         # sobe db + backend + frontend localmente
├── .env.example                # modelo das variáveis (copie para .env)
├── .gitignore                  # bloqueia .env, kubeconfigs, chaves, dirs duplicados
├── docs/                       # esta documentação
├── AQConnecta-back/            # submódulo: API Spring Boot  ← CANÔNICO
└── AQConnecta-front/           # submódulo: SPA React/Vite   ← CANÔNICO
```

> ⚠️ **Ignore** os diretórios `aqconnecta-front-2/`, `aqconnecta-front-refactor/` e
> `Refactoraqconnectafrontend/` — são cópias/rascunhos, **não** são o código oficial (já estão
> no `.gitignore`). O front oficial é **`AQConnecta-front/`**.

> Como são submódulos, o `git status` na raiz mostra `AQConnecta-back`/`AQConnecta-front` como
> uma única linha. As alterações de código são commitadas **dentro de cada submódulo**; o
> repo-raiz só guarda o ponteiro. Veja o fluxo de commit em [CI/CD](04-cicd.md#45-fluxo-de-commit-submódulos).

## 1.2 Tecnologias

| Camada | Tecnologias |
|---|---|
| **Backend** (`AQConnecta-back`) | Spring Boot 3.3.5, Java 21, Maven, MariaDB 11.4, Flyway (migrations), JWT (jjwt), Lombok, Micrometer (Prometheus + tracing OTLP) |
| **Frontend** (`AQConnecta-front`) | React 18, Vite, TypeScript, TailwindCSS, Radix UI, axios, React Router, TipTap (rich text), sonner (toasts) |
| **Infra** | Docker, Kubernetes, Gateway API (Envoy Gateway), Prometheus/Grafana/Tempo (observabilidade), Harbor (registry) |

## 1.3 Convenções que você precisa saber

**Backend**
- **IDs** são UUID armazenados como `BINARY(36)`. Tabelas usam prefixo `TB_` (entidades) e
  `RL_` (relações N:N); colunas em `SNAKE_CASE`.
- Toda resposta da API vem num envelope `ResponseHandler`: `{ "message", "status", "data" }`.
  O payload útil está em `data`.
- **Migrations** ficam em `src/main/resources/db/migration` (`V<n>__*.sql`) e rodam
  **automaticamente no boot** via Flyway. Nunca edite uma migration já aplicada.
- **Perfis Spring**: `dev` (`application-dev.properties`, para local) e `prod`
  (`application-prod.properties`, tudo via variáveis de ambiente). Segredos de prod **nunca**
  ficam no código — sempre `${VAR}`.
- Métricas em `/actuator/prometheus`, health em `/actuator/health`.

**Frontend**
- `axios` usa `baseURL: /api`. Em **dev**, o Vite faz proxy de `/api` → `http://localhost:8080`
  (removendo o prefixo `/api`). Em **produção**, o nginx do container faz esse proxy.
- Resposta tipada como `{ data: { data: T } }` → o payload real é `res.data.data`.
- Estado de auth no `AuthContext`; erros passam por `lib/errors` (`handleApiError`).
- Componentes de UI em `components/ui` (Radix + Tailwind).

## 1.4 Mapa de pastas

- `AQConnecta-back/src/main/java/com/aqConnecta/` → `controller/`, `service/`, `model/`,
  `DTOs/`, `repository/`, `config/` (Security etc.).
- `AQConnecta-back/k8s/` → manifests do cluster **Qualicloud** (teste).
- `AQConnecta-back/k8s-dacom/` → manifests do cluster **DACOM** (produção): `backend/`,
  `frontend/`, `database/`, `gateway/`, `acme/` (TLS Let's Encrypt), `monitoring/`
  (Prometheus/Grafana/Tempo/alertas), `harbor/` (registry), `azure-agent/`.
- `AQConnecta-back/.github/workflows/` e `AQConnecta-front/.github/workflows/` → pipelines CI/CD.
- `AQConnecta-front/src/` → `pages/`, `components/`, `services/endpoints/` (chamadas de API),
  `contexts/`, `hooks/`, `routes/`.
