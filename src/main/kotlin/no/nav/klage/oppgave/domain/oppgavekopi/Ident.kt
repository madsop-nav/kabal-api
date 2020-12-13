package no.nav.klage.oppgave.domain.oppgavekopi

import java.time.LocalDate
import javax.persistence.*

@Entity
@Table(name = "ident", schema = "oppgave")
data class Ident(
    @Id
    @Column(name = "id")
    @SequenceGenerator(
        name = "ident_seq",
        sequenceName = "oppgave.ident_seq",
        allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ident_seq")
    var id: Long?,
    @Enumerated(EnumType.STRING)
    @Column(name = "TYPE")
    var identType: IdentType,
    @Column(name = "verdi")
    var verdi: String,
    @Column(name = "folkeregisterident")
    var folkeregisterident: String? = null,
    @Column(name = "registrert_dato")
    var registrertDato: LocalDate? = null
)
