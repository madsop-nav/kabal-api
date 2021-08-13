package no.nav.klage.oppgave.domain.klage

import no.nav.klage.oppgave.domain.events.KlagebehandlingEndretEvent
import no.nav.klage.oppgave.domain.kodeverk.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

object KlagebehandlingAggregatFunctions {

    fun Klagebehandling.setTildeling(
        nyVerdiSaksbehandlerident: String?,
        nyVerdiEnhet: String?,
        saksbehandlerident: String
    ): KlagebehandlingEndretEvent {
        val gammelVerdiSaksbehandlerident = tildeling?.saksbehandlerident
        val gammelVerdiEnhet = tildeling?.enhet
        val gammelVerdiTidspunkt = tildeling?.tidspunkt
        val tidspunkt = LocalDateTime.now()
        if (tildeling != null) {
            tildelingHistorikk.add(TildelingHistorikk(tildeling = tildeling!!.copy()))
        }
        tildeling = Tildeling(nyVerdiSaksbehandlerident, nyVerdiEnhet, tidspunkt)
        modified = tidspunkt

        val endringslogginnslag = mutableListOf<Endringslogginnslag>()

        endringslogg(
            saksbehandlerident,
            Felt.TILDELT,
            gammelVerdiTidspunkt?.format(DateTimeFormatter.ISO_LOCAL_DATE),
            tidspunkt.format(DateTimeFormatter.ISO_LOCAL_DATE),
            tidspunkt
        )?.let { endringslogginnslag.add(it) }

        endringslogg(
            saksbehandlerident,
            Felt.TILDELT_SAKSBEHANDLERIDENT,
            gammelVerdiSaksbehandlerident,
            nyVerdiSaksbehandlerident,
            tidspunkt
        )?.let { endringslogginnslag.add(it) }

        endringslogg(saksbehandlerident, Felt.TILDELT_ENHET, gammelVerdiEnhet, nyVerdiEnhet, tidspunkt)
            ?.let { endringslogginnslag.add(it) }

        return KlagebehandlingEndretEvent(klagebehandling = this, endringslogginnslag = endringslogginnslag)
    }

    fun Klagebehandling.setType(
        nyVerdi: Type,
        saksbehandlerident: String
    ): KlagebehandlingEndretEvent {
        val gammelVerdi = type
        val tidspunkt = LocalDateTime.now()
        type = nyVerdi
        modified = tidspunkt
        val endringslogg =
            endringslogg(saksbehandlerident, Felt.SAKSTYPE, gammelVerdi.id.toString(), nyVerdi.id.toString(), tidspunkt)
        return KlagebehandlingEndretEvent(klagebehandling = this, endringslogginnslag = listOfNotNull(endringslogg))
    }

    fun Klagebehandling.setTema(
        nyVerdi: Tema,
        saksbehandlerident: String
    ): KlagebehandlingEndretEvent {
        val gammelVerdi = tema
        val tidspunkt = LocalDateTime.now()
        tema = nyVerdi
        modified = tidspunkt
        val endringslogg =
            endringslogg(saksbehandlerident, Felt.TEMA, gammelVerdi.id.toString(), nyVerdi.id.toString(), tidspunkt)
        return KlagebehandlingEndretEvent(klagebehandling = this, endringslogginnslag = listOfNotNull(endringslogg))
    }

    fun Klagebehandling.setInnsendt(
        nyVerdi: LocalDate,
        saksbehandlerident: String
    ): KlagebehandlingEndretEvent {
        val gammelVerdi = innsendt
        val tidspunkt = LocalDateTime.now()
        innsendt = nyVerdi
        modified = tidspunkt
        val endringslogg =
            endringslogg(
                saksbehandlerident,
                Felt.DATO_KLAGE_INNSENDT,
                gammelVerdi?.format(DateTimeFormatter.ISO_LOCAL_DATE),
                nyVerdi.format(DateTimeFormatter.ISO_LOCAL_DATE),
                tidspunkt
            )
        return KlagebehandlingEndretEvent(klagebehandling = this, endringslogginnslag = listOfNotNull(endringslogg))
    }

