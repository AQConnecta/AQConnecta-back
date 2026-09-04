# 2. Rodar localmente

[← Voltar ao índice](README.md)

## Pré-requisitos
- **Docker** + **Docker Compose** (caminho recomendado).
- Para rodar sem Docker: **Java 21** + **Maven**, **Node 22+**, e um **MariaDB 11.4**.

## 2.1 Opção A — Docker Compose (recomendado)

Sobe banco + backend + frontend com um comando.

```bash
# na raiz do repo
cp .env.example .env          # preencha as variáveis (senhas, JWT_SECRET, e-mail opcional)
docker compose up --build
```

Acessos:
- Frontend: <http://localhost:3000>
- Backend (API): <http://localhost:8080>  (health: <http://localhost:8080/actuator/health>)
- MariaDB: `localhost:3306`

Gerar um `JWT_SECRET` forte: `openssl rand -base64 64`.

## 2.2 Opção B — Cada app separado (para desenvolver)

**Banco** (via Docker, o mais simples):
```bash
docker compose up -d db
```

**Backend** (perfil `dev`, porta 8080):
```bash
cd AQConnecta-back
mvn spring-boot:run -Dspring-boot.run.profiles=dev
# o perfil dev já aponta para um MariaDB local; ajuste application-dev.properties se preciso
```

**Frontend** (Vite, porta 3000, com proxy de `/api` → 8080):
```bash
cd AQConnecta-front
npm ci
npm run dev
```

## 2.3 Variáveis de ambiente

Todas estão documentadas em [`.env.example`](../.env.example). **Nunca** commite o `.env` real
(já está no `.gitignore`). As principais:

| Variável | Para quê |
|---|---|
| `DB_ROOT_PASSWORD`, `MYSQL_USER`, `MYSQL_PASSWORD`, `MYSQL_DB` | Banco MariaDB |
| `JWT_SECRET` | Assinatura dos tokens (≥ 64 caracteres aleatórios) |
| `CORS_URLS` | Origens permitidas (ex.: `http://localhost:3000`) |
| `MAIL_*` | SMTP para e-mails de confirmação/recuperação (opcional em dev) |

## 2.4 Comandos úteis

```bash
# backend
mvn -B test                 # roda os testes
mvn -B clean package        # gera o .jar

# frontend
npm run lint                # eslint (estrito: 0 warnings)
npm run build               # tsc + build de produção
```
