package no.nav.klage.oppgave.service

import no.nav.klage.oppgave.domain.klage.KLAGEENHET_PREFIX
import no.nav.klage.oppgave.domain.klage.Klagebehandling
import no.nav.klage.oppgave.domain.klage.Mottak
import no.nav.klage.oppgave.domain.klage.Oppgavereferanse
import no.nav.klage.oppgave.domain.kodeverk.Kilde
import no.nav.klage.oppgave.domain.kodeverk.Sakstype
import no.nav.klage.oppgave.domain.kodeverk.Tema
import no.nav.klage.oppgave.domain.oppgavekopi.IdentType
import no.nav.klage.oppgave.domain.oppgavekopi.OppgaveKopiVersjon
import no.nav.klage.oppgave.domain.oppgavekopi.VersjonIdent
import no.nav.klage.oppgave.repositories.KlagebehandlingRepository
import no.nav.klage.oppgave.util.getLogger
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
@Transactional
class KlagebehandlingService(
    private val klagebehandlingRepository: KlagebehandlingRepository,
    private val hjemmelService: HjemmelService
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    fun getKlagebehandlingByOppgaveId(oppgaveId: Long): Klagebehandling {
        return klagebehandlingRepository.findByOppgavereferanserOppgaveId(oppgaveId)
            ?: throw RuntimeException("klagebehandling not found")
    }

    fun fetchKlagesakForOppgaveKopi(oppgaveId: Long): Klagebehandling? =
        klagebehandlingRepository.findByOppgavereferanserOppgaveId(oppgaveId)

    fun connectOppgaveKopiToKlagebehandling(oppgaveKopierOrdererByVersion: List<OppgaveKopiVersjon>) {
        val nyesteVersjon = oppgaveKopierOrdererByVersion.first()
        val klagesak = fetchKlagesakForOppgaveKopi(nyesteVersjon.id)
        //TODO: Oppdatere mottak selv om Klagebehandling har blitt laget tidligere? Hvor avslutter man oppgaven, hos oss eller i Gosys?
        if (klagesak == null && nyesteVersjon.tildeltEnhetsnr.startsWith(KLAGEENHET_PREFIX)) {
            requireNotNull(nyesteVersjon.ident)
            requireNotNull(nyesteVersjon.behandlingstype)

            val mottak: Mottak = Mottak(
                tema = mapTema(nyesteVersjon.tema),
                sakstype = mapSakstype(nyesteVersjon.behandlingstype),
                referanseId = nyesteVersjon.saksreferanse,
                foedselsnummer = nyesteVersjon.ident.folkeregisterident,
                organisasjonsnummer = mapOrganisasjonsnummer(nyesteVersjon.ident),
                hjemmelListe = mapHjemler(nyesteVersjon),
                avsenderSaksbehandlerident = mapAvsenderSaksbehandler(oppgaveKopierOrdererByVersion),
                avsenderEnhet = mapAvsenderEnhet(oppgaveKopierOrdererByVersion),
                oversendtKaEnhet = mapMottakerEnhet(oppgaveKopierOrdererByVersion),
                oversendtKaDato = mapOversendtKaDato(oppgaveKopierOrdererByVersion),
                fristFraFoersteinstans = nyesteVersjon.fristFerdigstillelse,
                kilde = Kilde.OPPGAVE
            )

            val createdKlagebehandling = klagebehandlingRepository.save(
                Klagebehandling(
                    tema = mottak.tema,
                    sakstype = mottak.sakstype,
                    foedselsnummer = mottak.foedselsnummer,
                    hjemler = mottak.hjemler().map { hjemmelService.generateHjemmelFromText(it) }.toMutableList(),
                    tildeltEnhet = mottak.oversendtKaEnhet,
                    mottattKlageinstans = mottak.oversendtKaDato ?: LocalDate.now(),
                    frist = mottak.fristFraFoersteinstans,
                    oppgavereferanser = mutableListOf(
                        Oppgavereferanse(
                            oppgaveId = nyesteVersjon.id
                        )
                    ),
                    mottak = mottak,
                    kilde = Kilde.OPPGAVE
                )
            )
            logger.debug("Created behandling ${createdKlagebehandling.id} with mottak ${mottak.id} for oppgave ${nyesteVersjon.id}")
        }
    }

    private fun mapHjemler(oppgaveKopiVersjon: OppgaveKopiVersjon) =
        hjemmelService.getHjemmelFromOppgaveKopiVersjon(oppgaveKopiVersjon)

    private fun mapOrganisasjonsnummer(ident: VersjonIdent) =
        if (ident.identType == IdentType.ORGNR) {
            ident.verdi
        } else {
            null
        }

    private fun mapSakstype(behandlingstype: String): Sakstype = Sakstype.of(behandlingstype)

    private fun mapTema(tema: String): Tema = Tema.of(tema)

    private fun mapOversendtKaDato(oppgaveKopiVersjoner: List<OppgaveKopiVersjon>): LocalDate? =
        findFirstVersionWhereTildeltEnhetIsKA(oppgaveKopiVersjoner).endretTidspunkt?.toLocalDate()

    private fun mapMottakerEnhet(oppgaveKopiVersjoner: List<OppgaveKopiVersjon>): String =
        findFirstVersionWhereTildeltEnhetIsKA(oppgaveKopiVersjoner).tildeltEnhetsnr

    private fun mapAvsenderEnhet(oppgaveKopiVersjoner: List<OppgaveKopiVersjon>): String? =
        findLastVersionWhereTildeltEnhetIsNotKA(oppgaveKopiVersjoner)?.tildeltEnhetsnr

    private fun mapAvsenderSaksbehandler(oppgaveKopiVersjoner: List<OppgaveKopiVersjon>): String? =
        findLastVersionWhereTildeltEnhetIsNotKAAndSaksbehandlerIsNotNull(oppgaveKopiVersjoner)?.tilordnetRessurs

    private fun findFirstVersionWhereTildeltEnhetIsKA(oppgaveKopiVersjoner: List<OppgaveKopiVersjon>): OppgaveKopiVersjon =
        oppgaveKopiVersjoner.last { it.tildeltEnhetsnr.startsWith(KLAGEENHET_PREFIX) }

    private fun findLastVersionWhereTildeltEnhetIsNotKA(oppgaveKopiVersjoner: List<OppgaveKopiVersjon>): OppgaveKopiVersjon? =
        oppgaveKopiVersjoner.firstOrNull { !it.tildeltEnhetsnr.startsWith(KLAGEENHET_PREFIX) }

    private fun findLastVersionWhereTildeltEnhetIsNotKAAndSaksbehandlerIsNotNull(oppgaveKopiVersjoner: List<OppgaveKopiVersjon>): OppgaveKopiVersjon? =
        oppgaveKopiVersjoner.firstOrNull { !it.tildeltEnhetsnr.startsWith(KLAGEENHET_PREFIX) && it.tilordnetRessurs != null }

}
