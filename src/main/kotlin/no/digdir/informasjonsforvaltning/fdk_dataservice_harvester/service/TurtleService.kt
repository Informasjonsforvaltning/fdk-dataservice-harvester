package no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.service

import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.model.CatalogTurtle
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.model.DataServiceTurtle
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.model.FDKCatalogTurtle
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.model.FDKDataServiceTurtle
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.model.HarvestSourceTurtle
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.model.TurtleDBO
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf.createRDFResponse
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.repository.CatalogTurtleRepository
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.repository.DataServiceTurtleRepository
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.repository.FDKCatalogTurtleRepository
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.repository.FDKDataServiceTurtleRepository
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.repository.HarvestSourceTurtleRepository
import org.apache.jena.rdf.model.Model
import org.apache.jena.riot.Lang
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream
import java.util.*
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import kotlin.text.Charsets.UTF_8

@Service
class TurtleService(
    private val catalogTurtleRepository: CatalogTurtleRepository,
    private val dataServiceTurtleRepository: DataServiceTurtleRepository,
    private val harvestSourceTurtleRepository: HarvestSourceTurtleRepository,
    private val fdkCatalogTurtleRepository: FDKCatalogTurtleRepository,
    private val fdkDataServiceTurtleRepository: FDKDataServiceTurtleRepository,
) {

    fun saveAsCatalog(model: Model, fdkId: String, withRecords: Boolean): TurtleDBO =
        if (withRecords) fdkCatalogTurtleRepository.save(model.createFDKCatalogTurtleDBO(fdkId))
        else catalogTurtleRepository.save(model.createCatalogTurtleDBO(fdkId))

    fun getCatalog(fdkId: String, withRecords: Boolean): String? =
        if (withRecords) fdkCatalogTurtleRepository.findByIdOrNull(fdkId)
            ?.turtle
            ?.let { ungzip(it) }
        else catalogTurtleRepository.findByIdOrNull(fdkId)
            ?.turtle
            ?.let { ungzip(it) }

    fun saveAsDataService(model: Model, fdkId: String, withRecords: Boolean): TurtleDBO =
        if (withRecords) fdkDataServiceTurtleRepository.save(model.createFDKDataServiceTurtleDBO(fdkId))
        else dataServiceTurtleRepository.save(model.createDataServiceTurtleDBO(fdkId))

    fun getDataService(fdkId: String, withRecords: Boolean): String? =
        if (withRecords) fdkDataServiceTurtleRepository.findByIdOrNull(fdkId)
            ?.turtle
            ?.let { ungzip(it) }
        else dataServiceTurtleRepository.findByIdOrNull(fdkId)
            ?.turtle
            ?.let { ungzip(it) }

    fun saveAsHarvestSource(model: Model, uri: String): TurtleDBO =
        harvestSourceTurtleRepository.save(model.createHarvestSourceTurtleDBO(uri))

    fun getHarvestSource(uri: String): String? =
        harvestSourceTurtleRepository.findByIdOrNull(uri)
            ?.turtle
            ?.let { ungzip(it) }

    fun deleteTurtleFiles(fdkId: String) {
        dataServiceTurtleRepository.deleteById(fdkId)
        fdkDataServiceTurtleRepository.deleteById(fdkId)
    }

}

private fun Model.createHarvestSourceTurtleDBO(uri: String): HarvestSourceTurtle =
    HarvestSourceTurtle(
        id = uri,
        turtle = gzip(createRDFResponse(Lang.TURTLE))
    )

private fun Model.createCatalogTurtleDBO(fdkId: String): CatalogTurtle =
    CatalogTurtle(
        id = fdkId,
        turtle = gzip(createRDFResponse(Lang.TURTLE))
    )

private fun Model.createFDKCatalogTurtleDBO(fdkId: String): FDKCatalogTurtle =
    FDKCatalogTurtle(
        id = fdkId,
        turtle = gzip(createRDFResponse(Lang.TURTLE))
    )

private fun Model.createDataServiceTurtleDBO(fdkId: String): DataServiceTurtle =
    DataServiceTurtle(
        id = fdkId,
        turtle = gzip(createRDFResponse(Lang.TURTLE))
    )

private fun Model.createFDKDataServiceTurtleDBO(fdkId: String): FDKDataServiceTurtle =
    FDKDataServiceTurtle(
        id = fdkId,
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
