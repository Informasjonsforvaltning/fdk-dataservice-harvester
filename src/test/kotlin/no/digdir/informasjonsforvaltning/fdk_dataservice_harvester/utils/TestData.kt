package no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.utils

import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.generated.model.Catalog
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.generated.model.Contact
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.generated.model.Dataservice
import java.net.URI

val dataservice1 = Dataservice().apply {
    id = "http://localhost:8080/dataservices/1"
    title = "National Data Directory Organization Catalogue"
    description = "Exposes a basic service which provides information related to the organization catalogue in the National Data Directory"
    endpointUrl = URI("https://fellesdatakatalog.brreg.no")
    endpointdescription = URI("https://raw.githubusercontent.com/Informasjonsforvaltning/organization-catalogue/master/src/main/resources/specification/organization-catalogue.yaml")
    contactpoint = Contact().apply { name = "Brønnøysundregistrene" }
}

val catalog1 = Catalog().apply {
    id = "http://localhost:8080/catalogs/1"
    publisherUrl = URI("https://organization-catalogue.fellesdatakatalog.brreg.no/organizations/991825827")
    title = "Dataservicekatalog for Digitaliseringsdirektoratet"
    description = null
    dataservices = listOf(dataservice1)
}