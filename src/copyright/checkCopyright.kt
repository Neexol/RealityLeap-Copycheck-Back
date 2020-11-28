package ru.rtuitlab.copycheck.copyright

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import ru.rtuitlab.copycheck.models.SongData

const val RAO_FOREIGN_URL = "https://rao.ru/information/reestry/reestr-proizvedenij-zarubezhnyh-pravoobladatelej/"
const val RAO_RUSSIAN_URL = "https://rao.ru/information/reestry/reestr-proizvedenij-rossijskih-pravoobladatelej/"

fun parseAudD(song: SongData): AppleResult? {
    val appleMusic = Jsoup.connect(song.songLink).get()
//        .select(".service a[data-player=\"applemusic\"]")
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