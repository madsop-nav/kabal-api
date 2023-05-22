package no.nav.klage.oppgave.api.controller

import no.nav.klage.oppgave.service.AdminService
import no.nav.klage.oppgave.service.FullmektigSearchService
import no.nav.klage.oppgave.util.TokenUtil
import no.nav.klage.oppgave.util.getLogger
import no.nav.security.token.support.core.api.Unprotected
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.*

@Profile("dev-gcp")
@RestController
class DevOnlyAdminController(
    private val adminService: AdminService,
    private val tokenUtil: TokenUtil,
    private val fullmektigSearchService: FullmektigSearchService,
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    @Unprotected
    @GetMapping("/internal/kafkaadmin/refill", produces = ["application/json"])
    @ResponseStatus(HttpStatus.OK)
    fun resetElasticIndex() {
        try {
            logger.info("Syncing db with Kafka")
            adminService.syncKafkaWithDb()
        } catch (e: Exception) {
            logger.warn("Failed to resync db with Kafka")
            throw e
        }
    }

    @Unprotected
    @DeleteMapping("/internal/behandlinger/{id}", produces = ["application/json"])
    @ResponseStatus(HttpStatus.OK)
    fun deleteBehandling(@PathVariable("id") behandlingId: UUID) {
        try {
            logger.info("Delete behandling i dev")
            adminService.deleteBehandlingInDev(behandlingId)
        } catch (e: Exception) {
            logger.warn("Failed to delete behandling i dev")
            throw e
        }
    }

    @Unprotected
    @PostMapping("/internal/generatemissingankeitrygderettendev", produces = ["application/json"])
    @ResponseStatus(HttpStatus.OK)
    fun generateMissingAnkeITrygderetten() {
        logger.debug("generateMissingAnkeITrygderetten is called in dev")

        try {
            adminService.generateMissingAnkeITrygderetten()
        } catch (e: Exception) {
            logger.warn("Failed to generate missing AnkeITrygderetten", e)
            throw e
        }
    }

    @Unprotected
    @PostMapping("/internal/isskjermetdev")
    @ResponseStatus(HttpStatus.OK)
    fun isSkjermet(
        @RequestBody input: Fnr
    ) {
        logger.debug("isSkjermet in dev is called")

        adminService.isSkjermet(input.fnr)
    }

    @Unprotected
    @PostMapping("/internal/migratedvh")
    fun migrateDvhEvents() {
        logger.debug("migrateDvhEvents is called")

        try {
            adminService.migrateDvhEvents()
        } catch (e: Exception) {
            logger.warn("Failed to migrate DVH events", e)
            throw e
        }
    }

    @Unprotected
    @GetMapping("/internal/mytoken")
    fun getToken(): String {
        return tokenUtil.getAccessTokenFrontendSent()
    }

    data class Fnr(val fnr: String)
}