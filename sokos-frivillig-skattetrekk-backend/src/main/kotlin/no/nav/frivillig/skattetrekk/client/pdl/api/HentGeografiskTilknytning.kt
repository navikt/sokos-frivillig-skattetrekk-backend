package no.nav.frivillig.skattetrekk.client.pdl.api

const val DEFAULT_UTLAND = "UTLAND"

data class HentPdlGeografiskTilknytningResponse(
    val data: DataWrapperGeografiskTilknytning?,
    val errors: List<PdlError>? = null,
)

data class DataWrapperGeografiskTilknytning(
    val hentGeografiskTilknytning: GeografiskTilknytning?,
)

data class GeografiskTilknytning(
    val gtType: GtType,
    val gtKommune: String? = null,
    val gtBydel: String? = null,
    val gtLand: String? = null,
)

enum class GtType {
    KOMMUNE,
    BYDEL,
    UTLAND,
    UDEFINERT,
}

fun GeografiskTilknytning.getValidFieldAsString() =
    when (this.gtType) {
        GtType.KOMMUNE -> gtKommune
        GtType.BYDEL -> gtBydel
        GtType.UTLAND ->
            when (gtLand) {
                null -> DEFAULT_UTLAND
                else -> gtLand
            }
        else -> null
    }

fun GeografiskTilknytning.getValidKommuneOrBydelAsString() =
    when (this.gtType) {
        GtType.KOMMUNE -> gtKommune
        GtType.BYDEL -> gtBydel
        GtType.UTLAND -> null
        else -> null
    }
