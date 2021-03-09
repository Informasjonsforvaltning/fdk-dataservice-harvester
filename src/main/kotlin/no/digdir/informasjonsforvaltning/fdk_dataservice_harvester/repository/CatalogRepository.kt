package no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.repository

import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.model.CatalogMeta
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface CatalogRepository : MongoRepository<CatalogMeta, String>
