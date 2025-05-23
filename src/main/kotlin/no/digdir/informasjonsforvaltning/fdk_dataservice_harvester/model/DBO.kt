package no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.model

import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.rdf.parseRDFResponse
import no.digdir.informasjonsforvaltning.fdk_dataservice_harvester.service.ungzip
import org.apache.jena.riot.Lang
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "dataServiceMeta")
data class DataServiceMeta (
    @Id
    val uri: String,

    val fdkId: String,

    val isPartOf: String,
    val removed: Boolean = false,

    val issued: Long,
    val modified: Long
)

@Document(collection = "catalogMeta")
data class CatalogMeta (
        @Id
        val uri: String,

        val fdkId: String,

        val issued: Long,
        val modified: Long
)

@Document(collection = "harvestSourceTurtle")
data class HarvestSourceTurtle(
    @Id override val id: String,
    override val turtle: String
) : TurtleDBO()

@Document(collection = "catalogTurtle")
data class CatalogTurtle(
    @Id override val id: String,
    override val turtle: String
) : TurtleDBO()

@Document(collection = "fdkCatalogTurtle")
data class FDKCatalogTurtle(
    @Id override val id: String,
    override val turtle: String
) : TurtleDBO()

@Document(collection = "dataServiceTurtle")
data class DataServiceTurtle(
    @Id override val id: String,
    override val turtle: String
) : TurtleDBO()

@Document(collection = "fdkDataServiceTurtle")
data class FDKDataServiceTurtle(
    @Id override val id: String,
    override val turtle: String
) : TurtleDBO()

abstract class TurtleDBO {
    abstract val id: String
    abstract val turtle: String
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TurtleDBO

        return when {
            id != other.id -> false
            else -> zippedModelsAreIsomorphic(turtle, other.turtle)
        }
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + turtle.hashCode()
        return result
    }
}

private fun zippedModelsAreIsomorphic(zip0: String, zip1: String): Boolean {
    val model0 = parseRDFResponse(ungzip(zip0), Lang.TURTLE, null)
    val model1 = parseRDFResponse(ungzip(zip1), Lang.TURTLE, null)

    return when {
        model0 != null && model1 != null -> model0.isIsomorphicWith(model1)
        model0 == null && model1 == null -> true
        else -> false
    }
}
