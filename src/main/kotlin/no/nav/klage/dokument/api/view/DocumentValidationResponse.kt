package no.nav.klage.dokument.api.view

data class DocumentValidationResponse(
    val dokumentId: String,
    val errors: List<DocumentValidationError> = emptyList()
) {
    data class DocumentValidationError(
        val type: String,
        val paths: List<List<Int>> = emptyList()
    )
}