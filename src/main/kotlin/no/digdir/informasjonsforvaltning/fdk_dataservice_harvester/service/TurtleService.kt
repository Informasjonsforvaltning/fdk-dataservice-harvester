package no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.service

import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.model.TurtleDBO
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf.createRDFResponse
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.repository.TurtleRepository
import org.apache.jena.rdf.model.Model
import org.apache.jena.riot.Lang
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream
import java.util.*
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import kotlin.text.Charsets.UTF_8

private const val NO_RECORDS_ID_PREFIX = "no-records-"
const val UNION_ID = "union-graph"
private const val CATALOG_ID_PREFIX = "catalog-"
private const val DATASERVICE_ID_PREFIX = "dataservice-"

@Service
class TurtleService(private val turtleRepository: TurtleRepository) {

    fun saveAsCatalogUnion(model: Model, withRecords: Boolean): TurtleDBO =
        turtleRepository.save(model.createCatalogTurtleDBO(UNION_ID, withRecords))

    fun getCatalogUnion(withRecords: Boolean): String? =
        turtleRepository.findByIdOrNull(catalogTurtleID(UNION_ID, withRecords))
            ?.turtle
            ?.let { ungzip(it) }

    fun saveAsCatalog(model: Model, fdkId: String, withRecords: Boolean): TurtleDBO =
        turtleRepository.save(model.createCatalogTurtleDBO(fdkId, withRecords))

    fun getCatalog(fdkId: String, withRecords: Boolean): String? =
        turtleRepository.findByIdOrNull(catalogTurtleID(fdkId, withRecords))
            ?.turtle
            ?.let { ungzip(it) }

    fun saveAsDataService(model: Model, fdkId: String, withRecords: Boolean): TurtleDBO =
        turtleRepository.save(model.createDataServiceTurtleDBO(fdkId, withRecords))

    fun getDataService(fdkId: String, withRecords: Boolean): String? =
        turtleRepository.findByIdOrNull(dataServiceTurtleID(fdkId, withRecords))
            ?.turtle
            ?.let { ungzip(it) }

    fun saveAsHarvestSource(model: Model, uri: String): TurtleDBO =
        turtleRepository.save(model.createHarvestSourceTurtleDBO(uri))

    fun getHarvestSource(uri: String): String? =
        turtleRepository.findByIdOrNull(uri)
            ?.turtle
            ?.let { ungzip(it) }

    fun deleteTurtleFiles(fdkId: String) {
        turtleRepository.findAllById(
            listOf(
                dataServiceTurtleID(fdkId, true),
                dataServiceTurtleID(fdkId, false)
            )
        ).run { turtleRepository.deleteAll(this) }
    }

}

fun catalogTurtleID(fdkId: String, withFDKRecords: Boolean): String =
    "$CATALOG_ID_PREFIX${if (withFDKRecords) "" else NO_RECORDS_ID_PREFIX}$fdkId"

fun dataServiceTurtleID(fdkId: String, withFDKRecords: Boolean): String =
    "$DATASERVICE_ID_PREFIX${if (withFDKRecords) "" else NO_RECORDS_ID_PREFIX}$fdkId"

private fun Model.createHarvestSourceTurtleDBO(uri: String): TurtleDBO =
    TurtleDBO(
        id = uri,
        turtle = gzip(createRDFResponse(Lang.TURTLE))
    )

private fun Model.createCatalogTurtleDBO(fdkId: String, withRecords: Boolean): TurtleDBO =
    TurtleDBO(
        id = catalogTurtleID(fdkId, withRecords),
        turtle = gzip(createRDFResponse(Lang.TURTLE))
    )

private fun Model.createDataServiceTurtleDBO(fdkId: String, withRecords: Boolean): TurtleDBO =
    TurtleDBO(
        id = dataServiceTurtleID(fdkId, withRecords),
        turtle = gzip(createRDFResponse(Lang.TURTLE))
    )

fun gzip(content: String): String {
    val bos = ByteArrayOutputStream()
    GZIPOutputStream(bos).bufferedWriter(UTF_8).use { it.write(content) }
    return Base64.getEncoder().encodeToString(bos.toByteArray())
}

fun ungzip(base64Content: String): String {
    val content = Base64.getDecoder().decode(base64Content)
    return GZIPInputStream(content.inputStream())
        .bufferedReader(UTF_8)
        .use { it.readText() }
}