    fun Klagebehandling.setMottattFoersteinstans(
        nyVerdi: LocalDate,
        saksbehandlerident: String
    ): KlagebehandlingEndretEvent {
        val gammelVerdi = mottattFoersteinstans
        val tidspunkt = LocalDateTime.now()
        mottattFoersteinstans = nyVerdi
        modified = tidspunkt
        val endringslogg =
            endringslogg(
                saksbehandlerident,
                Felt.DATO_MOTTATT_FOERSTEINSTANS,
                gammelVerdi?.format(DateTimeFormatter.ISO_LOCAL_DATE),
                nyVerdi.format(DateTimeFormatter.ISO_LOCAL_DATE),
                tidspunkt
            )
        return KlagebehandlingEndretEvent(klagebehandling = this, endringslogginnslag = listOfNotNull(endringslogg))
    }

    fun Klagebehandling.setMottattKlageinstans(
        nyVerdi: LocalDateTime,
        saksbehandlerident: String
    ): KlagebehandlingEndretEvent {
        val gammelVerdi = mottattKlageinstans
        val tidspunkt = LocalDateTime.now()
        mottattKlageinstans = nyVerdi
        modified = tidspunkt
        val endringslogg =
            endringslogg(
                saksbehandlerident,
                Felt.DATO_OVERSENDT_KA,
                gammelVerdi.format(DateTimeFormatter.ISO_LOCAL_DATE),
                nyVerdi.format(DateTimeFormatter.ISO_LOCAL_DATE),
                tidspunkt
            )
        return KlagebehandlingEndretEvent(klagebehandling = this, endringslogginnslag = listOfNotNull(endringslogg))
    }

    fun Klagebehandling.setFrist(
        nyVerdi: LocalDate,
        saksbehandlerident: String
    ): KlagebehandlingEndretEvent {
        val gammelVerdi = frist
        val tidspunkt = LocalDateTime.now()
        frist = nyVerdi
        modified = tidspunkt
        val endringslogg =
            endringslogg(
                saksbehandlerident,
                Felt.DATO_FRIST,
                gammelVerdi?.format(DateTimeFormatter.ISO_LOCAL_DATE),
                nyVerdi.format(DateTimeFormatter.ISO_LOCAL_DATE),
                tidspunkt
            )
        return KlagebehandlingEndretEvent(klagebehandling = this, endringslogginnslag = listOfNotNull(endringslogg))
    }

    fun Klagebehandling.setAvsenderSaksbehandleridentFoersteinstans(
        nyVerdi: String,
        saksbehandlerident: String
    ): KlagebehandlingEndretEvent {
        val gammelVerdi = avsenderSaksbehandleridentFoersteinstans
        val tidspunkt = LocalDateTime.now()
        avsenderSaksbehandleridentFoersteinstans = nyVerdi
        modified = tidspunkt
        val endringslogg =
            endringslogg(
                saksbehandlerident,
                Felt.AVSENDER_SAKSBEHANDLERIDENT,
                gammelVerdi,
                nyVerdi,
                tidspunkt
            )
        return KlagebehandlingEndretEvent(klagebehandling = this, endringslogginnslag = listOfNotNull(endringslogg))
    }

    fun Klagebehandling.setAvsenderEnhetFoersteinstans(
        nyVerdi: String,
        saksbehandlerident: String
    ): KlagebehandlingEndretEvent {
        val gammelVerdi = avsenderEnhetFoersteinstans
        val tidspunkt = LocalDateTime.now()
        avsenderEnhetFoersteinstans = nyVerdi
        modified = tidspunkt
        val endringslogg =
            endringslogg(
                saksbehandlerident,
                Felt.AVSENDER_ENHET,
                gammelVerdi,
                nyVerdi,
                tidspunkt
            )
        return KlagebehandlingEndretEvent(klagebehandling = this, endringslogginnslag = listOfNotNull(endringslogg))
    }

