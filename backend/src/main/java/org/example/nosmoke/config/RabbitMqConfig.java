package org.example.nosmoke.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;



@Configuration
public class RabbitMqConfig {

    public static final String EXCHANGE_NAME = "monkey.exchange";
    public static final String QUEUE_NAME = "monkey.ai.queue";
    public static final String ROUTING_KEY = "monkey.ai.request";

    // 큐 생성 (AI 요청을 쌓아 둘 버퍼)
    @Bean
    public Queue aiQueue(){
        return new Queue(QUEUE_NAME, true);
    }

    // 익스체인지 생성
    @Bean
    public TopicExchange exchange(){
        return new TopicExchange(EXCHANGE_NAME);
    }

    // 큐와 익스체인지 바인딩
    @Bean
    public Binding binding(Queue queue, TopicExchange exchange){
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY);
    }

    // 메시지 컨버터(Java 객체 -> JSON 변환)
    @Bean
    public MessageConverter converter(){
        return new Jackson2JsonMessageConverter();
    }

    // RabbitTemplate 설정
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory){
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter());
        return template;
    }




}
