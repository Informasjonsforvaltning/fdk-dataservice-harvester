package no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown=true)
data class RabbitHarvestTrigger(
    val dataSourceId: String? = null,
    val publisherId: String? = null,
    val dataSourceType: String? = null,
    val forceUpdate: Boolean = false
)
