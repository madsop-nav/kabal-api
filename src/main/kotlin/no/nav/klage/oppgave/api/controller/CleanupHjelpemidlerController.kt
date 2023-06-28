package no.nav.klage.oppgave.api.controller

import no.nav.klage.oppgave.config.SecurityConfiguration
import no.nav.klage.oppgave.service.AdminService
import no.nav.klage.oppgave.service.InnloggetSaksbehandlerService
import no.nav.klage.oppgave.util.getLogger
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@ProtectedWithClaims(issuer = SecurityConfiguration.ISSUER_AAD)
class CleanupHjelpemidlerController(
    private val adminService: AdminService,
    private val innloggetSaksbehandlerService: InnloggetSaksbehandlerService,
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    @GetMapping("/internal/cleanup-hje", produces = ["application/json"])
    @ResponseStatus(HttpStatus.OK)
    fun cleanupHje() {
        try {
            logger.info("Cleanup HJE")
        } catch (e: Exception) {
            logger.warn("Failed to cleanup HJE")
            throw e
        }
    }


}