package no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.service

import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.fuseki.DataServiceFuseki
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf.HarvestMetaData
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf.listOfCatalogResources
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf.listOfDataServiceResources
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.Resource
import org.apache.jena.vocabulary.DCTerms
import org.springframework.stereotype.Service
import java.util.*

@Service
class MetaDataService(private val dataServiceFuseki: DataServiceFuseki) {

    fun addMetaDataToModel(model: Model): Model {
        model.setNsPrefix("meta", HarvestMetaData.uri)

        model.listOfCatalogResources().forEach {
            model.add(it.modelWithMetaData())
        }

        model.listOfDataServiceResources().forEach {
            model.add(it.modelWithMetaData())
        }

        return model
    }

    private fun Resource.modelWithMetaData(): Model {
        addProperty(
            HarvestMetaData.metaData,
            model.createResource(HarvestMetaData.HarvestMetaData)
                .addProperty(DCTerms.identifier, uri.createIdFromUri()))

        return model
    }

    private fun String.createIdFromUri(): String =
        UUID.nameUUIDFromBytes(toByteArray())
            .toString()
}