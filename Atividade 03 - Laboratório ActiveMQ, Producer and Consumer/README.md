# Atividade 03 — Laboratório ActiveMQ: Producer and Consumer

## Descrição

Laboratório prático de mensageria utilizando **Apache ActiveMQ** com o padrão **Producer / Consumer** via protocolo JMS (Java Message Service).  
O sistema simula um fluxo de pedidos de venda: um producer gera pedidos em formato JSON e os publica em uma fila; um consumer escuta a fila, recebe os pedidos e os persiste em arquivo.

---

## Arquitetura

```
┌──────────────────┐        fila: venda.pedido        ┌──────────────────────┐
│   Gerar Pedido   │  ──────────────────────────────▶  │  Processar Pedido    │
│  (Producer/JMS)  │        Apache ActiveMQ             │  (Consumer/JMS)      │
└──────────────────┘                                   └──────────────────────┘
                                                                  │
                                                                  ▼
                                                       pedidos_recebidos.txt
```

---

## Projetos

### 1. Gerar Pedido (Producer)

| Item | Detalhe |
|---|---|
| Artifact | `GerarPedido` |
| Classe principal | `br.com.devigode.msg.ProduzirPedidos` |
| Fila destino | `venda.pedido` |
| Intervalo de envio | 30 segundos |
| Formato da mensagem | JSON (via Gson) |

**Exemplo de mensagem publicada:**
```json
{"numero": 264, "valor": 100.0, "data": "Wed Mar 11 21:20:35 GMT-03:00 2026"}
```

#### Dependências (pom.xml)

| Dependência | Versão |
|---|---|
| `activemq-client` | 5.18.3 |
| `gson` | 2.10.1 |
| `slf4j-simple` | 2.0.9 |

---

### 2. Processar Pedido (Consumer)

| Item | Detalhe |
|---|---|
| Artifact | `ProcessarPedido` |
| Classe principal | `br.com.devigode.msg.ConsumerPedidos` |
| Fila consumida | `venda.pedido` |
| Saída | `pedidos_recebidos.txt` (append) |

O consumer fica em escuta contínua (`consumer.receive()`) e, ao receber uma mensagem do tipo `TextMessage`, grava o conteúdo no arquivo `pedidos_recebidos.txt`.

---

## Pré-requisitos

- **Java 17+** (projeto compilado com source/target 25)
- **Apache Maven 3.8+**
- **Apache ActiveMQ 5.x** em execução local

### Subindo o ActiveMQ localmente

1. Baixe em: https://activemq.apache.org/components/classic/download/
2. Extraia e execute:
   ```bash
   # Linux/macOS
   bin/activemq start

   # Windows
   bin\activemq start
   ```
3. Acesse o console de administração: http://localhost:8161  
   Credenciais padrão: `admin` / `admin`
4. O broker JMS estará disponível em `tcp://localhost:61616`

---

## Como executar

> Execute cada projeto em um terminal separado.

### 1. Compilar os projetos

```bash
# Producer
cd "Gerar Pedido"
mvn clean package

# Consumer
cd "Processar Pedido"
mvn clean package
```

### 2. Iniciar o Consumer primeiro

```bash
cd "Processar Pedido"
mvn exec:java -Dexec.mainClass="br.com.devigode.msg.ConsumerPedidos"
```

Aguarde a mensagem:
```
Consumidor pronto. Aguardando mensagens...
```

### 3. Iniciar o Producer

```bash
cd "Gerar Pedido"
mvn exec:java -Dexec.mainClass="br.com.devigode.msg.ProduzirPedidos"
```

Aguarde a mensagem:
```
Gerador iniciado. Enviando a cada 30s...
```

### 4. Verificar os pedidos recebidos

```bash
cat "Processar Pedido/pedidos_recebidos.txt"
```

---

## Estrutura de Pastas

```
Atividade 03 - Laboratório ActiveMQ, Producer and Consumer/
├── Gerar Pedido/
│   ├── pom.xml
│   └── src/main/java/br/com/devigode/
│       └── msg/ProduzirPedidos.java       ← Producer JMS
└── Processar Pedido/
    ├── pom.xml
    ├── pedidos_recebidos.txt              ← saída gravada pelo consumer
    └── src/main/java/br/com/devigode/
        └── msg/ConsumerPedidos.java       ← Consumer JMS
```

---

## Conceitos Abordados

- **JMS (Java Message Service)** — API padrão para troca de mensagens em Java
- **Apache ActiveMQ** — broker de mensagens open-source
- **Padrão Producer / Consumer** — desacoplamento entre quem produz e quem consome dados
- **Filas (Queue)** — entrega garantida ponto-a-ponto (`venda.pedido`)
- **Serialização JSON** — uso da biblioteca Gson para converter objetos em mensagens de texto
