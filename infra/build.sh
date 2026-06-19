#!/bin/bash

set -euo pipefail # "Wenn ein Befehl fehlschlägt, beende das Skript sofort." 

docker build --platform linux/amd64 \
  -t backend-ai-deskhelp:latest \
  -f ../backend/backend-docker/Dockerfile ..

docker build --platform linux/amd64 \
  -t frontend-ai-deskhelp:latest \
  -f ../ai-deskhelp-frontend/frontend-Docker-file/Dockerfile ..



VERSION="${VERSION:-v2}"

docker tag backend-ai-deskhelp:latest ghcr.io/rashids10/backend-ai-deskhelp:$VERSION
docker push ghcr.io/rashids10/backend-ai-deskhelp:$VERSION

docker tag frontend-ai-deskhelp:latest ghcr.io/rashids10/frontend-ai-deskhelp:$VERSION
docker push ghcr.io/rashids10/frontend-ai-deskhelp:$VERSION

## dann in deploeymentes den richtigen Namen einfügen


SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)" #Dirname : dirname = Entferne den Dateinamen und gib nur den Ordner zurück

# oc apply -f "$SCRIPT_DIR/openshift/configmap.yaml"
# oc apply -f "$SCRIPT_DIR/openshift/secret.yaml"

oc apply -f "$SCRIPT_DIR/openshift/db-deployment.yaml" # was ausgeführt wird /Users/abdirashid.farah/work/ai-helpdesk/infra/openshift/db-deployment.yaml
oc apply -f "$SCRIPT_DIR/openshift/backend-deployment.yaml"
oc apply -f "$SCRIPT_DIR/openshift/frontend-deployment.yaml"
oc apply -f "$SCRIPT_DIR/openshift/ollama-deyployment.yaml"
oc apply -f "$SCRIPT_DIR/openshift/route.yaml"
