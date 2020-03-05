package no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf

import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.generated.model.Catalog
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.generated.model.Contact
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.generated.model.Dataservice
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.rdf.model.Property
import org.apache.jena.rdf.model.Resource
import org.apache.jena.rdf.model.Statement
import org.apache.jena.vocabulary.DCAT
import org.apache.jena.vocabulary.DCTerms
import org.apache.jena.vocabulary.RDF
import org.apache.jena.vocabulary.VCARD4
import java.io.StringReader
import java.net.URI

enum class JenaType(val value: String){
    TURTLE("TURTLE")
}

fun parseRDFResponse(responseBody: String, rdfLanguage: JenaType): Model {
    val responseModel = ModelFactory.createDefaultModel()
    responseModel.read(StringReader(responseBody), "", rdfLanguage.value)
    return responseModel
}

fun Model.parseCatalog(): Catalog? {
    val catalogResources = this.listResourcesWithProperty(RDF.type, DCAT.Catalog)

    return if (catalogResources.hasNext()) {
        catalogResources
            .nextResource()
            .let {
                Catalog().apply {
                    id = it.uri
                    publisherUrl = it.extractPropertyURI(DCTerms.publisher)
                    title = it.extractProperty(DCTerms.title)?.string
                    description = it.extractProperty(DCTerms.description)?.string
                    dataservices = it.extractDataservices()
                }
            }
    } else { null }
}

private fun Resource.extractProperty(property: Property) : Statement? =
    if (this.hasProperty(property)) this.getProperty(property)
    else null

private fun Resource.extractPropertyURI(property: Property) : URI? =
    if (this.hasProperty(property)) URI(this.getProperty(property).resource.uri)
    else null

private fun Resource.extractDataservices() : List<Dataservice> =
    listProperties(DCAT.service)
        .toList()
        .map { it.resource }
        .map { Dataservice().apply {
            id = it.uri
            title = it.extractProperty(DCTerms.title)?.string
            description = it.extractProperty(DCTerms.description)?.string
            endpointUrl = it.extractPropertyURI(DCAT.endpointURL)
            endpointdescription = it.extractPropertyURI(DCAT.endpointDescription)
            contactpoint = it.extractContactPoint()
        } }

private fun Resource.extractContactPoint() : Contact? =
    extractProperty(DCAT.contactPoint)
        ?.resource
        ?.let { Contact().apply {
            name = it.extractProperty(VCARD4.hasOrganizationName)?.string
        } }
