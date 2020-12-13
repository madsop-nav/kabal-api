package no.nav.klage.oppgave.domain.oppgavekopi

import java.time.LocalDate
import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(name = "oppgave", schema = "oppgave")
data class OppgaveKopi(

    @Id
    @Column(name = "id")
    var id: Long,
    @Column(name = "versjon")
    var versjon: Int,
    @Column(name = "journalpostid")
    var journalpostId: String? = null,
    @Column(name = "saksreferanse")
    var saksreferanse: String? = null,
    @Column(name = "mappe_id")
    var mappeId: Long? = null,
    @Column(name = "status_id")
    @Convert(converter = StatusConverter::class)
    var status: Status,
    @Column(name = "tildelt_enhetsnr")
    var tildeltEnhetsnr: String,
    @Column(name = "opprettet_av_enhetsnr")
    var opprettetAvEnhetsnr: String? = null,
    @Column(name = "endret_av_enhetsnr")
    var endretAvEnhetsnr: String? = null,
    @Column(name = "tema")
    var tema: String,
    @Column(name = "temagruppe")
    var temagruppe: String? = null,
    @Column(name = "behandlingstema")
    var behandlingstema: String? = null,
    @Column(name = "oppgavetype")
    var oppgavetype: String,
    @Column(name = "behandlingstype")
    var behandlingstype: String? = null,
    @Column(name = "prioritet")
    @Enumerated(EnumType.STRING)
    var prioritet: Prioritet,
    @Column(name = "tilordnet_ressurs")
    var tilordnetRessurs: String? = null,
    @Column(name = "beskrivelse")
    var beskrivelse: String? = null,
    @Column(name = "frist_ferdigstillelse")
    var fristFerdigstillelse: LocalDate,
    @Column(name = "aktiv_dato")
    var aktivDato: LocalDate,
    @Column(name = "opprettet_av")
    var opprettetAv: String,
    @Column(name = "endret_av")
    var endretAv: String? = null,
    @Column(name = "opprettet_tidspunkt")
    var opprettetTidspunkt: LocalDateTime,
    @Column(name = "endret_tidspunkt")
    var endretTidspunkt: LocalDateTime? = null,
    @Column(name = "ferdigstilt_tidspunkt")
    var ferdigstiltTidspunkt: LocalDateTime? = null,
    @Column(name = "behandles_av_applikasjon")
    var behandlesAvApplikasjon: String? = null,
    @Column(name = "journalpostkilde")
    var journalpostkilde: String? = null,
    @OneToOne(cascade = [CascadeType.ALL])
    @JoinColumn(name = "ident_id", referencedColumnName = "id")
    var ident: Ident? = null,
    @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true)
    @JoinColumn(name = "oppgave_id", referencedColumnName = "id", nullable = false)
    var metadata: Set<Metadata> = mutableSetOf()
) {
    fun statuskategori(): Statuskategori = status.kategoriForStatus()

    fun getMetadataAsMap(): Map<MetadataNoekkel, String> {
        return metadata.map { it.noekkel to it.verdi }.toMap()
    }

    fun setMetadataAsMap(map: Map<MetadataNoekkel, String>) {
        metadata = map.map { Metadata(noekkel = it.key, verdi = it.value) }.toSet()
    }

    fun toVersjon(): OppgaveKopiVersjon =
        OppgaveKopiVersjon(
            id = this.id,
            versjon = versjon,
            journalpostId = journalpostId,
            saksreferanse = saksreferanse,
            mappeId = mappeId,
            status = status,
            tildeltEnhetsnr = tildeltEnhetsnr,
            opprettetAvEnhetsnr = opprettetAvEnhetsnr,
            endretAvEnhetsnr = endretAvEnhetsnr,
            tema = tema,
            temagruppe = temagruppe,
            behandlingstema = behandlingstema,
            oppgavetype = oppgavetype,
            behandlingstype = behandlingstype,
            prioritet = prioritet,
            tilordnetRessurs = tilordnetRessurs,
            beskrivelse = beskrivelse,
            fristFerdigstillelse = fristFerdigstillelse,
            aktivDato = aktivDato,
            opprettetAv = opprettetAv,
            endretAv = endretAv,
            opprettetTidspunkt = opprettetTidspunkt,
            endretTidspunkt = endretTidspunkt,
            ferdigstiltTidspunkt = ferdigstiltTidspunkt,
            behandlesAvApplikasjon = behandlesAvApplikasjon,
            journalpostkilde = journalpostkilde,
            ident = if (ident == null) {
                null
            } else {
                VersjonIdent(
                    id = null,
                    identType = ident!!.identType,
                    verdi = ident!!.verdi,
                    folkeregisterident = ident!!.folkeregisterident,
                    registrertDato = ident!!.registrertDato
                )
            },
            metadata = metadata.map {
                VersjonMetadata(
                    id = null,
                    noekkel = it.noekkel,
                    verdi = it.verdi
                )
            }
        )
}