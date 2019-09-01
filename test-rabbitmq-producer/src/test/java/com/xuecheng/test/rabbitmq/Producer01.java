package com.xuecheng.test.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class Producer01 {

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

        Connection connection = null;
        Channel channel = null;

        try {
            // 建立新连接
            connection = connectionFactory.newConnection();
            // 创建会话通道，生产者和mq服务所有通信都在channel中完成
            channel = connection.createChannel();
            // 声明队列
            channel.queueDeclare(QUEUE,true,false,false,null);
            // 发送消息
            // 消息内容
            String messqge = "hello 蜗牛";
            channel.basicPublish("",QUEUE,null,messqge.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                channel.close();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (TimeoutException e) {
                e.printStackTrace();
            }
            try {
                connection.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
