package br.com.devigode.msg;

import org.apache.activemq.ActiveMQConnectionFactory;
import com.google.gson.Gson;
import javax.jms.*;
import java.util.*;

public class ProduzirPedidos {
    public static void main(String[] args) throws Exception {
        String brokerUrl = "tcp://localhost:61616";

        ActiveMQConnectionFactory factory =
                new ActiveMQConnectionFactory(brokerUrl);

        Connection connection = factory.createConnection();
        connection.start();

        Session session = connection.createSession(false,
                Session.AUTO_ACKNOWLEDGE);

        Destination destination =
                session.createQueue("venda.pedido");

        MessageProducer producer =
                session.createProducer(destination);

        Gson gson = new Gson();
        System.out.println("Gerador iniciado. Enviando a cada 30s...");

        while (true) {
            Map<String, Object> pedido = Map.of(
                    "data", new Date().toString(),
                    "numero", new Random().nextInt(1000),
                    "valor", 100.0
            );

            String json = gson.toJson(pedido);

            producer.send(session.createTextMessage(json));

            System.out.println("Enviado para ActiveMQ: " + json);

            Thread.sleep(30000);
        }
    }
}
