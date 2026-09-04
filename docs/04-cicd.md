# 4. CI/CD (GitHub Actions)

[← Voltar ao índice](README.md)

Cada app tem sua **própria pipeline**, no seu próprio repositório (são submódulos):

- Backend: [`AQConnecta-back/.github/workflows/cicd.yml`](../AQConnecta-back/.github/workflows/cicd.yml)
- Frontend: [`AQConnecta-front/.github/workflows/cicd.yml`](../AQConnecta-front/.github/workflows/cicd.yml)

## 4.1 O que cada pipeline faz

Disparo: `push`/`pull_request` em `main` (e `workflow_dispatch` manual). Em **Pull Request**
roda só a validação (CI); o build de imagem e o deploy só acontecem em `push`/dispatch na `main`.

| Estágio | Backend | Frontend |
|---|---|---|
| **1. Validar (CI)** | `mvn -B clean verify` (testes) | `npm ci` + `npm run lint` + `npm run build` |
| **2. Build & Push** | Build da imagem Docker e push para o registry (tag = SHA curto + `latest`) | idem |
| **3. Deploy** | `kubectl set image` + `rollout status` (kubeconfig) | idem |

## 4.2 Secrets a configurar

Em cada repositório: *Settings → Secrets and variables → Actions*.

| Secret | Conteúdo |
|---|---|
| `REGISTRY_USER` | Usuário do registry (ex.: `admin` no Harbor) |
| `REGISTRY_PASSWORD` | Senha do registry |
| `KUBECONFIG_B64` | Kubeconfig do cluster DACOM em **base64** (`base64 -w0 kubeconfig`). O `server:` dele deve apontar para o endpoint **público** do kube API: `https://200.134.21.86:48643` |

> O registry e o caminho da imagem estão na variável `IMAGE` no topo de cada workflow
> (`harbor.200.134.21.86.nip.io/library/...`). Para usar Docker Hub, troque por
> `docker.io/riume/aqconnecta-backend` (e `-frontend`).

## 4.3 Como o deploy funciona

O kube API do DACOM é exposto num endpoint **público** em `https://200.134.21.86:48643`
(a porta padrão `:6443` fica só intra-UTFPR). Por isso o runner hospedado do GitHub consegue
rodar `kubectl` direto, usando o `KUBECONFIG_B64` — **sem SSH**.

Fluxo: `runner do GitHub` → build & push da imagem no registry → `kubectl set image` (via
kubeconfig, contra `:48643`) → `rollout status`.

> **Segurança (opcional, recomendado):** em vez do kubeconfig de admin, gere um kubeconfig
> a partir de uma **ServiceAccount com RBAC mínimo** (só `patch` nos Deployments do namespace
> `aqconnecta`). Assim o secret do CI não dá acesso total ao cluster.

## 4.4 Pré-requisitos no cluster

- O `KUBECONFIG_B64` precisa apontar para `https://200.134.21.86:48643` (endpoint público).
- Se o registry for o **Harbor com projeto privado**, os Deployments precisam de um
  `imagePullSecret` com as credenciais do Harbor. Alternativa sem config: imagens públicas no
  Docker Hub.

## 4.5 Fluxo de commit (submódulos)

Para versionar mudanças de código, comite **dentro do submódulo** e depois atualize o ponteiro
na raiz:

```bash
# 1) no submódulo alterado (dispara a pipeline dele ao dar push)
cd AQConnecta-back && git add -A && git commit -m "feat: ..." && git push && cd ..

# 2) na raiz, registra o novo ponteiro do submódulo
git add AQConnecta-back && git commit -m "chore: bump backend" && git push
```
