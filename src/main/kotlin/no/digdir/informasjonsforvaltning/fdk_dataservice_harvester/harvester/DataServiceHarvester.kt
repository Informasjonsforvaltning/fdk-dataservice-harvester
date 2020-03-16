package no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.harvester

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.adapter.DataServiceAdapter
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.configuration.ApplicationProperties
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.dto.HarvestDataSource
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.fuseki.CatalogFuseki
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.fuseki.DataServiceFuseki
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf.JenaType
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf.createDataserviceModel
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf.createIdFromUri
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf.createModelOfTopLevelProperties
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf.extractMetaDataIdentifier
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf.listOfCatalogResources
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf.listOfDataServiceResources
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf.parseRDFResponse
import org.apache.jena.rdf.model.Literal
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.rdf.model.Resource
import org.apache.jena.sparql.vocabulary.FOAF
import org.apache.jena.vocabulary.DCAT
import org.apache.jena.vocabulary.DCTerms
import org.apache.jena.vocabulary.RDF
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.*

private val LOGGER = LoggerFactory.getLogger(DataServiceHarvester::class.java)

@Service
class DataServiceHarvester(
    private val adapter: DataServiceAdapter,
    private val dataServiceFuseki: DataServiceFuseki,
    private val catalogFuseki: CatalogFuseki,
    private val applicationProperties: ApplicationProperties
) {

    fun initiateHarvest(sources: List<HarvestDataSource>) {
        sources.forEach {
            if (it.url != null) {
                GlobalScope.launch { harvestDataServiceCatalog(it.url, Calendar.getInstance()) }
            }
        }
    }

    fun harvestDataServiceCatalog(url: String, harvestDate: Calendar) {
        adapter.getDataServiceCatalog(url)
            ?.let { parseRDFResponse(it, JenaType.TURTLE) }
            ?.filterModifiedAndAddMetaData(harvestDate)
            ?.run {
                catalogs.forEach{ catalogFuseki.saveWithGraphName(it.extractMetaDataIdentifier(), it) }
                dataServices.forEach{ dataServiceFuseki.saveWithGraphName(it.extractMetaDataIdentifier(), it) }
            }
    }

    private fun catalogModelWithMetaData(resource: Resource, harvested: Model, harvestDate: Calendar): HarvestedModel {
        val dbId = createIdFromUri(resource.uri)
        val dbModel = catalogFuseki.fetchByGraphName(dbId)

        val dbMetaData: Resource? = dbModel?.extractMetaDataResource()

        val isModified = !harvestedIsIsomorphicWithDatabaseModel(dbModel, harvested, dbMetaData?.createModelOfTopLevelProperties())

        val updatedModel = if (!isModified && dbModel != null) dbModel
        else harvested.addCatalogMetaData(dbId, resource.uri, dbMetaData, harvestDate)

        return HarvestedModel(updatedModel, isModified)
    }

    private fun dataServiceWithMetaData(resource: Resource, harvested: Model, harvestDate: Calendar): HarvestedModel {
        val dbId = createIdFromUri(resource.uri)
        val dbModel = dataServiceFuseki.fetchByGraphName(dbId)

        val dbMetaData: Resource? = dbModel?.extractMetaDataResource()

        val isModified = !harvestedIsIsomorphicWithDatabaseModel(dbModel, harvested, dbMetaData?.createModelOfTopLevelProperties())

        val updatedModel = if (!isModified && dbModel != null) dbModel
        else harvested.addDataServiceMetaData(dbId, resource.uri, dbMetaData, harvestDate)

        return HarvestedModel(updatedModel, isModified)
    }

    private fun Model.addCatalogMetaData(dbId: String, uri: String, dbMetaData: Resource?, harvestDate: Calendar): Model {
        createResource("${applicationProperties.catalogUri}/$dbId")
            .addProperty(RDF.type, DCAT.record)
            .addProperty(DCTerms.identifier, dbId)
            .addProperty(FOAF.primaryTopic, createResource(uri))
            .addProperty(DCTerms.issued, issuedDate(dbMetaData, harvestDate))
            .addModified(dbMetaData, harvestDate)
        return this
    }

    private fun Model.addDataServiceMetaData(dbId: String, uri: String, dbMetaData: Resource?, harvestDate: Calendar): Model {
        createResource("${applicationProperties.dataserviceUri}/$dbId")
            .addProperty(RDF.type, DCAT.record)
            .addProperty(DCTerms.identifier, dbId)
            .addProperty(FOAF.primaryTopic, createResource(uri))
            .addProperty(DCTerms.issued,  issuedDate(dbMetaData, harvestDate))
            .addModified(dbMetaData, harvestDate)

        return this
    }

    private fun Model.filterModifiedAndAddMetaData(harvestDate: Calendar): ModifiedModels {
        val catalogModels = mutableListOf<HarvestedModel>()
        val dataServiceModels = mutableListOf<HarvestedModel>()

        listOfCatalogResources().forEach {
            val catalogModel = it.createModelOfTopLevelProperties()
            val modifiedModel = catalogModelWithMetaData(it, catalogModel, harvestDate)
            catalogModels.add(modifiedModel)
        }

        listOfDataServiceResources().forEach {
            val dataServiceModel = it.createDataserviceModel()
            val modifiedModel = dataServiceWithMetaData(it, dataServiceModel, harvestDate)
            dataServiceModels.add(modifiedModel)
        }

        return ModifiedModels(
            catalogModels
                .toList()
                .filter { it.isModified }
                .map { it.model },
            dataServiceModels
                .toList()
                .filter { it.isModified }
                .map { it.model }
        )
    }

}

private data class HarvestedModel(
    val model: Model,
    val isModified: Boolean
)

private data class ModifiedModels(
    val catalogs: List<Model>,
    val dataServices: List<Model>
)

private fun Model.extractMetaDataResource(): Resource? =
    listResourcesWithProperty(RDF.type, DCAT.record)
        .toList()
        .let { if (it.isNotEmpty()) it.first() else null }

private fun harvestedIsIsomorphicWithDatabaseModel(fullModelFromDB: Model?, harvestedModel: Model, metaDataModelFromDB: Model?): Boolean =
    fullModelFromDB?.isIsomorphicWith(
        harvestedModel.union(
            metaDataModelFromDB ?: ModelFactory.createDefaultModel()
        )
    ) ?: false

private fun Resource.extractMetaDataFirstHarvested(): String? =
    getProperty(DCTerms.issued)
        ?.string

private fun Model.issuedDate(dbResource: Resource?, harvestDate: Calendar): Literal =
    dbResource?.listProperties(DCTerms.issued)
        ?.toList()
        ?.firstOrNull()
        ?.literal
        ?: createTypedLiteral(harvestDate)

private fun Resource.addModified(dbResource: Resource?, harvestDate: Calendar) {
    addProperty(DCTerms.modified, model.createTypedLiteral(harvestDate))

    dbResource?.listProperties(DCTerms.modified)
        ?.toList()
        ?.forEach { addProperty(DCTerms.modified, it.string) }
}
