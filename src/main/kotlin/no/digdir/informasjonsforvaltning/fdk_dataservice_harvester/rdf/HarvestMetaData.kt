package no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf

import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.rdf.model.Property
import org.apache.jena.rdf.model.Resource

class HarvestMetaData {
    companion object {
        private val m = ModelFactory.createDefaultModel()

        const val uri = "http://dcat.difi.no/metadata/"

        val HarvestMetaData: Resource = m.createResource(uri + "HarvestMetaData")
        val metaData: Property = m.createProperty(uri + "metaData")
        val firstHarvested: Property = m.createProperty(uri + "firstHarvested")
        val lastHarvested: Property = m.createProperty(uri + "lastHarvested")
        val lastChanged: Property = m.createProperty(uri + "lastChanged")
        val changed: Property = m.createProperty(uri + "changed")
    }
}