import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.BuiltinExchangeType;

import java.nio.charset.StandardCharsets;

public class Produtor {

    private static final String EXCHANGE = "ex.logistica";
    private static final String HOST = "broker-rabbitmq";
    private static final int PORT = 5672;

    public static void main(String[] args) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(HOST);
        factory.setPort(PORT);
        factory.setUsername("guest");
        factory.setPassword("guest");

        // aguarda o broker ficar pronto
        Connection connection = connectWithRetry(factory);

        try (Channel channel = connection.createChannel()) {

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

            String[] tipos = {"expresso", "economico"};
            String[] regioes = {"norte", "sul", "leste", "oeste"};

            int id = 1;
            for (String tipo : tipos) {
                for (String regiao : regioes) {
                    String routingKey = tipo + "." + regiao;
                    String mensagem = String.format(
                            "Encomenda #%03d | frete=%s | destino=%s",
                            id++, tipo, regiao);

                    channel.basicPublish(
                            EXCHANGE,
                            routingKey,
                            null,
                            mensagem.getBytes(StandardCharsets.UTF_8));

                    System.out.printf("[PRODUTOR] RK=%-18s -> %s%n", routingKey, mensagem);
                }
            }

            System.out.println("[PRODUTOR] Todas as mensagens foram publicadas.");
        } finally {
            connection.close();
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
                System.out.println("[PRODUTOR] Aguardando RabbitMQ... tentativa " + tentativas);
                Thread.sleep(2000);
            }
        }
    }
}
