package no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf

import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.Property
import org.apache.jena.rdf.model.RDFNode
import org.apache.jena.rdf.model.Resource
import org.apache.jena.vocabulary.DCAT
import org.apache.jena.vocabulary.DCTerms
import org.apache.jena.vocabulary.RDF

fun changedCatalogAndDataServices(harvested: Model, dbModel: Model?): Map<String, List<String>> {
    val catalogMap = mutableMapOf<String, List<String>>()
    harvested.listResourcesWithProperty(RDF.type, DCAT.Catalog)
        .toList()
        .forEach { catalog ->
            catalogMap[catalog.uri] = catalog.listProperties(DCAT.service)
                .toList()
                .map { dataservice -> dataservice.resource.uri }
                .filter {
                    if (dbModel != null) serviceDiffersInModels(it, harvested, dbModel)
                    else true
                }
        }

    return catalogMap.toMap()
}

fun serviceDiffersInModels(serviceURI: String, harvested: Model, fromDB: Model): Boolean {
    val harvestedDataset = harvested.getResource(serviceURI)
    val obj: RDFNode? = null
    return if (fromDB.contains(harvestedDataset, null, obj)) {
        val dbDataset = fromDB.getResource(serviceURI)

        when {
            resourceLiteralsDiffers(harvestedDataset, dbDataset) -> true
            propertyDiffers(DCAT.contactPoint, harvestedDataset, dbDataset) -> true
            else -> false
        }
    } else true
}

fun resourceLiteralsDiffers(harvested: Resource, db: Resource): Boolean =
    !harvested.listProperties().toModel().isIsomorphicWith(db.listProperties().toModel())

fun propertyDiffers(property: Property, harvested: Resource, db: Resource): Boolean {
    val harvestedPropertiesModel = harvested.modelOfResourceProperties(property)
    val dbPropertiesModel = db.modelOfResourceProperties(property)

    return !harvestedPropertiesModel.isIsomorphicWith(dbPropertiesModel)
}

fun catalogLiteralsDiffers(uri: String, harvested: Model, fromDB: Model): Boolean {
    val harvestedCatalog = harvested.getResource(uri)
    val obj: RDFNode? = null
    return if (fromDB.contains(harvestedCatalog, null, obj)) {
        val dbCatalog = fromDB.getResource(uri)
        resourceLiteralsDiffers(harvestedCatalog, dbCatalog)
    } else true
}
