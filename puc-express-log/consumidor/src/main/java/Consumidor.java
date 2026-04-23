import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.BuiltinExchangeType;

import java.nio.charset.StandardCharsets;

public class Consumidor {

    private static final String EXCHANGE = "ex.logistica";
    private static final String HOST = "broker-rabbitmq";
    private static final int PORT = 5672;

    private static final String[] FILAS = {
            "q.prioridade",
            "q.regional.norte",
            "q.regional.sul",
            "q.regional.leste",
            "q.regional.oeste",
            "q.auditoria"
    };

    public static void main(String[] args) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(HOST);
        factory.setPort(PORT);
        factory.setUsername("guest");
        factory.setPassword("guest");

        Connection connection = connectWithRetry(factory);
        Channel channel = connection.createChannel();

        channel.exchangeDeclare(EXCHANGE, BuiltinExchangeType.TOPIC, true);

        channel.queueDeclare("q.prioridade", true, false, false, null);
        channel.queueDeclare("q.regional.norte", true, false, false, null);
        channel.queueDeclare("q.regional.sul", true, false, false, null);
        channel.queueDeclare("q.regional.leste", true, false, false, null);
        channel.queueDeclare("q.regional.oeste", true, false, false, null);
        channel.queueDeclare("q.auditoria", true, false, false, null);

        channel.queueBind("q.prioridade", EXCHANGE, "expresso.#");
        channel.queueBind("q.regional.norte", EXCHANGE, "*.norte");
        channel.queueBind("q.regional.sul", EXCHANGE, "*.sul");
        channel.queueBind("q.regional.leste", EXCHANGE, "*.leste");
        channel.queueBind("q.regional.oeste", EXCHANGE, "*.oeste");
        channel.queueBind("q.auditoria", EXCHANGE, "#");

        System.out.println("[CONSUMIDOR] Aguardando mensagens em todas as filas...");

        for (String fila : FILAS) {
            DeliverCallback callback = (consumerTag, delivery) -> {
                String msg = new String(delivery.getBody(), StandardCharsets.UTF_8);
                String rk = delivery.getEnvelope().getRoutingKey();
                System.out.printf("[CONSUMIDOR] fila=%-18s rk=%-18s msg=%s%n", fila, rk, msg);
            };
            channel.basicConsume(fila, true, callback, consumerTag -> {});
        }
    }

    private static Connection connectWithRetry(ConnectionFactory factory) throws Exception {
        int tentativas = 0;
        while (true) {
            try {
                return factory.newConnection();
            } catch (Exception e) {
                tentativas++;
                if (tentativas > 30) throw e;
                System.out.println("[CONSUMIDOR] Aguardando RabbitMQ... tentativa " + tentativas);
                Thread.sleep(2000);
            }
        }
    }
}
