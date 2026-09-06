# 3. Produção

[← Voltar ao índice](README.md)

## 3.1 Arquitetura

A aplicação roda em **Kubernetes** (cluster **DACOM**). Os manifests ficam em
`AQConnecta-back/k8s/` (`backend/`, `frontend/`, `database/`).

- **Ingress**: **Envoy Gateway** (Gateway API), com TLS **Let's Encrypt**.
- **Registry**: **Harbor** — `https://harbor.200.134.21.86.nip.io`. Alternativa: Docker Hub
  (`docker.io/riume/aqconnecta-backend` e `-frontend`).

## 3.2 Deploy manual

O deploy é atualizar a imagem do Deployment e acompanhar o rollout:

```bash
export KUBECONFIG=/caminho/kubeconfig      # do cluster DACOM
kubectl -n aqconnecta set image deployment/aqconnecta-backend  backend=<registry>/aqconnecta-backend:<tag>
kubectl -n aqconnecta set image deployment/aqconnecta-frontend frontend=<registry>/aqconnecta-frontend:<tag>
kubectl -n aqconnecta rollout status deployment/aqconnecta-backend
kubectl -n aqconnecta rollout status deployment/aqconnecta-frontend
```

> O deploy automatizado (recomendado) é feito pelas pipelines — veja [CI/CD](04-cicd.md).

## 3.3 Acesso ao cluster

- **Kube API**: exposto num endpoint **público** em `https://200.134.21.86:48643` (a porta
  padrão `:6443` fica só intra-UTFPR). É esse endpoint que as pipelines usam com um kubeconfig
  — ver [CI/CD → deploy](04-cicd.md#43-como-o-deploy-funciona).
- **SSH ao nó (`:22022`)**: uso administrativo/manual.

## 3.4 Pontos de atenção

- **Migrations**: o Flyway roda no **boot do backend**. Um deploy novo pode aplicar migrations
  na base de produção — **revise migrations destrutivas** antes de subir.
- **Registry privado (Harbor)**: se o projeto no Harbor for privado, os Deployments precisam de
  um `imagePullSecret` apontando para as credenciais do Harbor. Se preferir zero configuração,
  use imagens públicas no Docker Hub (basta trocar a variável `IMAGE` nos workflows).
- **Segredos**: em produção entram por **variáveis de ambiente** (backend) e **Kubernetes
  Secrets** (cluster). Nunca versione senhas, kubeconfigs ou chaves.