    fun Klagebehandling.setMedunderskriverident(
        nyVerdiMedunderskriverident: String,
        saksbehandlerident: String
    ): KlagebehandlingEndretEvent {
        val gammelVerdiMedunderskriverident = medunderskriver?.saksbehandlerident
        val gammelVerdiTidspunkt = medunderskriver?.tidspunkt
        val tidspunkt = LocalDateTime.now()
        if (medunderskriver != null) {
            medunderskriverHistorikk.add(MedunderskriverHistorikk(medunderskriver = medunderskriver!!.copy()))
        }
        medunderskriver = MedunderskriverTildeling(nyVerdiMedunderskriverident, tidspunkt)
        modified = tidspunkt

        val endringslogginnslag = mutableListOf<Endringslogginnslag>()

        endringslogg(
            saksbehandlerident,
            Felt.OVERSENDT_MEDUNDERSKRIVER,
            gammelVerdiTidspunkt?.format(DateTimeFormatter.ISO_LOCAL_DATE),
            tidspunkt.format(DateTimeFormatter.ISO_LOCAL_DATE),
            tidspunkt
        )?.let { endringslogginnslag.add(it) }

        endringslogg(
            saksbehandlerident,
            Felt.MEDUNDERSKRIVERIDENT,
            gammelVerdiMedunderskriverident,
            nyVerdiMedunderskriverident,
            tidspunkt
        )?.let { endringslogginnslag.add(it) }

        return KlagebehandlingEndretEvent(klagebehandling = this, endringslogginnslag = endringslogginnslag)
    }

    fun Klagebehandling.setGrunnInVedtak(
        vedtakId: UUID,
        nyVerdi: Grunn?,
        saksbehandlerident: String
    ): KlagebehandlingEndretEvent {
        val vedtak = this.getVedtak(vedtakId)
        val gammelVerdi = vedtak.grunn
        val tidspunkt = LocalDateTime.now()
        vedtak.grunn = nyVerdi
        vedtak.modified = tidspunkt
        modified = tidspunkt
        val endringslogg =
            endringslogg(
                saksbehandlerident,
                Felt.OMGJOERINGSGRUNN,
                gammelVerdi?.id.toString(),
                nyVerdi?.id.toString(),
                tidspunkt
            )
        return KlagebehandlingEndretEvent(klagebehandling = this, endringslogginnslag = listOfNotNull(endringslogg))
    }

    fun Klagebehandling.setHjemlerInVedtak(
        vedtakId: UUID,
        nyVerdi: Set<Hjemmel>,
        saksbehandlerident: String
    ): KlagebehandlingEndretEvent {
        val vedtak = this.getVedtak(vedtakId)
        val gammelVerdi = vedtak.hjemler
        val tidspunkt = LocalDateTime.now()
        vedtak.hjemler = nyVerdi.toMutableSet()
        vedtak.modified = tidspunkt
        modified = tidspunkt
        val endringslogg =
            endringslogg(
                saksbehandlerident,
                Felt.HJEMLER_I_VEDTAK,
                gammelVerdi.joinToString { it.id },
                nyVerdi.joinToString { it.id },
                tidspunkt
            )
        return KlagebehandlingEndretEvent(klagebehandling = this, endringslogginnslag = listOfNotNull(endringslogg))
    }

    fun Klagebehandling.setKvalitetsvurderingInkluderteDatoForKlage(
        nyVerdi: Boolean?,
        saksbehandlerident: String
    ): KlagebehandlingEndretEvent {
        if (kvalitetsvurdering == null) {
            kvalitetsvurdering = Kvalitetsvurdering()
        }
        val gammelVerdi = kvalitetsvurdering!!.inkluderteDatoForKlage
        val tidspunkt = LocalDateTime.now()
        kvalitetsvurdering!!.inkluderteDatoForKlage = nyVerdi
        kvalitetsvurdering!!.modified = tidspunkt
        modified = tidspunkt
        val endringslogg =
            endringslogg(
                saksbehandlerident,
                Felt.INKLUDERTE_DATO_FOR_KLAGE,
                gammelVerdi.toString(),
                nyVerdi.toString(),
                tidspunkt
            )
        return KlagebehandlingEndretEvent(klagebehandling = this, endringslogginnslag = listOfNotNull(endringslogg))
    }

