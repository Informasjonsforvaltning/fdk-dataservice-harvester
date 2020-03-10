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
import org.apache.jena.vocabulary.RDFS
import org.apache.jena.vocabulary.VCARD4
import org.apache.jena.vocabulary.XSD
import java.io.ByteArrayOutputStream
import java.io.StringReader
import java.net.URI

enum class JenaType(val value: String){
    TURTLE("TURTLE"),
    RDF_XML("RDF/XML"),
    RDF_JSON("RDF/JSON"),
    JSON_LD("JSON-LD"),
    NOT_JENA("NOT-JENA")
}
fun returnTypeFromAcceptHeader(accept: String?): JenaType =
    when (accept) {
        "text/turtle" -> JenaType.TURTLE
        "application/rdf+xml" -> JenaType.RDF_XML
        "application/rdf+json" -> JenaType.RDF_JSON
        "application/ld+json" -> JenaType.JSON_LD
        else -> JenaType.NOT_JENA
    }

fun parseRDFResponse(responseBody: String, rdfLanguage: JenaType): Model {
    val responseModel = ModelFactory.createDefaultModel()
    responseModel.read(StringReader(responseBody), "", rdfLanguage.value)
    return responseModel
}

fun Model.listOfCatalogResources(): List<Resource> =
    listResourcesWithProperty(RDF.type, DCAT.Catalog)
        .toList()

fun Model.listOfDataServiceResources(): List<Resource> =
    listResourcesWithProperty(RDF.type, DCAT.DataService)
        .toList()

fun Resource.createCatalogModel(): Model {
    val model = ModelFactory.createDefaultModel()
    model.add(listProperties())
    model.add(extractProperty(HarvestMetaData.metaData)?.resource?.listProperties())

    listProperties(DCAT.service)
        .toList()
        .map { it.resource.createDataserviceModel() }
        .forEach { model.add(it) }

    return model
}

fun Resource.createDataserviceModel(): Model {
    val model = ModelFactory.createDefaultModel()
    model.add(listProperties())
    model.add(extractProperty(HarvestMetaData.metaData)?.resource?.listProperties())
    model.add(extractProperty(DCAT.contactPoint)?.resource?.listProperties())

    return model
}

fun Model.parseCatalogs(): List<Catalog> =
    listOfCatalogResources()
        .map {
            Catalog().apply {
                id = it.uri
                publisherUrl = it.extractPropertyURI(DCTerms.publisher)
                title = it.extractProperty(DCTerms.title)?.string
                description = it.extractProperty(DCTerms.description)?.string
                dataservices = it.extractDataServices()
        }}

private fun Resource.extractProperty(property: Property) : Statement? =
    if (this.hasProperty(property)) this.getProperty(property)
    else null

private fun Resource.extractPropertyURI(property: Property) : URI? =
    if (this.hasProperty(property)) URI(this.getProperty(property).resource.uri)
    else null

private fun Resource.extractDataServices() : List<Dataservice> =
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

fun Model.addDefaultPrefixes(): Model {
    setNsPrefix("meta", HarvestMetaData.uri)
    setNsPrefix("dcat", DCAT.NS)
    setNsPrefix("dct", DCTerms.NS)
    setNsPrefix("rdf", RDF.uri)
    setNsPrefix("rdfs", RDFS.uri)
    setNsPrefix("vcard", VCARD4.NS)
    setNsPrefix("xml", "http://www.w3.org/XML/1998/namespace")
    setNsPrefix("xsd", XSD.NS)

    return this
}

fun Model.createRDFResponse(responseType: JenaType): String =
    ByteArrayOutputStream().use{ out ->
        write(out, responseType.value)
        out.flush()
        out.toString("UTF-8")
    }

fun Resource.extractMetaDataIdentifier(): String =
    getProperty(HarvestMetaData.metaData)
        .resource
        .getProperty(DCTerms.identifier)
        .string