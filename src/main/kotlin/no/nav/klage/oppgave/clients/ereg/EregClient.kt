package no.nav.klage.oppgave.clients.ereg


import no.nav.klage.oppgave.exceptions.EREGOrganizationNotFoundException
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class EregClient(
    private val eregWebClient: WebClient,
) {

    @Value("\${spring.application.name}")
    lateinit var applicationName: String

    fun hentOrganisasjon(orgnummer: String): Organisasjon {
        return kotlin.runCatching {
            eregWebClient.get()
                .uri { uriBuilder ->
                    uriBuilder
                        .path("/organisasjon/{orgnummer}")
                        .queryParam("inkluderHierarki", false)
                        .build(orgnummer)
                }
                .accept(MediaType.APPLICATION_JSON)
                .header("Nav-Consumer-Id", applicationName)
                .retrieve()
                .bodyToMono<Organisasjon>()
                .block() ?: throw EREGOrganizationNotFoundException("Search for organization $orgnummer in Ereg returned null.")
        }.fold(
            onSuccess = { it },
            onFailure = { error ->
                when (error) {
                    is WebClientResponseException.NotFound -> {
                        throw EREGOrganizationNotFoundException("Couldn't find organization $orgnummer in Ereg.")
                    }
                    else -> throw error
                }
            }

        )
    }

    fun isOrganisasjonActive(orgnummer: String) = hentOrganisasjon(orgnummer).isActive()
}