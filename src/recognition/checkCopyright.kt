package ru.rtuitlab.copycheck.copyright

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.zip
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import ru.rtuitlab.copycheck.models.*

const val RAO_FOREIGN_URL = "https://rao.ru/information/reestry/reestr-proizvedenij-zarubezhnyh-pravoobladatelej/"
const val RAO_RUSSIAN_URL = "https://rao.ru/information/reestry/reestr-proizvedenij-rossijskih-pravoobladatelej/"

suspend fun checkCopyright(recognitionResult: RecognitionResult): CopycheckResult {
    val russianResultFlow = flowOf(parseRao(recognitionResult.result!!, RAO_RUSSIAN_URL))
    val foreignResultFlow = flowOf(parseRao(recognitionResult.result, RAO_FOREIGN_URL))
    val appleResultFlow = flowOf(parseAudD(recognitionResult.result))

    return russianResultFlow.zip(foreignResultFlow) { result1, result2 ->
        result1.zip(result2)
    }.zip(appleResultFlow) { copyrightResult, appleResult ->
        CopycheckResult(recognitionResult, copyrightResult, appleResult)
    }.first()
}

fun CopyrightResult.zip(copyrightResult: CopyrightResult) = CopyrightResult(
        Integer.max(this.resultStatus, copyrightResult.resultStatus),
        this.data + copyrightResult.data
)

fun parseAudD(song: SongData): AppleResult? {
    val appleMusic = Jsoup.connect(song.songLink).get()
        .select(".service a[data-uri*=\"music.apple\"]")
        .attr("href")
    if (appleMusic.isBlank()) return null
    val doc = Jsoup.connect(appleMusic).get()
    return AppleResult(
        doc.select(".song-copyright").html(),
        doc.select(".explicit").isNotEmpty()
    )
}

fun parseRao(song: SongData, url: String): CopyrightResult {
    val selectTd = ".search-result-reestr tr:not(:first-of-type) td"
    val maxPage = { doc: Document ->
        doc.select(".pagination a:last-of-type").html().toIntOrNull() ?: 0
    }
    val findResults = { elements: Elements ->
        elements.fold("", { acc, e ->
            when (e.className()) {
                "title" -> acc + "\n" + (if (elements.first() != e) "\t\n" else "") + e.html()
                "artist" -> acc + "\n" + e.html()
                "role" -> acc + " (${e.select("a").html()})"
                else -> acc + "\n" + e.html()
            }
        }).split("\t").map {
            val list = it.split("\n")
            RaoSearchResult(
                list[1],
                list[2],
                list.subList(2, list.size - 1).joinToString(", ")
            )
        }
    }

    var site = Jsoup.connect("$url?work=${song.title}&author=${song.artist}").get()
    val results: MutableList<RaoSearchResult> = mutableListOf()
    if (site.select(selectTd).isNotEmpty()) {
        results.addAll(findResults(site.select(selectTd)))
    }
    if (results.isEmpty()) {
        site = Jsoup.connect("$url?work=${song.title}").get()
        if (site.select(selectTd).isNotEmpty()) {
            results.addAll(findResults(site.select(selectTd)))
        }
        return if (results.isEmpty()) {
            CopyrightResult(0, emptyList())
        } else {
            // half match
            repeat (maxPage(site)-1) {
                results.addAll(findResults(
                    Jsoup.connect("$url${it+2}/?work=${song.title}").get()
                        .select(selectTd)
                ))
            }
            CopyrightResult(1, results)
        }
    } else {
        // full match
        repeat (maxPage(site)-1) {
            results.addAll(findResults(
                Jsoup.connect("$url/${it+2}?work=${song.title}&author=${song.artist}").get()
                    .select(selectTd)
            ))
        }
        return CopyrightResult(2, results)
    }
}