    fun Klagebehandling.setKvalitetsvurderingInkluderteDatoForVedtak(
        nyVerdi: Boolean?,
        saksbehandlerident: String
    ): KlagebehandlingEndretEvent {
        if (kvalitetsvurdering == null) {
            kvalitetsvurdering = Kvalitetsvurdering()
        }
        val gammelVerdi = kvalitetsvurdering!!.inkluderteDatoForVedtak
        val tidspunkt = LocalDateTime.now()
        kvalitetsvurdering!!.inkluderteDatoForVedtak = nyVerdi
        kvalitetsvurdering!!.modified = tidspunkt
        modified = tidspunkt
        val endringslogg =
            endringslogg(
                saksbehandlerident,
                Felt.INKLUDERTE_DATO_FOR_VEDTAK,
                gammelVerdi.toString(),
                nyVerdi.toString(),
                tidspunkt
            )
        return KlagebehandlingEndretEvent(klagebehandling = this, endringslogginnslag = listOfNotNull(endringslogg))
    }

    fun Klagebehandling.setKvalitetsvurderingOversendelsesbrevBra(
        nyVerdi: Boolean?,
        saksbehandlerident: String
    ): KlagebehandlingEndretEvent {
        if (kvalitetsvurdering == null) {
            kvalitetsvurdering = Kvalitetsvurdering()
        }
        val gammelVerdi = kvalitetsvurdering!!.oversendelsesbrevBra
        val tidspunkt = LocalDateTime.now()
        kvalitetsvurdering!!.oversendelsesbrevBra = nyVerdi
        kvalitetsvurdering!!.modified = tidspunkt
        modified = tidspunkt
        val endringslogg =
            endringslogg(
                saksbehandlerident,
                Felt.OVERSENDELSESBREV_BRA,
                gammelVerdi.toString(),
                nyVerdi.toString(),
                tidspunkt
            )
        return KlagebehandlingEndretEvent(klagebehandling = this, endringslogginnslag = listOfNotNull(endringslogg))
    }

    fun Klagebehandling.setKvalitetsvurderingKvalitetsavvikOversendelsesbrev(
        nyVerdi: Set<KvalitetsavvikOversendelsesbrev>,
        saksbehandlerident: String
    ): KlagebehandlingEndretEvent {
        if (kvalitetsvurdering == null) {
            kvalitetsvurdering = Kvalitetsvurdering()
        }
        val gammelVerdi = kvalitetsvurdering!!.kvalitetsavvikOversendelsesbrev
        val tidspunkt = LocalDateTime.now()
        kvalitetsvurdering!!.kvalitetsavvikOversendelsesbrev = nyVerdi.toMutableSet()
        kvalitetsvurdering!!.modified = tidspunkt
        modified = tidspunkt
        val endringslogg =
            endringslogg(
                saksbehandlerident,
                Felt.KVALITETSAVVIK_OVERSENDELSESBREV,
                gammelVerdi.joinToString { it.id },
                nyVerdi.joinToString { it.id },
                tidspunkt
            )
        return KlagebehandlingEndretEvent(klagebehandling = this, endringslogginnslag = listOfNotNull(endringslogg))
    }

    fun Klagebehandling.setKvalitetsvurderingKommentarOversendelsesbrev(
        nyVerdi: String?,
        saksbehandlerident: String
    ): KlagebehandlingEndretEvent {
        if (kvalitetsvurdering == null) {
            kvalitetsvurdering = Kvalitetsvurdering()
        }
        val gammelVerdi = kvalitetsvurdering!!.kommentarOversendelsesbrev
        val tidspunkt = LocalDateTime.now()
        kvalitetsvurdering!!.kommentarOversendelsesbrev = nyVerdi
        kvalitetsvurdering!!.modified = tidspunkt
        modified = tidspunkt
        val endringslogg =
            endringslogg(
                saksbehandlerident,
                Felt.KOMMENTAR_OVERSENDELSESBREV,
                gammelVerdi,
                nyVerdi,
                tidspunkt
            )
        return KlagebehandlingEndretEvent(klagebehandling = this, endringslogginnslag = listOfNotNull(endringslogg))
    }

