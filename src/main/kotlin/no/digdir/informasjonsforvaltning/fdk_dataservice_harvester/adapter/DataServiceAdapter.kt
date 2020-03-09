package no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.adapter

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import java.io.BufferedReader
import java.net.HttpURLConnection
import java.net.URL

private val LOGGER = LoggerFactory.getLogger(DataServiceAdapter::class.java)

enum class AcceptHeaders(val value: String) {
    TURTLE("text/turtle")
}

@Service
class DataServiceAdapter {

    fun getDataServiceCatalog(url: String): String? {
        val connection = URL(url).openConnection() as HttpURLConnection

        connection.setRequestProperty("Accept", AcceptHeaders.TURTLE.value)

        return if (connection.responseCode != HttpStatus.OK.value()) {
            LOGGER.error("Harvest from $url has failed")
            null
        } else {
            connection
                .inputStream
                .bufferedReader()
                .use(BufferedReader::readText)
        }
    }

}