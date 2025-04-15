package no.nav.pensjon.selvbetjening.skattetrekk.client.pdl.api

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

data class HentPdlGeografiskTilknytningOgAdressebeskyttelseResponse(val data: GeografiskTilknytningOgAdressebeskyttelse?, val errors: List<PdlError>? = null)

data class GeografiskTilknytningOgAdressebeskyttelse(
    val hentPerson: PersonMedAdressebeskyttelse?,
    val hentGeografiskTilknytning: GeografiskTilknytning?
)

data class PersonMedAdressebeskyttelse(
    val adressebeskyttelse: List<Adressebeskyttelse>? = null
)

data class Adressebeskyttelse(
    val gradering: AdressebeskyttelseGradering,
    val folkeregistermetadata: FolkeregisterMetadata? = null,
    val metadata: Metadata
)

enum class AdressebeskyttelseGradering {
    STRENGT_FORTROLIG_UTLAND,
    STRENGT_FORTROLIG,
    FORTROLIG,
    UGRADERT
}

data class Metadata(
    @JsonProperty("master") val master: String?,
    @JsonProperty("endringer") val endringer: List<MetadataEndring>?,
    @JsonProperty("historisk") val historisk: Boolean?
)

data class MetadataEndring(
    @JsonFormat(shape = JsonFormat.Shape.STRING) @JsonProperty("registrert") val registrert: LocalDateTime?,
    @JsonProperty("kilde") val kilde: String?,
    @JsonProperty("registrertAv") val registrertAv: String?,
    @JsonProperty("systemkilde") val systemkilde: String?,
    @JsonProperty("type") val type: String?
)

data class FolkeregisterMetadata(
    @JsonFormat(shape = JsonFormat.Shape.STRING) @JsonProperty("ajourholdstidspunkt") val ajourholdstidspunkt: LocalDateTime?,
    @JsonFormat(shape = JsonFormat.Shape.STRING) @JsonProperty("gyldighetstidspunkt") val gyldighetstidspunkt: LocalDateTime?
)
