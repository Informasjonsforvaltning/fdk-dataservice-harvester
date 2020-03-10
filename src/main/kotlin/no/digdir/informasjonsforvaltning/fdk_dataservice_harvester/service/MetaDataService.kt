package no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.service

import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.fuseki.CatalogFuseki
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.fuseki.DataServiceFuseki
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf.HarvestMetaData
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf.listOfCatalogResources
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf.listOfDataServiceResources
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.Resource
import org.apache.jena.vocabulary.DCTerms
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.util.*

@Service
class MetaDataService(
    private val dataServiceFuseki: DataServiceFuseki,
    private val catalogFuseki: CatalogFuseki
) {

    fun addMetaDataToModel(model: Model): Model {

        model.listOfCatalogResources().forEach {
            val dbId = it.uri.createIdFromUri()
            it.addProperty(
                HarvestMetaData.fdkMetaData,
                model.addMetaData(fetchCatalogMetaData(dbId), dbId))
        }

        model.listOfDataServiceResources().forEach {
            val dbId = it.uri.createIdFromUri()
            it.addProperty(
                HarvestMetaData.fdkMetaData,
                model.addMetaData(fetchDataServiceMetaData(dbId), dbId))
        }

        return model
    }

    private fun fetchCatalogMetaData(dbId: String): Resource? =
        catalogFuseki.fetchByGraphName(dbId)
            ?.listResourcesWithProperty(HarvestMetaData.fdkMetaData)
            ?.toList()
            ?.let { if (it.size == 1) it.first() else null }

    private fun fetchDataServiceMetaData(dbId: String): Resource? =
        dataServiceFuseki.fetchByGraphName(dbId)
            ?.listResourcesWithProperty(HarvestMetaData.fdkMetaData)
            ?.toList()
            ?.let { if (it.size == 1) it.first() else null }
}

private fun Model.addMetaData(dbResource: Resource?, dbId: String): Resource {
    val nowDateString: String = LocalDate.now().toString()

    return if (dbResource == null) {
        createResource(HarvestMetaData.HarvestMetaData)
            .addProperty(DCTerms.identifier, dbId)
            .addProperty(HarvestMetaData.lastHarvested, nowDateString)
            .addProperty(HarvestMetaData.lastChanged, nowDateString)
            .addProperty(HarvestMetaData.changed, nowDateString)
            .addProperty(HarvestMetaData.firstHarvested, nowDateString)
    } else {
        createResource(HarvestMetaData.HarvestMetaData)
            .addProperty(DCTerms.identifier, dbId)
            .addProperty(HarvestMetaData.lastHarvested, nowDateString)
            .addProperty(HarvestMetaData.lastChanged, dbResource.extractMetaDataLastChanged() ?: nowDateString)
            .addProperty(HarvestMetaData.firstHarvested, dbResource.extractMetaDataFirstHarvested() ?: nowDateString)
            .addChanged(dbResource)
    }
}

private fun String.createIdFromUri(): String =
    UUID.nameUUIDFromBytes(toByteArray())
        .toString()

private fun Resource.extractMetaDataLastChanged(): String? =
    getProperty(HarvestMetaData.lastChanged)
        ?.string

private fun Resource.extractMetaDataFirstHarvested(): String? =
    getProperty(HarvestMetaData.firstHarvested)
        ?.string

private fun Resource.addChanged(dbResource: Resource): Resource {
    dbResource.listProperties(HarvestMetaData.changed)
        .toList()
        .let { list ->
            if (list.size > 0) list.forEach { addProperty(HarvestMetaData.changed, it.string) }
            else addProperty(HarvestMetaData.changed, LocalDate.now().toString())
        }

    return this
}
