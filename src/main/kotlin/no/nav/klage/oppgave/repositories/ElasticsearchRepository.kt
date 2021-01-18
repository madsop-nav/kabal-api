package no.nav.klage.oppgave.repositories

import no.nav.klage.oppgave.domain.*
import no.nav.klage.oppgave.domain.elasticsearch.EsOppgave
import no.nav.klage.oppgave.util.getLogger
import org.elasticsearch.index.query.BoolQueryBuilder
import org.elasticsearch.index.query.QueryBuilder
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.sort.SortBuilders
import org.elasticsearch.search.sort.SortOrder
import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.core.io.ClassPathResource
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate
import org.springframework.data.elasticsearch.core.SearchHits
import org.springframework.data.elasticsearch.core.document.Document
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder
import org.springframework.data.elasticsearch.core.query.Query
import java.time.format.DateTimeFormatter


open class ElasticsearchRepository(
    val esTemplate: ElasticsearchRestTemplate,
    val innloggetSaksbehandlerRepository: InnloggetSaksbehandlerRepository
) :
    ApplicationListener<ContextRefreshedEvent> {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    override fun onApplicationEvent(event: ContextRefreshedEvent) {
        val indexOps = esTemplate.indexOps(IndexCoordinates.of("oppgavekopier"))
        if (!indexOps.exists()) {
            indexOps.create(readFromfile("settings.json"))
            indexOps.putMapping(readFromfile("mapping.json"))
        }
    }

    private fun readFromfile(filename: String): Document {
        val text: String =
            ClassPathResource("elasticsearch/${filename}").inputStream.bufferedReader(Charsets.UTF_8).readText()
        return Document.parse(text)
    }

    fun save(oppgaver: List<EsOppgave>) {
        esTemplate.save(oppgaver)
    }

    fun save(oppgave: EsOppgave) {
        esTemplate.save(oppgave)
    }

    open fun findByCriteria(criteria: OppgaverSearchCriteria): SearchHits<EsOppgave> {
        val query: Query = NativeSearchQueryBuilder()
            .withPageable(toPageable(criteria))
            .withSort(SortBuilders.fieldSort("fristFerdigstillelse").order(mapOrder(criteria.order)))
            .withQuery(criteria.toEsQuery())
            .build()
        val searchHits: SearchHits<EsOppgave> = esTemplate.search(query, EsOppgave::class.java)
        println("ANTALL TREFF: ${searchHits.totalHits}")
        return searchHits
    }

    private fun mapOrder(order: OppgaverSearchCriteria.Order?): SortOrder {
        return order.let {
            when {
                it == null -> SortOrder.ASC
                it == OppgaverSearchCriteria.Order.ASC -> SortOrder.ASC
                it == OppgaverSearchCriteria.Order.DESC -> SortOrder.DESC
                else -> SortOrder.ASC
            }
        }
    }

    private fun toPageable(criteria: OppgaverSearchCriteria): Pageable {
        val page: Int = (criteria.offset / criteria.limit)
        val size: Int = criteria.limit
        return PageRequest.of(page, size)
    }

    private fun OppgaverSearchCriteria.toEsQuery(): QueryBuilder {

        val baseQuery: BoolQueryBuilder = QueryBuilders.boolQuery()
        logger.debug("Search criteria: {}", this)

        val filterQuery = QueryBuilders.boolQuery()
        //TODO: Kunne nok vurdert å bruke filters også til andre ting enn sikkerhet, ref https://stackoverflow.com/questions/14595988/queries-vs-filters
        baseQuery.filter(filterQuery)
        if (!innloggetSaksbehandlerRepository.kanBehandleEgenAnsatt()) {
            filterQuery.mustNot(QueryBuilders.termQuery("egenAnsatt", true))
        }
        if (!innloggetSaksbehandlerRepository.kanBehandleFortrolig()) {
            filterQuery.mustNot(QueryBuilders.termQuery("fortrolig", true))
        }
        if (!innloggetSaksbehandlerRepository.kanBehandleStrengtFortrolig()) {
            filterQuery.mustNot(QueryBuilders.termQuery("strengtFortrolig", true))
        }
        
        baseQuery.must(QueryBuilders.termQuery("statuskategori", statuskategori))

        val innerQueryOppgavetype = QueryBuilders.boolQuery()
        baseQuery.must(innerQueryOppgavetype)
        innerQueryOppgavetype.should(QueryBuilders.termQuery("oppgavetype", "BEH_SAK_MK"))
        innerQueryOppgavetype.should(QueryBuilders.termQuery("oppgavetype", "BEH_SAK"))

        enhetsnr?.let {
            baseQuery.must(QueryBuilders.termQuery("tildeltEnhetsnr", enhetsnr))
        }

        val innerQueryBehandlingtype = QueryBuilders.boolQuery()
        baseQuery.must(innerQueryBehandlingtype)
        if (typer.isNotEmpty()) {
            typer.forEach {
                innerQueryBehandlingtype.should(QueryBuilders.termQuery("behandlingstype", mapType(it)))
            }
        } else {
            innerQueryBehandlingtype.should(QueryBuilders.termQuery("behandlingstype", mapType(TYPE_KLAGE)))
        }

        val innerQueryTema = QueryBuilders.boolQuery()
        baseQuery.must(innerQueryTema)
        temaer.forEach {
            innerQueryTema.should(QueryBuilders.termQuery("tema", it))
        }

        erTildeltSaksbehandler?.let {
            if (erTildeltSaksbehandler) {
                baseQuery.must(QueryBuilders.existsQuery("tilordnetRessurs"))
            } else {
                baseQuery.mustNot(QueryBuilders.existsQuery("tilordnetRessurs"))
            }
        }
        saksbehandler?.let {
            baseQuery.must(QueryBuilders.termQuery("tilordnetRessurs", saksbehandler))
        }

        opprettetFom?.let {
            baseQuery.must(
                QueryBuilders.rangeQuery("opprettetTidspunkt").gte(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(it))
            )
        }
        opprettetTom?.let {
            baseQuery.must(
                QueryBuilders.rangeQuery("opprettetTidspunkt").lte(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(it))
            )
        }
        ferdigstiltFom?.let {
            baseQuery.must(
                QueryBuilders.rangeQuery("ferdigstiltTidspunkt").gte(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(it))
            )
        }
        ferdigstiltTom?.let {
            baseQuery.must(
                QueryBuilders.rangeQuery("ferdigstiltTidspunkt").lte(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(it))
            )
        }
        fristFom?.let {
            baseQuery.must(
                QueryBuilders.rangeQuery("fristFerdigstillelse").gte(DateTimeFormatter.ISO_LOCAL_DATE.format(it))
            )
        }
        fristTom?.let {
            baseQuery.must(
                QueryBuilders.rangeQuery("fristFerdigstillelse").lte(DateTimeFormatter.ISO_LOCAL_DATE.format(it))
            )
        }

        if (hjemler.isNotEmpty()) {
            val innerQueryHjemler = QueryBuilders.boolQuery()
            baseQuery.must(innerQueryHjemler)
            hjemler.forEach {
                innerQueryHjemler.should(QueryBuilders.termQuery("hjemler", it))
            }
        }

        logger.info("Making search request with query {}", baseQuery.toString())
        return baseQuery
    }

    private fun mapType(type: String): String {
        return when (type) {
            TYPE_KLAGE -> BEHANDLINGSTYPE_KLAGE
            TYPE_FEILUTBETALING -> BEHANDLINGSTYPE_FEILUTBETALING
            else -> {
                logger.warn("Unknown type: {}", type)
                type
            }
        }
    }
}