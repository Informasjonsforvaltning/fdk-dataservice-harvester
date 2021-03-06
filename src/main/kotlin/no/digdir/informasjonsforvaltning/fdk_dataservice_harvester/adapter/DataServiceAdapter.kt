package no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.adapter

import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.harvester.HarvestException
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.model.HarvestDataSource
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import java.io.BufferedReader
import java.net.HttpURLConnection
import java.net.URL

private val LOGGER = LoggerFactory.getLogger(DataServiceAdapter::class.java)

@Service
class DataServiceAdapter {

    fun getDataServices(source: HarvestDataSource): String? =
        try {
            val connection = URL(source.url).openConnection() as HttpURLConnection
            connection.setRequestProperty("Accept", source.acceptHeaderValue)

            if (connection.responseCode != HttpStatus.OK.value()) {
                LOGGER.error("${source.url} responded with ${connection.responseCode}, harvest will be aborted", HarvestException(source.url ?: "undefined"))
                null
            } else {
                connection
                    .inputStream
                    .bufferedReader()
                    .use(BufferedReader::readText)
            }

        } catch (ex: Exception) {
            LOGGER.error("Error when harvesting from ${source.url}", ex)
            null
        }

}
