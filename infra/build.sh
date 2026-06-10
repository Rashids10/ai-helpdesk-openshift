#!/bin/bash
eval $(minikube -p minikube docker-env)
set -euo pipefail # "Wenn ein Befehl fehlschlägt, beende das Skript sofort." 

docker build \
  -t backend-ai-deskhelp:latest \
  -f ../backend/backend-docker/Dockerfile ..

docker build \
  -t frontend-ai-deskhelp:latest \
  -f ../ai-deskhelp-frontend/frontend-Docker-file/Dockerfile ..

# minikube addons enable ingress : nur einmalig notwendig, damit Ingress Controller installiert wird

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)" #Dirname : dirname = Entferne den Dateinamen und gib nur den Ordner zurück

kubectl apply -f "$SCRIPT_DIR/k8s/db-deployment.yaml" # was ausgeführt wird /Users/abdirashid.farah/work/ai-helpdesk/infra/k8s/db-deployment.yaml
kubectl apply -f "$SCRIPT_DIR/k8s/frontend-deployment.yaml"
kubectl apply -f "$SCRIPT_DIR/k8s/backend-deployment.yaml"
kubectl apply -f "$SCRIPT_DIR/k8s/web-ingress.yaml"
