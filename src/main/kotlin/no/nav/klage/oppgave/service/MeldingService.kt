package no.nav.klage.oppgave.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import jakarta.persistence.EntityNotFoundException
import no.nav.klage.oppgave.api.mapper.MeldingMapper
import no.nav.klage.oppgave.domain.kafka.Event
import no.nav.klage.oppgave.domain.klage.Melding
import no.nav.klage.oppgave.exceptions.MeldingNotFoundException
import no.nav.klage.oppgave.exceptions.MissingTilgangException
import no.nav.klage.oppgave.repositories.BehandlingRepository
import no.nav.klage.oppgave.repositories.MeldingRepository
import no.nav.klage.oppgave.util.getLogger
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@Service
@Transactional
class MeldingService(
    private val meldingRepository: MeldingRepository,
    private val behandlingRepository: BehandlingRepository,
    private val kafkaInternalEventService: KafkaInternalEventService,
    private val meldingMapper: MeldingMapper,
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)

        val objectMapper: ObjectMapper = jacksonObjectMapper().registerModule(JavaTimeModule())
    }

    fun addMelding(
        behandlingId: UUID,
        innloggetIdent: String,
        text: String
    ): Melding {
        logger.debug("saving new melding by $innloggetIdent")
        return meldingRepository.save(
            Melding(
                text = text,
                behandlingId = behandlingId,
                saksbehandlerident = innloggetIdent,
                created = LocalDateTime.now()
            )
        ).also {
            publishInternalEvent(melding = it, type = "message_added")
        }
    }

    fun deleteMelding(
        behandlingId: UUID,
        innloggetIdent: String,
        meldingId: UUID
    ) {
        try {
            val melding = meldingRepository.getReferenceById(meldingId)
            validateRightsToDeleteMelding(melding, innloggetIdent)

            meldingRepository.delete(melding)

            logger.debug("melding ({}) deleted by {}", meldingId, innloggetIdent)

            publishInternalEvent(melding = melding, type = "message_deleted")
        } catch (enfe: EntityNotFoundException) {
            throw MeldingNotFoundException("couldn't find melding with id $meldingId")
        }
    }

    fun modifyMelding(
        behandlingId: UUID,
        innloggetIdent: String,
        meldingId: UUID,
        text: String
    ): Melding {
        try {
            val melding = meldingRepository.getReferenceById(meldingId)
            validateRightsToModifyMelding(melding, innloggetIdent)

            melding.text = text
            melding.modified = LocalDateTime.now()

            meldingRepository.save(melding)
            logger.debug("melding ({}) modified by {}", meldingId, innloggetIdent)

            publishInternalEvent(melding = melding, type = "message_modified")

            return melding
        } catch (enfe: EntityNotFoundException) {
            throw MeldingNotFoundException("couldn't find melding with id $meldingId")
        }
    }

    fun getMeldingerForBehandling(behandlingId: UUID) =
        meldingRepository.findByBehandlingIdOrderByCreatedDesc(behandlingId)

    private fun validateRightsToModifyMelding(melding: Melding, innloggetIdent: String) {
        if (melding.saksbehandlerident != innloggetIdent) {
            throw MissingTilgangException(
                "Saksbehandler ($innloggetIdent) is not the author of melding (${melding.id}), and is not allowed to modify it."
            )
        }
    }

    private fun validateRightsToDeleteMelding(melding: Melding, innloggetIdent: String) {
        val behandling = behandlingRepository.getReferenceById(melding.behandlingId)

        if (behandling.tildeling?.saksbehandlerident == innloggetIdent || melding.saksbehandlerident == innloggetIdent) {
            return
        } else {
            throw MissingTilgangException(
                "Saksbehandler ($innloggetIdent) is not allowed to delete melding ${melding.id}."
            )
        }
    }

    private fun publishInternalEvent(melding: Melding, type: String) {
        kafkaInternalEventService.publishEvent(
            Event(
                behandlingId = melding.behandlingId.toString(),
                name = type,
                id = melding.id.toString(),
                data = objectMapper.writeValueAsString(meldingMapper.toMeldingView(melding)),
            )
        )
    }
}