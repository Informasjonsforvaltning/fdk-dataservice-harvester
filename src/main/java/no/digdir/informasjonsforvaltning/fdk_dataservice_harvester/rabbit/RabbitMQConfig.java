package no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rabbit;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.adapter.HarvestAdminAdapter;
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.harvester.DataServiceHarvester;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    private final HarvestAdminAdapter harvestAdminAdapter;
    private final ObjectMapper objectMapper;
    private final DataServiceHarvester harvester;

    @Autowired
    public RabbitMQConfig(HarvestAdminAdapter harvestAdminAdapter, ObjectMapper objectMapper, DataServiceHarvester harvester) {
        this.harvestAdminAdapter = harvestAdminAdapter;
        this.objectMapper = objectMapper;
        this.harvester = harvester;
    }

    @Bean
    public RabbitMQListener receiver() {
        return new RabbitMQListener(harvestAdminAdapter, objectMapper, harvester);
    }

    @Bean
    public Queue queue() {
        return new AnonymousQueue();
    }

    @Bean
    public Jackson2JsonMessageConverter converter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange("harvests", false, false);
    }

    @Bean
    public Binding binding(TopicExchange topicExchange, Queue queue) {
        return BindingBuilder.bind(queue).to(topicExchange).with("dataservice.*.HarvestTrigger");
    }
}
