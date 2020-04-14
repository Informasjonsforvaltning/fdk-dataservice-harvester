package no.acat.configuration;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.core.*;

@Configuration
public class RabbitMQConfigurer {

    @Value("${spring.rabbitmq.template.exchange}")
    private String exchange;

    @Bean
    public Queue sendQueue() {
        return new Queue("harvester.UpdateSearchTrigger", false);
    }

    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange(exchange, false, false);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public Binding sendBinding(TopicExchange topicExchange, Queue sendQueue) {
        return BindingBuilder.bind(sendQueue).to(topicExchange).with("harvester.UpdateSearchTrigger");
    }

    @Bean
    public AmqpTemplate jsonRabbitTemplate(ConnectionFactory connectionFactory) {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}
