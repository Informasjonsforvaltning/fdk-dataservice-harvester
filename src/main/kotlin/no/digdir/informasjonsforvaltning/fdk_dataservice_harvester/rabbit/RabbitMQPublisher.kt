package no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rabbit

import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.model.HarvestReport
import org.slf4j.LoggerFactory
import org.springframework.amqp.AmqpException
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Service

private val LOGGER = LoggerFactory.getLogger(RabbitMQPublisher::class.java)

@Service
class RabbitMQPublisher(private val template: RabbitTemplate) {
    fun send(reports: List<HarvestReport>) {
        try {
            template.convertAndSend("harvests","dataservices.harvested", reports)
            LOGGER.debug("Successfully sent harvest completed message")
        } catch (e: AmqpException) {
            LOGGER.error("Unable to send harvest completed message", e)
        }
    }
}
