package no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.model

import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.generated.model.Dataservice
import java.net.URI
import java.util.*

data class DataserviceDB (
    val id: String,
    val dataservice: Dataservice,
    val metadataId: String,
    val catalogId: String
)

data class CatalogDB (
    val id: String,
    val catalog: HarvestedCatalog,
    val metadataId: String
)

data class HarvestedCatalog (
    val id: String,
    val publisherUrl: URI,
    val title: String,
    val description: String,
    val dataservices: List<String>
)

data class HarvestMetadata (
    val id: String,
    val firstHarvested: Date,
    val lastHarvested: Date,
    val lastChanged: Date,
    val changed: List<Date>
)