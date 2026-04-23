#!/usr/bin/env bash
#
# PUC Express Log - Orquestracao Docker para o laboratorio RabbitMQ Topic
# Uso: bash comandos.sh
#

set -e

echo "==> [1/6] Criando a rede Docker 'rede-logistica' (bridge)"
docker network inspect rede-logistica >/dev/null 2>&1 \
  || docker network create --driver bridge rede-logistica

echo "==> [2/6] Subindo o RabbitMQ (rabbitmq:3-management) na rede"
docker rm -f broker-rabbitmq >/dev/null 2>&1 || true
docker run -d \
  --name broker-rabbitmq \
  --hostname broker-rabbitmq \
  --network rede-logistica \
  -p 5672:5672 \
  -p 15672:15672 \
  rabbitmq:3-management

echo "==> [3/6] Aguardando o RabbitMQ iniciar..."
for i in {1..30}; do
  if docker exec broker-rabbitmq rabbitmq-diagnostics -q ping >/dev/null 2>&1; then
    echo "    RabbitMQ pronto."
    break
  fi
  echo "    ... ($i/30)"
  sleep 2
done

echo "==> [4/6] Build das imagens Docker"
docker build -t puc-express-log/produtor:1.0 ./produtor
docker build -t puc-express-log/consumidor:1.0 ./consumidor

echo "==> [5/6] Subindo o Consumidor"
docker rm -f consumidor >/dev/null 2>&1 || true
docker run -d \
  --name consumidor \
  --network rede-logistica \
  puc-express-log/consumidor:1.0

sleep 3

echo "==> [6/6] Subindo o Produtor"
docker rm -f produtor >/dev/null 2>&1 || true
docker run -d \
  --name produtor \
  --network rede-logistica \
  puc-express-log/produtor:1.0

echo ""
echo "Ambiente no ar!"
echo "  - RabbitMQ Management UI : http://localhost:15672  (user/pass: guest/guest)"
echo "  - Logs do produtor       : docker logs -f produtor"
echo "  - Logs do consumidor     : docker logs -f consumidor"
