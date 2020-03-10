package no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.fuseki

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties("fuseki")
data class FusekiProperties(
    val dataserviceUri: String,
    val catalogUri: String
)
