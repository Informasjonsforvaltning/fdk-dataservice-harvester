package no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.model

import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.generated.model.Dataservice
import org.springframework.data.annotation.Id
import org.springframework.data.elasticsearch.annotations.Document
import java.net.URI
import java.util.*

@Document(indexName = "dataservices", type = "dataservice")
data class DataserviceDB (
    @Id val id: String,
    val dataservice: Dataservice,
    val metadataId: String,
    val catalogId: String
)

@Document(indexName = "dataservice_catalogs", type = "dataservice_catalog")
data class CatalogDB (
    @Id val id: String,
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

@Document(indexName = "dataservice_metadata", type = "metadata")
data class HarvestMetadata (
    @Id val id: String,
    val firstHarvested: Date,
    val lastHarvested: Date,
    val lastChanged: Date,
    val changed: List<Date>
)