    fun Klagebehandling.setKvalitetsvurderingUtredningBra(
        nyVerdi: Boolean?,
        saksbehandlerident: String
    ): KlagebehandlingEndretEvent {
        if (kvalitetsvurdering == null) {
            kvalitetsvurdering = Kvalitetsvurdering()
        }
        val gammelVerdi = kvalitetsvurdering!!.utredningBra
        val tidspunkt = LocalDateTime.now()
        kvalitetsvurdering!!.utredningBra = nyVerdi
        kvalitetsvurdering!!.modified = tidspunkt
        modified = tidspunkt
        val endringslogg =
            endringslogg(
                saksbehandlerident,
                Felt.UTREDNING_BRA,
                gammelVerdi.toString(),
                nyVerdi.toString(),
                tidspunkt
            )
        return KlagebehandlingEndretEvent(klagebehandling = this, endringslogginnslag = listOfNotNull(endringslogg))
    }

    fun Klagebehandling.setKvalitetsvurderingKvalitetsavvikUtredning(
        nyVerdi: Set<KvalitetsavvikUtredning>,
        saksbehandlerident: String
    ): KlagebehandlingEndretEvent {
        if (kvalitetsvurdering == null) {
            kvalitetsvurdering = Kvalitetsvurdering()
        }
        val gammelVerdi = kvalitetsvurdering!!.kvalitetsavvikUtredning
        val tidspunkt = LocalDateTime.now()
        kvalitetsvurdering!!.kvalitetsavvikUtredning = nyVerdi.toMutableSet()
        kvalitetsvurdering!!.modified = tidspunkt
        modified = tidspunkt
        val endringslogg =
            endringslogg(
                saksbehandlerident,
                Felt.KVALITETSAVVIK_UTREDNING,
                gammelVerdi.joinToString { it.id },
                nyVerdi.joinToString { it.id },
                tidspunkt
            )
        return KlagebehandlingEndretEvent(klagebehandling = this, endringslogginnslag = listOfNotNull(endringslogg))
    }

    fun Klagebehandling.setKvalitetsvurderingKommentarUtredning(
        nyVerdi: String?,
        saksbehandlerident: String
    ): KlagebehandlingEndretEvent {
        if (kvalitetsvurdering == null) {
            kvalitetsvurdering = Kvalitetsvurdering()
        }
        val gammelVerdi = kvalitetsvurdering!!.kommentarUtredning
        val tidspunkt = LocalDateTime.now()
        kvalitetsvurdering!!.kommentarUtredning = nyVerdi
        kvalitetsvurdering!!.modified = tidspunkt
        modified = tidspunkt
        val endringslogg =
            endringslogg(
                saksbehandlerident,
                Felt.KOMMENTAR_UTREDNING,
                gammelVerdi,
                nyVerdi,
                tidspunkt
            )
        return KlagebehandlingEndretEvent(klagebehandling = this, endringslogginnslag = listOfNotNull(endringslogg))
    }

    fun Klagebehandling.setKvalitetsvurderingVedtakBra(
        nyVerdi: Boolean?,
        saksbehandlerident: String
    ): KlagebehandlingEndretEvent {
        if (kvalitetsvurdering == null) {
            kvalitetsvurdering = Kvalitetsvurdering()
        }
        val gammelVerdi = kvalitetsvurdering!!.vedtakBra
        val tidspunkt = LocalDateTime.now()
        kvalitetsvurdering!!.vedtakBra = nyVerdi
        kvalitetsvurdering!!.modified = tidspunkt
        modified = tidspunkt
        val endringslogg =
            endringslogg(
                saksbehandlerident,
                Felt.VEDTAK_BRA,
                gammelVerdi.toString(),
                nyVerdi.toString(),
                tidspunkt
            )
        return KlagebehandlingEndretEvent(klagebehandling = this, endringslogginnslag = listOfNotNull(endringslogg))
    }

