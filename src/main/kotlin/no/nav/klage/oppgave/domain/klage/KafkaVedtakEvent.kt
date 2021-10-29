package no.nav.klage.oppgave.domain.klage

import no.nav.klage.oppgave.domain.kodeverk.Utfall
import no.nav.klage.oppgave.domain.kodeverk.UtfallConverter
import no.nav.klage.oppgave.domain.kodeverk.UtsendingStatus
import no.nav.klage.oppgave.domain.kodeverk.UtsendingStatusConverter
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "kafka_vedtak_event", schema = "klage")
class KafkaVedtakEvent(
    @Id
    val id: UUID = UUID.randomUUID(),
    @Column(name = "kilde_referanse")
    var kildeReferanse: String,
    @Column(name = "kilde")
    var kilde: String,
    @Column(name = "utfall_id")
    @Convert(converter = UtfallConverter::class)
    var utfall: Utfall,
    @Column(name = "vedtaksbrev_referanse")
    var vedtaksbrevReferanse: String?,
    @Column(name = "kabal_referanse")
    var kabalReferanse: String,
    @Column(name = "status_id")
    @Convert(converter = UtsendingStatusConverter::class)
    var status: UtsendingStatus,
    @Column(name = "error_message")
    var errorMessage: String? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as KafkaVedtakEvent

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String {
        return "KafkaVedtakEvent(id=$id, kildeReferanse='$kildeReferanse', kilde='$kilde', utfall=$utfall, vedtaksbrevReferanse=$vedtaksbrevReferanse, kabalReferanse='$kabalReferanse', status=$status, errorMessage=$errorMessage)"
    }

}
