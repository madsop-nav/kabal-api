package no.nav.klage.oppgave.api.mapper


import no.nav.klage.oppgave.api.internal.OppgaveKopiAPIModel
import no.nav.klage.oppgave.api.view.*
import no.nav.klage.oppgave.clients.gosys.*
import no.nav.klage.oppgave.clients.pdl.HentPersonBolkResult
import no.nav.klage.oppgave.clients.pdl.PdlClient
import no.nav.klage.oppgave.domain.oppgavekopi.*
import no.nav.klage.oppgave.domain.oppgavekopi.Ident
import no.nav.klage.oppgave.domain.oppgavekopi.Prioritet
import no.nav.klage.oppgave.domain.oppgavekopi.Status
import no.nav.klage.oppgave.util.getLogger
import no.nav.klage.oppgave.util.getSecureLogger
import org.springframework.stereotype.Service
import no.nav.klage.oppgave.api.view.Oppgave as OppgaveView
import no.nav.klage.oppgave.clients.gosys.Oppgave as OppgaveBackend

@Service
class OppgaveMapper(val pdlClient: PdlClient) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val secureLogger = getSecureLogger()
    }

    fun mapOppgaveToView(oppgaveBackend: OppgaveBackend, fetchPersoner: Boolean): OppgaveView {
        return mapOppgaverToView(listOf(oppgaveBackend), fetchPersoner).single()
    }

    fun mapOppgaverToView(oppgaverBackend: List<OppgaveBackend>, fetchPersoner: Boolean): List<OppgaveView> {
        val personer = mutableMapOf<String, OppgaveView.Person>()
        if (fetchPersoner) {
            personer.putAll(getPersoner(getFnr(oppgaverBackend)))
        }

        return oppgaverBackend.map { oppgaveBackend ->
            OppgaveView(
                id = oppgaveBackend.id.toString(),
                person = if (fetchPersoner) {
                    personer[oppgaveBackend.getFnrForBruker()] ?: OppgaveView.Person("Mangler fnr", "Mangler navn")
                } else {
                    null
                },
                type = oppgaveBackend.toType(),
                tema = oppgaveBackend.toYtelse(),
                hjemmel = oppgaveBackend.metadata.toHjemmel(),
                frist = oppgaveBackend.fristFerdigstillelse,
                versjon = oppgaveBackend.versjon
            )
        }
    }

    private fun Map<String, String>?.toHjemmel() = this?.get(HJEMMEL) ?: "mangler"

    private fun OppgaveBackend.toType(): String {
        return if (behandlingstema == null) {
            when (behandlingstype) {
                BEHANDLINGSTYPE_KLAGE -> TYPE_KLAGE
                BEHANDLINGSTYPE_FEILUTBETALING -> TYPE_FEILUTBETALING
                else -> "ukjent"
            }
        } else "mangler"
    }

    private fun OppgaveBackend.toYtelse(): String = when (tema) {
        TEMA_SYK -> YTELSE_SYK
        TEMA_FOR -> YTELSE_FOR
        else -> tema
    }

    private fun getFnr(oppgaver: List<OppgaveBackend>) =
        oppgaver.mapNotNull {
            it.getFnrForBruker()
        }

    private fun getPersoner(fnrList: List<String>): Map<String, OppgaveView.Person> {
        logger.debug("getPersoner is called with {} fnr", fnrList.size)
        secureLogger.debug("getPersoner with fnr: {}", fnrList)

        val people = pdlClient.getPersonInfo(fnrList).data?.hentPersonBolk ?: emptyList()

        logger.debug("pdl returned {} people", people.size)
        secureLogger.debug("pdl returned {}", people)

        val fnrToPerson: Map<String, no.nav.klage.oppgave.api.view.Oppgave.Person> = people.map {
            val fnr = it.ident
            fnr to OppgaveView.Person(
                fnr = fnr,
                navn = it.person?.navn?.firstOrNull()?.toName() ?: "mangler navn"
            )
        }.toMap()
        return fnrList.map {
            if (fnrToPerson.containsKey(it)) {
                Pair(it, fnrToPerson.getValue(it))
            } else {
                Pair(it, OppgaveView.Person(fnr = it, navn = "Mangler navn"))
            }
        }.toMap()
    }

    private fun OppgaveBackend.getFnrForBruker(): String? {
        logger.debug("getFnrForBruker is called")
        secureLogger.debug("getFnrForBruker is called with oppgave: {}", this)

        return identer?.find { i -> i.gruppe == Gruppe.FOLKEREGISTERIDENT }?.ident.also {
            if (it != null) {
                logger.debug("Returning found fnr")
                secureLogger.debug("Returning found fnr from oppgave: {}", it)
            } else {
                logger.debug("No fnr found in oppgave")
            }
        }
    }

    private fun HentPersonBolkResult.Person.Navn.toName() = "$fornavn $etternavn"

    fun mapOppgaveKopiAPIModelToOppgaveKopi(oppgave: OppgaveKopiAPIModel): OppgaveKopi {
        return OppgaveKopi(
            id = oppgave.id,
            versjon = oppgave.versjon,
            journalpostId = oppgave.journalpostId,
            saksreferanse = oppgave.saksreferanse,
            mappeId = oppgave.mappeId,
            status = Status.valueOf(oppgave.status.name),
            tildeltEnhetsnr = oppgave.tildeltEnhetsnr,
            opprettetAvEnhetsnr = oppgave.opprettetAvEnhetsnr,
            endretAvEnhetsnr = oppgave.endretAvEnhetsnr,
            tema = oppgave.tema,
            temagruppe = oppgave.temagruppe,
            behandlingstema = oppgave.behandlingstema,
            oppgavetype = oppgave.oppgavetype,
            behandlingstype = oppgave.behandlingstype,
            prioritet = Prioritet.valueOf(oppgave.prioritet.name),
            tilordnetRessurs = oppgave.tilordnetRessurs,
            beskrivelse = oppgave.beskrivelse,
            fristFerdigstillelse = oppgave.fristFerdigstillelse,
            aktivDato = oppgave.aktivDato,
            opprettetAv = oppgave.opprettetAv,
            endretAv = oppgave.endretAv,
            opprettetTidspunkt = oppgave.opprettetTidspunkt,
            endretTidspunkt = oppgave.endretTidspunkt,
            ferdigstiltTidspunkt = oppgave.ferdigstiltTidspunkt,
            behandlesAvApplikasjon = oppgave.behandlesAvApplikasjon,
            journalpostkilde = oppgave.journalpostkilde,
            ident = Ident(
                id = oppgave.ident.id,
                identType = IdentType.valueOf(oppgave.ident.identType.name),
                verdi = oppgave.ident.verdi,
                folkeregisterident = oppgave.ident.folkeregisterident,
                registrertDato = null
            ),
            metadata = oppgave.metadata?.map { (k, v) ->
                MetadataNoekkel.valueOf(k.name) to v
            }?.toMap() ?: emptyMap()
        )
    }

}
