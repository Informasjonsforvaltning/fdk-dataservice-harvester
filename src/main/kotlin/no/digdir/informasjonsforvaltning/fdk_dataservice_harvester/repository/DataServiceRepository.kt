package no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.repository

import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.model.DataServiceDBO
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface DataServiceRepository : MongoRepository<DataServiceDBO, String> {
    fun findOneByFdkId(fdkId: String): DataServiceDBO?
}