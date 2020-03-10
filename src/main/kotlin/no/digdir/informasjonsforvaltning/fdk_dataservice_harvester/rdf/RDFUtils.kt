package no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf

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
    model.add(extractProperty(HarvestMetaData.fdkMetaData)?.resource?.listProperties())

    listProperties(DCAT.service)
        .toList()
        .map { it.resource.createDataserviceModel() }
        .forEach { model.add(it) }

    return model
}

fun Resource.createDataserviceModel(): Model {
    val model = ModelFactory.createDefaultModel()
    model.add(listProperties())
    model.add(extractProperty(HarvestMetaData.fdkMetaData)?.resource?.listProperties())
    model.add(extractProperty(DCAT.contactPoint)?.resource?.listProperties())

    return model
}

private fun Resource.extractProperty(property: Property) : Statement? =
    if (this.hasProperty(property)) this.getProperty(property)
    else null

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
    getProperty(HarvestMetaData.fdkMetaData)
        .resource
        .getProperty(DCTerms.identifier)
        .string