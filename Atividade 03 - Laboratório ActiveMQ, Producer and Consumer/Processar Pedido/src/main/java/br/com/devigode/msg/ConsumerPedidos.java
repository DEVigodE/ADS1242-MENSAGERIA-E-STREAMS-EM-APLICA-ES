package br.com.devigode.msg;

import org.apache.activemq.ActiveMQConnectionFactory;
import javax.jms.*;
import java.io.*;

public class ConsumerPedidos {

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

        MessageConsumer consumer =
                session.createConsumer(destination);

        System.out.println("Consumidor pronto. Aguardando mensagens...");

        while (true) {

            Message msg = consumer.receive();

            if (msg instanceof TextMessage) {

                String texto = ((TextMessage) msg).getText();

                // Grava no arquivo na pasta do seu projeto
                try (PrintWriter out = new PrintWriter(
                        new BufferedWriter(
                                new FileWriter("pedidos_recebidos.txt", true)))) {

                    out.println(texto);
                }

                System.out.println("Pedido salvo no arquivo: " + texto);
            }
        }
    }
}