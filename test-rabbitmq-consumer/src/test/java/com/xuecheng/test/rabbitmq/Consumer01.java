package com.xuecheng.test.rabbitmq;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class Consumer01 {
    // 队列
    private static final String QUEUE = "helloworld";

    public static void main(String[] args) {
        // 创建连接.
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("127.0.0.1");
        connectionFactory.setPort(5672);
        connectionFactory.setUsername("guest");
        connectionFactory.setPassword("guest");
        // 设置虚拟机，每个mq服务器可以设置多个虚拟机
        connectionFactory.setVirtualHost("/");

        // 建立新连接
        Connection connection = null;

        try {
            connection = connectionFactory.newConnection();
            // 创建会话通道，生产者和mq服务所有通信都在channel中完成
            Channel channel = connection.createChannel();
            // 声明队列
            channel.queueDeclare(QUEUE,true,false,false,null);
            // 定义消费方法
            DefaultConsumer defaultConsumer = new DefaultConsumer(channel){
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    String exchange = envelope.getExchange();
                    long deliveryTag = envelope.getDeliveryTag();
                    String message = new String(body, "utf-8");
                    System.out.println("receive message.." + message);
                }
            };

            channel.basicConsume(QUEUE, true, defaultConsumer);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }


    }
}
