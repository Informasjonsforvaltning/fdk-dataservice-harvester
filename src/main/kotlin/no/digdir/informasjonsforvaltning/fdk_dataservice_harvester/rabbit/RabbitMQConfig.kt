package no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rabbit

import com.fasterxml.jackson.databind.ObjectMapper
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.adapter.HarvestAdminAdapter
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.harvester.DataServiceHarvester
import org.springframework.amqp.core.AnonymousQueue
import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.Queue
import org.springframework.amqp.core.TopicExchange
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class RabbitMQConfig (
    private val harvestAdminAdapter: HarvestAdminAdapter,
    private val objectMapper: ObjectMapper,
    private val harvester: DataServiceHarvester
) {
    @Bean
    open fun receiver(): RabbitMQListener = RabbitMQListener(harvestAdminAdapter, objectMapper, harvester)

    @Bean
    open fun queue(): Queue = AnonymousQueue()

    @Bean
    open fun converter(): Jackson2JsonMessageConverter = Jackson2JsonMessageConverter()

    @Bean
    open fun topicExchange(): TopicExchange = TopicExchange("harvests", false, false)

    @Bean
    open fun binding(topicExchange: TopicExchange?, queue: Queue?): Binding =
        BindingBuilder.bind(queue).to(topicExchange).with("dataservice.*.HarvestTrigger")
}