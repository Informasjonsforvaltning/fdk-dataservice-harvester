package no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.adapter

import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.harvester.HarvestException
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.model.HarvestDataSource
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import java.io.BufferedReader
import java.net.HttpURLConnection
import java.net.URL

private const val TEN_MINUTES = 600000

@Service
class DataServiceAdapter {

    fun getDataServices(source: HarvestDataSource): String {
        val connection = URL(source.url).openConnection() as HttpURLConnection
        connection.setRequestProperty("Accept", source.acceptHeaderValue)
        connection.connectTimeout = TEN_MINUTES
        connection.readTimeout = TEN_MINUTES

        return if (connection.responseCode != HttpStatus.OK.value()) {
            throw HarvestException("${source.url} responded with ${connection.responseCode}, harvest will be aborted")
        } else {
            connection
                .inputStream
                .bufferedReader()
                .use(BufferedReader::readText)
        }
    }
}
