package no.nav.klage.oppgave.api.controller

import io.swagger.v3.oas.annotations.tags.Tag
import no.nav.klage.oppgave.api.view.FlowStateInput
import no.nav.klage.oppgave.api.view.FlowStateView
import no.nav.klage.oppgave.api.view.RolView
import no.nav.klage.oppgave.api.view.SaksbehandlerInput
import no.nav.klage.oppgave.config.SecurityConfiguration.Companion.ISSUER_AAD
import no.nav.klage.oppgave.domain.klage.Behandling
import no.nav.klage.oppgave.service.BehandlingService
import no.nav.klage.oppgave.service.InnloggetSaksbehandlerService
import no.nav.klage.oppgave.service.SaksbehandlerService
import no.nav.klage.oppgave.util.getLogger
import no.nav.klage.oppgave.util.logBehandlingMethodDetails
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@Tag(name = "kabal-api")
@ProtectedWithClaims(issuer = ISSUER_AAD)
@RequestMapping("/behandlinger")
class BehandlingROLController(
    private val innloggetSaksbehandlerService: InnloggetSaksbehandlerService,
    private val behandlingService: BehandlingService,
    private val saksbehandlerService: SaksbehandlerService,
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    @GetMapping("/{id}/rol")
    fun getROL(
        @PathVariable("id") behandlingId: UUID
    ): RolView {
        logBehandlingMethodDetails(
            ::getROL.name,
            innloggetSaksbehandlerService.getInnloggetIdent(),
            behandlingId,
            logger
        )
        val behandling = behandlingService.getBehandling(behandlingId)

        return behandling.toRolView()
    }

    @GetMapping("/{id}/rolflowstate")
    fun getROLFlowState(
        @PathVariable("id") behandlingId: UUID
    ): FlowStateView {
        logBehandlingMethodDetails(
            ::getROLFlowState.name,
            innloggetSaksbehandlerService.getInnloggetIdent(),
            behandlingId,
            logger
        )
        return FlowStateView(flowState = behandlingService.getBehandling(behandlingId).rolFlowState)
    }

    @PutMapping("/{behandlingId}/rolnavident")
    fun setROLIdent(
        @PathVariable("behandlingId") behandlingId: UUID,
        @RequestBody input: SaksbehandlerInput
    ): RolView {
        logBehandlingMethodDetails(
            ::setROLIdent.name,
            innloggetSaksbehandlerService.getInnloggetIdent(),
            behandlingId,
            logger
        )

        val behandling = behandlingService.setROLIdent(
            behandlingId = behandlingId,
            rolIdent = input.navIdent,
            utfoerendeSaksbehandlerIdent = innloggetSaksbehandlerService.getInnloggetIdent()
        )

        return behandling.toRolView()
    }

    @PutMapping("/{behandlingId}/rolflowstate")
    fun setROLFlowState(
        @PathVariable("behandlingId") behandlingId: UUID,
        @RequestBody input: FlowStateInput,
    ): RolView {
        logBehandlingMethodDetails(
            ::setROLFlowState.name,
            innloggetSaksbehandlerService.getInnloggetIdent(),
            behandlingId,
            logger
        )

        val behandling = behandlingService.setROLFlowState(
            behandlingId = behandlingId,
            flowState = input.flowState,
            utfoerendeSaksbehandlerIdent = innloggetSaksbehandlerService.getInnloggetIdent())

        return behandling.toRolView()
    }

    private fun Behandling.toRolView() = RolView(
        navIdent = rolIdent,
        navn = if (rolIdent != null) saksbehandlerService.getNameForIdent(rolIdent!!) else null,
        flowState = rolFlowState,
        modified = modified,
    )
}