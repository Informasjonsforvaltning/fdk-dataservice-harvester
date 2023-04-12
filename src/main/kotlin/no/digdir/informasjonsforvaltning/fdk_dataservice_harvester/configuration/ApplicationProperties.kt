package no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.configuration

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("application")
data class ApplicationProperties(
    val dataserviceUri: String,
    val catalogUri: String,
    val harvestAdminRootUrl: String
)
