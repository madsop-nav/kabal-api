package no.nav.klage.oppgave.repositories

import jakarta.persistence.LockModeType
import no.nav.klage.kodeverk.Ytelse
import no.nav.klage.oppgave.domain.klage.Klagebehandling
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.stereotype.Repository
import java.util.*


@Repository
interface KlagebehandlingRepository : JpaRepository<Klagebehandling, UUID>, KlagebehandlingRepositoryCustom {

    fun findByMottakId(mottakId: UUID): Klagebehandling?

    fun findByIdAndAvsluttetIsNotNull(id: UUID): Klagebehandling?

    fun findByAvsluttetIsNotNullAndFeilregistreringIsNull(): List<Klagebehandling>

    fun findByKildeReferanseAndYtelseAndFeilregistreringIsNull(kildeReferanse: String, ytelse: Ytelse): Klagebehandling?

    fun findByKakaKvalitetsvurderingVersionIs(version: Int): List<Klagebehandling>

    @Deprecated("See getOne")
    @Lock(LockModeType.OPTIMISTIC_FORCE_INCREMENT)
    override fun getOne(id: UUID): Klagebehandling

}
