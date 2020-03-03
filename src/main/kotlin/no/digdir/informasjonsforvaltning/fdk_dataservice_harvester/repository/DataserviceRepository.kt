package no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.repository

import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.model.DataserviceDB
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository
import org.springframework.stereotype.Repository

@Repository
interface DataserviceRepository : ElasticsearchRepository<DataserviceDB, String> {

}