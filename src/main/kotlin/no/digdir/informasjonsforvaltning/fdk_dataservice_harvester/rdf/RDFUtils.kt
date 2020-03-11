package no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf

import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.rdf.model.Property
import org.apache.jena.rdf.model.Resource
import org.apache.jena.rdf.model.Statement
import org.apache.jena.sparql.vocabulary.FOAF
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

fun Resource.createModelOfTopLevelProperties(): Model {
    val model = ModelFactory.createDefaultModel()
    model.add(listProperties())

    return model
}

fun Resource.createDataserviceModel(): Model {
    val model = ModelFactory.createDefaultModel()
    model.add(listProperties())
    model.add(extractProperty(DCAT.contactPoint)?.resource?.listProperties())

    return model
}

private fun Resource.extractProperty(property: Property) : Statement? =
    if (this.hasProperty(property)) this.getProperty(property)
    else null

fun Model.addDefaultPrefixes(): Model {
    setNsPrefix("dct", DCTerms.NS)
    setNsPrefix("dcat", DCAT.NS)
    setNsPrefix("foaf", FOAF.getURI())
    setNsPrefix("vcard", VCARD4.NS)

    return this
}

fun Model.createRDFResponse(responseType: JenaType): String =
    ByteArrayOutputStream().use{ out ->
        write(out, responseType.value)
        out.flush()
        out.toString("UTF-8")
    }

fun Model.extractMetaDataIdentifier(): String =
    listResourcesWithProperty(RDF.type, DCAT.record)
        .toList()
        .first()
        .getProperty(DCTerms.identifier).string
