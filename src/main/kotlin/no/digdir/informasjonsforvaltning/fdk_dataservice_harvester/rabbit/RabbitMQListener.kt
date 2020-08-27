package no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rabbit

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.harvester.HarvesterActivity
import org.slf4j.LoggerFactory
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap

private val logger = LoggerFactory.getLogger(RabbitMQListener::class.java)
private val ALLOWED_FIELDS = listOf("publisherId", "dataType")

@Service
class RabbitMQListener(
    private val objectMapper: ObjectMapper,
    private val harvesterActivity: HarvesterActivity
) {

    private fun createQueryParams(body: JsonNode?): MultiValueMap<String, String> {
        val params = LinkedMultiValueMap<String, String>()
        val fields = objectMapper.convertValue(body, object : TypeReference<Map<String, String>>() {})
        params.setAll( fields.filter { ALLOWED_FIELDS.contains(it.key) } )
        return params
    }

    @RabbitListener(queues = ["#{receiverQueue.name}"])
    fun receiveDataServiceHarvestTrigger(@Payload body: JsonNode?, message: Message) {
        val routingKey = message.extractRoutingKey()
        logger.info("Received message from key: $routingKey")

        // convert from map to multivaluemap for UriComponentBuilder
        val params: MultiValueMap<String, String> = createQueryParams(body)

        harvesterActivity.initiateHarvest(params)
    }

}

private fun Message.extractRoutingKey(): String =
    messageProperties.receivedRoutingKey.split("\\.".toRegex()).toTypedArray()[0]