    fun Klagebehandling.setKvalitetsvurderingKvalitetsavvikVedtak(
        nyVerdi: Set<KvalitetsavvikVedtak>,
        saksbehandlerident: String
    ): KlagebehandlingEndretEvent {
        if (kvalitetsvurdering == null) {
            kvalitetsvurdering = Kvalitetsvurdering()
        }
        val gammelVerdi = kvalitetsvurdering!!.kvalitetsavvikVedtak
        val tidspunkt = LocalDateTime.now()
        kvalitetsvurdering!!.kvalitetsavvikVedtak = nyVerdi.toMutableSet()
        kvalitetsvurdering!!.modified = tidspunkt
        modified = tidspunkt
        val endringslogg =
            endringslogg(
                saksbehandlerident,
                Felt.KVALITETSAVVIK_VEDTAK,
                gammelVerdi.joinToString { it.id },
                nyVerdi.joinToString { it.id },
                tidspunkt
            )
        return KlagebehandlingEndretEvent(klagebehandling = this, endringslogginnslag = listOfNotNull(endringslogg))
    }

    fun Klagebehandling.setKvalitetsvurderingKommentarVedtak(
        nyVerdi: String?,
        saksbehandlerident: String
    ): KlagebehandlingEndretEvent {
        if (kvalitetsvurdering == null) {
            kvalitetsvurdering = Kvalitetsvurdering()
        }
        val gammelVerdi = kvalitetsvurdering!!.kommentarVedtak
        val tidspunkt = LocalDateTime.now()
        kvalitetsvurdering!!.kommentarVedtak = nyVerdi
        kvalitetsvurdering!!.modified = tidspunkt
        modified = tidspunkt
        val endringslogg =
            endringslogg(
                saksbehandlerident,
                Felt.KOMMENTAR_VEDTAK,
                gammelVerdi,
                nyVerdi,
                tidspunkt
            )
        return KlagebehandlingEndretEvent(klagebehandling = this, endringslogginnslag = listOfNotNull(endringslogg))
    }

    fun Klagebehandling.setKvalitetsvurderingAvvikStorKonsekvens(
        nyVerdi: Boolean?,
        saksbehandlerident: String
    ): KlagebehandlingEndretEvent {
        if (kvalitetsvurdering == null) {
            kvalitetsvurdering = Kvalitetsvurdering()
        }
        val gammelVerdi = kvalitetsvurdering!!.avvikStorKonsekvens
        val tidspunkt = LocalDateTime.now()
        kvalitetsvurdering!!.avvikStorKonsekvens = nyVerdi
        kvalitetsvurdering!!.modified = tidspunkt
        modified = tidspunkt
        val endringslogg =
            endringslogg(
                saksbehandlerident,
                Felt.AVVIK_STOR_KONSEKVENS,
                gammelVerdi.toString(),
                nyVerdi.toString(),
                tidspunkt
            )
        return KlagebehandlingEndretEvent(klagebehandling = this, endringslogginnslag = listOfNotNull(endringslogg))
    }

    fun Klagebehandling.setBrukSomEksempelIOpplaering(
        nyVerdi: Boolean?,
        saksbehandlerident: String
    ): KlagebehandlingEndretEvent {
        if (kvalitetsvurdering == null) {
            kvalitetsvurdering = Kvalitetsvurdering()
        }
        val gammelVerdi = kvalitetsvurdering!!.brukSomEksempelIOpplaering
        val tidspunkt = LocalDateTime.now()
        kvalitetsvurdering!!.brukSomEksempelIOpplaering = nyVerdi
        kvalitetsvurdering!!.modified = tidspunkt
        modified = tidspunkt
        val endringslogg =
            endringslogg(
                saksbehandlerident,
                Felt.BRUK_SOM_EKSEMPEL_I_OPPLAERING,
                gammelVerdi.toString(),
                nyVerdi.toString(),
                tidspunkt
            )
        return KlagebehandlingEndretEvent(klagebehandling = this, endringslogginnslag = listOfNotNull(endringslogg))
    }

