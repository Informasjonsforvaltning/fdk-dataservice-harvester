package no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf

import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.Application
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.rdf.model.Property
import org.apache.jena.rdf.model.Resource
import org.apache.jena.rdf.model.ResourceRequiredException
import org.apache.jena.rdf.model.Statement
import org.apache.jena.riot.Lang
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

fun jenaTypeFromAcceptHeader(accept: String?): Lang? =
    when {
        accept == null -> null
        accept.contains("text/turtle") -> Lang.TURTLE
        accept.contains("application/rdf+xml") -> Lang.RDFXML
        accept.contains("application/rdf+json") -> Lang.RDFJSON
        accept.contains("application/ld+json") -> Lang.JSONLD
        accept.contains("application/n-triples") -> Lang.NTRIPLES
        accept.contains("text/n3") -> Lang.N3
        accept.contains("*/*") -> null
        else -> Lang.RDFNULL
    }

fun parseRDFResponse(responseBody: String, rdfLanguage: Lang, rdfSource: String?): Model? {
    val responseModel = ModelFactory.createDefaultModel()

    try {
        responseModel.read(StringReader(responseBody), BACKUP_BASE_URI, rdfLanguage.name)
    } catch (ex: Exception) {
        logger.error("Parse from $rdfSource has failed: ${ex.message}")
        return null
    }

    return responseModel
}

fun Resource.modelOfResourceProperties(property: Property): Model {
    val model = ModelFactory.createDefaultModel()

    listProperties(property)
        .toList()
        .filter { it.isResourceProperty() }
        .map { it.resource }
        .forEach { model.add(it.listProperties()) }

    return model
}

fun Statement.isResourceProperty(): Boolean =
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

fun Model.createRDFResponse(responseType: Lang): String =
    ByteArrayOutputStream().use{ out ->
        write(out, responseType.name)
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
