# Manifests K8s — AQConnecta

Este diretório contém **toda a infraestrutura** da plataforma no namespace `aqconnecta`:
banco (MariaDB), backend, e os recursos de borda do frontend (Service, Ingress, ConfigMap).

O **Deployment do frontend** fica em `../AQConnecta-front/k8s/deployment.yaml`, pois é o
artefato cuja imagem muda com cada release de UI.

## Estrutura

```
k8s/
├── namespace.yaml
├── kustomization.yaml
├── database/
│   ├── configmap.yaml
│   ├── secret.example.yaml      # NÃO contém valores reais
│   ├── pvc.yaml
│   ├── service.yaml
│   └── statefulset.yaml
├── backend/
│   ├── configmap.yaml
│   ├── secret.example.yaml      # NÃO contém valores reais
│   ├── service.yaml
│   ├── deployment.yaml
│   └── ingress.yaml
└── frontend/
    ├── configmap.yaml           # BACKEND_URL para o proxy do nginx
    ├── service.yaml
    └── ingress.yaml
```

## Primeiro deploy

```bash
# 1. Criar Secrets reais (NÃO versionar)
kubectl -n aqconnecta create secret generic mariadb-secret \
    --from-literal=root-password='SENHA_ROOT_FORTE' \
    --from-literal=user-password='SENHA_APP_FORTE'

kubectl -n aqconnecta create secret generic aqconnecta-backend-secrets \
    --from-literal=MYSQL_USER='aqconnecta' \
    --from-literal=MYSQL_PASSWORD='SENHA_APP_FORTE' \
    --from-literal=JWT_SECRET="$(openssl rand -base64 64)" \
    --from-literal=JWT_EXPIRATION='604800000' \
    --from-literal=AWS_ACCESS_KEY='...' \
    --from-literal=AWS_SECRET='...' \
    --from-literal=MAIL_HOST='smtp.gmail.com' \
    --from-literal=MAIL_PORT='465' \
    --from-literal=MAIL_USERNAME='...' \
    --from-literal=MAIL_PASSWORD='...' \
    --from-literal=MAIL_SMTP_AUTH='true' \
    --from-literal=MAIL_SMTP_CONNECTION_TIMEOUT='5000' \
    --from-literal=MAIL_SMTP_TIMEOUT='5000' \
    --from-literal=MAIL_SMTP_WRITETIMEOUT='5000' \
    --from-literal=MAIL_SMTP_STARTTLS_ENABLE='true' \
    --from-literal=MAIL_SMTP_STARTTLS_REQUIRED='true' \
    --from-literal=MAIL_SMTP_SSL_PROTOCOLS='TLSv1.2'

# 2. Aplicar a infra (Kustomize)
kubectl apply -k AQConnecta-back/k8s/

# 3. Aplicar o Deployment do front
kubectl apply -k AQConnecta-front/k8s/
```

## Atualizar uma imagem

```bash
# Backend
kubectl -n aqconnecta set image deploy/aqconnecta-backend \
    backend=docker.io/riume/aqconnecta-backend:NOVA_TAG

# Frontend
kubectl -n aqconnecta set image deploy/aqconnecta-frontend \
    frontend=docker.io/riume/aqconnecta-frontend:NOVA_TAG
```

## Convenções importantes

### CORS / proxy
O frontend faz proxy interno via `BACKEND_URL` (default `http://aqconnecta-backend:8080`,
in-cluster) e remove o header `Origin`, então o backend nem precisa habilitar CORS pra
chamadas do nosso frontend. A annotation `cors-allow-origin` no ingress do backend
existe apenas para clientes externos eventuais.

### Filesystem read-only
Tanto backend quanto frontend rodam com `readOnlyRootFilesystem: true`. Os únicos
caminhos graváveis são `/tmp` (montado como `emptyDir`).

- Backend: `workingDir: /tmp` + `SPRING_SERVLET_MULTIPART_LOCATION=/tmp`
- Frontend: nginx renderiza config em `/tmp/nginx-conf.d/` e PID em `/tmp/nginx.pid`

### Banco
- `Service mysql` aponta para o `StatefulSet mariadb` (nome histórico)
- Volume persistente em `mariadb-pvc` (5Gi, `microk8s-hostpath`)

### TLS / certificados
Os certificados Let's Encrypt são gerados pelo `cert-manager` via `Issuer letsencrypt`
e ficam nos Secrets:
- `letsencrypt-nginx-cert-aqconnecta`        (front)
- `letsencrypt-nginx-cert-aqconnecta-back`   (back)

Esses Secrets **não** estão neste repo — o cert-manager cria/renova sozinho.