    fun Klagebehandling.setUtfallInVedtak(
        vedtakId: UUID,
        nyVerdi: Utfall?,
        saksbehandlerident: String
    ): KlagebehandlingEndretEvent {
        val vedtak = this.getVedtak(vedtakId)
        val gammelVerdi = vedtak.utfall
        val tidspunkt = LocalDateTime.now()
        vedtak.utfall = nyVerdi
        vedtak.modified = tidspunkt
        modified = tidspunkt
        val endringslogg =
            endringslogg(
                saksbehandlerident,
                Felt.UTFALL,
                gammelVerdi.toString(),
                nyVerdi.toString(),
                tidspunkt
            )
        return KlagebehandlingEndretEvent(klagebehandling = this, endringslogginnslag = listOfNotNull(endringslogg))
    }

    fun Klagebehandling.setJournalpostIdOgOpplastetInVedtak(
        vedtakId: UUID,
        nyVerdi: String?,
        saksbehandlerident: String
    ): KlagebehandlingEndretEvent {
        val vedtak = this.getVedtak(vedtakId)
        val gammelVerdi = vedtak.journalpostId
        val tidspunkt = LocalDateTime.now()
        val gammelVerdiOpplastet = vedtak.opplastet
        vedtak.journalpostId = nyVerdi
        vedtak.modified = tidspunkt
        vedtak.opplastet = if (nyVerdi == null) null else tidspunkt
        modified = tidspunkt
        val endringslogg = listOfNotNull(
            endringslogg(
                saksbehandlerident,
                Felt.JOURNALPOST_I_VEDTAK,
                gammelVerdi,
                nyVerdi,
                tidspunkt
            ),
            endringslogg(
                saksbehandlerident,
                Felt.OPPLASTET_I_VEDTAK,
                gammelVerdiOpplastet.toString(),
                tidspunkt.toString(),
                tidspunkt
            )
        )
        return KlagebehandlingEndretEvent(klagebehandling = this, endringslogginnslag = endringslogg)
    }

    fun Klagebehandling.setVedtakFerdigDistribuert(
        vedtakId: UUID,
        saksbehandlerident: String
    ): KlagebehandlingEndretEvent {
        val vedtak = this.getVedtak(vedtakId)
        val gammelVerdi = vedtak.ferdigDistribuert
        val tidspunkt = LocalDateTime.now()
        val nyVerdi = tidspunkt
        vedtak.ferdigDistribuert = nyVerdi
        vedtak.modified = tidspunkt
        modified = tidspunkt
        val endringslogg =
            endringslogg(
                saksbehandlerident,
                Felt.VEDTAK_DISTRIBUERT,
                gammelVerdi.toString(),
                nyVerdi.toString(),
                tidspunkt
            )
        return KlagebehandlingEndretEvent(klagebehandling = this, endringslogginnslag = listOfNotNull(endringslogg))
    }

    fun Klagebehandling.setVedtakFerdigstiltIJoark(
        vedtakId: UUID,
        saksbehandlerident: String
    ): KlagebehandlingEndretEvent {
        val vedtak = this.getVedtak(vedtakId)
        val gammelVerdi = vedtak.ferdigstiltIJoark
        val tidspunkt = LocalDateTime.now()
        val nyVerdi = tidspunkt
        vedtak.ferdigstiltIJoark = nyVerdi
        vedtak.modified = tidspunkt
        modified = tidspunkt
        val endringslogg =
            endringslogg(
                saksbehandlerident,
                Felt.VEDTAK_SLUTTFOERT,
                gammelVerdi.toString(),
                nyVerdi.toString(),
                tidspunkt
            )
        return KlagebehandlingEndretEvent(klagebehandling = this, endringslogginnslag = listOfNotNull(endringslogg))
    }

    fun Klagebehandling.setAvsluttetAvSaksbehandler(
        saksbehandlerident: String
    ): KlagebehandlingEndretEvent {
        val gammelVerdi = avsluttetAvSaksbehandler
        val tidspunkt = LocalDateTime.now()
        val nyVerdi = tidspunkt
        avsluttetAvSaksbehandler = nyVerdi
        modified = tidspunkt
        val endringslogg =
            endringslogg(
                saksbehandlerident,
                Felt.AVSLUTTET_AV_SAKSBEHANDLER,
                gammelVerdi.toString(),
                nyVerdi.toString(),
                tidspunkt
            )
        return KlagebehandlingEndretEvent(klagebehandling = this, endringslogginnslag = listOfNotNull(endringslogg))
    }

