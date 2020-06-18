package no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf

import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.Application
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.rdf.model.Resource
import org.apache.jena.rdf.model.ResourceRequiredException
import org.apache.jena.rdf.model.Statement
import org.apache.jena.sparql.vocabulary.FOAF
import org.apache.jena.vocabulary.DCAT
import org.apache.jena.vocabulary.DCTerms
import org.apache.jena.vocabulary.RDF
import org.apache.jena.vocabulary.VCARD4
import org.apache.jena.vocabulary.XSD
import org.slf4j.LoggerFactory
import java.io.ByteArrayOutputStream
import java.io.StringReader
import java.util.*

private val logger = LoggerFactory.getLogger(Application::class.java)
const val BACKUP_BASE_URI = "http://example.com/"

enum class JenaType(val value: String){
    TURTLE("TURTLE"),
    RDF_XML("RDF/XML"),
    RDF_JSON("RDF/JSON"),
    JSON_LD("JSON-LD"),
    NTRIPLES("N-TRIPLES"),
    N3("N3"),
    NOT_JENA("NOT-JENA")
}

fun jenaTypeFromAcceptHeader(accept: String?): JenaType? =
    when (accept) {
        "text/turtle" -> JenaType.TURTLE
        "application/rdf+xml" -> JenaType.RDF_XML
        "application/rdf+json" -> JenaType.RDF_JSON
        "application/ld+json" -> JenaType.JSON_LD
        "application/n-triples" -> JenaType.NTRIPLES
        "text/n3" -> JenaType.N3
        "*/*" -> null
        null -> null
        else -> JenaType.NOT_JENA
    }

fun parseRDFResponse(responseBody: String, rdfLanguage: JenaType, rdfSource: String?): Model? {
    val responseModel = ModelFactory.createDefaultModel()

    try {
        responseModel.read(StringReader(responseBody), BACKUP_BASE_URI, rdfLanguage.value)
    } catch (ex: Exception) {
        logger.error("Parse from $rdfSource has failed: ${ex.message}")
        return null
    }

    return responseModel
}

fun Model.listOfCatalogResources(): List<Resource> =
    listResourcesWithProperty(RDF.type, DCAT.Catalog)
        .toList()

fun Model.listOfDataServiceResources(): List<Resource> =
    listResourcesWithProperty(RDF.type, DCAT.DataService)
        .toList()

fun Resource.createModel(): Model =
    listProperties()
        .toModel()
        .addResourceNodes(this)

private fun Model.addResourceNodes(resource: Resource): Model {
    add(resource.listProperties())

    resource.listProperties()
        .toList()
        .filter { it.isResourceProperty() }
        .forEach { addResourceNodes(it.resource) }

    return this
}

private fun Statement.isResourceProperty(): Boolean =
    try {
        resource.isResource
    } catch (ex: ResourceRequiredException) {
        false
    }

fun Model.addDefaultPrefixes(): Model {
    setNsPrefix("dct", DCTerms.NS)
    setNsPrefix("dcat", DCAT.NS)
    setNsPrefix("foaf", FOAF.getURI())
    setNsPrefix("vcard", VCARD4.NS)
    setNsPrefix("xsd", XSD.NS)

    return this
}

fun Model.createRDFResponse(responseType: JenaType): String =
    ByteArrayOutputStream().use{ out ->
        write(out, responseType.value)
        out.flush()
        out.toString("UTF-8")
    }

fun Model.extractMetaDataIdentifier(): String? =
    listResourcesWithProperty(RDF.type, DCAT.CatalogRecord)
        .toList()
        .firstOrNull()
        ?.getProperty(DCTerms.identifier)
        ?.string

fun createIdFromUri(uri: String): String =
    UUID.nameUUIDFromBytes(uri.toByteArray())
        .toString()

fun Model.extractMetaDataTopic(): String? =
    listResourcesWithProperty(RDF.type, DCAT.CatalogRecord)
        .toList()
        .firstOrNull()
        ?.getPropertyResourceValue(FOAF.primaryTopic)
        ?.uri

fun queryToGetMetaDataByUri(uri: String): String =
    """PREFIX foaf: <${FOAF.NS}>
       DESCRIBE * WHERE { 
           ?s foaf:primaryTopic <$uri> 
       }""".trimIndent()

fun queryToGetMetaDataByCatalogUri(uri: String): String =
    """PREFIX dct: <${DCTerms.NS}>
       DESCRIBE * WHERE { 
           ?s dct:isPartOf <$uri> 
       }""".trimIndent()