    fun Klagebehandling.setAvsluttet(
        saksbehandlerident: String
    ): KlagebehandlingEndretEvent {
        val gammelVerdi = avsluttet
        val tidspunkt = LocalDateTime.now()
        val nyVerdi = tidspunkt
        avsluttet = nyVerdi
        modified = tidspunkt
        val endringslogg =
            endringslogg(
                saksbehandlerident,
                Felt.AVSLUTTET,
                gammelVerdi.toString(),
                nyVerdi.toString(),
                tidspunkt
            )
        return KlagebehandlingEndretEvent(klagebehandling = this, endringslogginnslag = listOfNotNull(endringslogg))
    }

    fun Klagebehandling.setDokdistReferanseInVedtaksmottaker(
        vedtakId: UUID,
        mottakerId: UUID,
        nyVerdi: UUID,
        saksbehandlerident: String
    ): KlagebehandlingEndretEvent {
        val vedtak = this.getVedtak(vedtakId)
        val mottaker = vedtak.getMottaker(mottakerId)
        val gammelVerdi = mottaker.dokdistReferanse
        val tidspunkt = LocalDateTime.now()
        mottaker.dokdistReferanse = nyVerdi
        vedtak.modified = tidspunkt
        modified = tidspunkt
        val endringslogg =
            endringslogg(
                saksbehandlerident,
                Felt.DOKDIST_REFERANSE,
                gammelVerdi.toString(),
                nyVerdi.toString(),
                tidspunkt
            )
        return KlagebehandlingEndretEvent(klagebehandling = this, endringslogginnslag = listOfNotNull(endringslogg))
    }

    fun Klagebehandling.addSaksdokument(
        saksdokument: Saksdokument,
        saksbehandlerident: String
    ): KlagebehandlingEndretEvent? {
        if (saksdokumenter.none { it.journalpostId == saksdokument.journalpostId && it.dokumentInfoId == saksdokument.dokumentInfoId }) {
            val tidspunkt = LocalDateTime.now()
            saksdokumenter.add(saksdokument)
            modified = tidspunkt
            val endringslogg = Endringslogginnslag.endringslogg(
                saksbehandlerident,
                Felt.SAKSDOKUMENT,
                null,
                saksdokument.dokumentInfoId,
                id,
                tidspunkt
            )
            return KlagebehandlingEndretEvent(klagebehandling = this, endringslogginnslag = listOfNotNull(endringslogg))
        }
        return null
    }

    fun Klagebehandling.removeSaksdokument(
        saksdokument: Saksdokument,
        saksbehandlerident: String
    ): KlagebehandlingEndretEvent? {
        if (saksdokumenter.any { it.journalpostId == saksdokument.journalpostId && it.dokumentInfoId == saksdokument.dokumentInfoId }) {
            val tidspunkt = LocalDateTime.now()
            saksdokumenter.removeIf { it.journalpostId == saksdokument.journalpostId && it.dokumentInfoId == saksdokument.dokumentInfoId }
            modified = tidspunkt
            val endringslogg = Endringslogginnslag.endringslogg(
                saksbehandlerident,
                Felt.SAKSDOKUMENT,
                saksdokument.dokumentInfoId,
                null,
                id,
                tidspunkt
            )
            return KlagebehandlingEndretEvent(klagebehandling = this, endringslogginnslag = listOfNotNull(endringslogg))
        }
        return null
    }

    private fun Klagebehandling.endringslogg(
        saksbehandlerident: String,
        felt: Felt,
        fraVerdi: String?,
        tilVerdi: String?,
        tidspunkt: LocalDateTime
    ): Endringslogginnslag? {
        return Endringslogginnslag.endringslogg(
            saksbehandlerident,
            felt,
            fraVerdi,
            tilVerdi,
            this.id,
            tidspunkt
        )
    }

}