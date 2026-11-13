package com.example.data.remote

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Xml
import androidx.core.text.HtmlCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.xmlpull.v1.XmlPullParser
import java.io.StringReader
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

data class OnlineTopic(
    val id: String,
    val title: String,
    val source: String,
    val link: String,
    val pubDateFormatted: String,
    val timestampMillis: Long,
    val category: String,
    val summary: String
)

data class RssFetchResult(
    val topics: List<OnlineTopic>,
    val errorMessage: String? = null
)

object PetNewsRssRepository {

    private data class FeedSpec(
        val url: String,
        val category: String,
        val defaultSource: String,
        val isGovFeed: Boolean = false
    )

    private val feedSpecs = listOf(
        FeedSpec(
            url = "https://news.google.com/rss/search?q=(veterin%C3%A1r+OR+veterin%C3%A1rna+OR+besnota+OR+o%C4%8Dkovanie+OR+klie%C5%A1te)+AND+(pes+OR+ma%C4%8Dka+OR+zviera)+when:90d&hl=sk&gl=SK&ceid=SK:sk",
            category = "Veterina & Zdravie",
            defaultSource = "Slovenské správy"
        ),
        FeedSpec(
            url = "https://news.google.com/rss/search?q=(%22starostlivos%C5%A5+o+psa%22+OR+%22starostlivos%C5%A5+o+ma%C4%8Dku%22+OR+%22dom%C3%A1cich+mil%C3%A1%C4%8Dikov%22+OR+%22v%C3%BD%C5%BEiva+psa%22)+when:90d&hl=sk&gl=SK&ceid=SK:sk",
            category = "Psy & Mačky",
            defaultSource = "Slovenské správy"
        ),
        FeedSpec(
            url = "https://svps.sk/feed/",
            category = "ŠVPS SR",
            defaultSource = "ŠVPS SR",
            isGovFeed = true
        )
    )

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(12, TimeUnit.SECONDS)
            .callTimeout(15, TimeUnit.SECONDS)
            .followRedirects(true)
            .build()
    }

    private val govHrNoiseKeywords = listOf(
        "voľné pracovné",
        "voľné miesto",
        "hlavný radca",
        "štátnozamestnaneck",
        "výberové konanie",
        "verejné obstarávanie",
        "zverejňovanie zmlúv",
        "faktúry",
        "objednávky"
    )

    fun isNetworkAvailable(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return false
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    suspend fun fetchLiveTopics(context: Context): RssFetchResult = withContext(Dispatchers.IO) {
        if (!isNetworkAvailable(context)) {
            return@withContext RssFetchResult(
                topics = emptyList(),
                errorMessage = "Zariadenie je offline. Zapnite Wi-Fi alebo mobilné dáta pre stiahnutie aktuálnych tém."
            )
        }

        val allItems = mutableListOf<OnlineTopic>()
        var successfulFeeds = 0
        var lastError: String? = null

        coroutineScope {
            val deferredResults = feedSpecs.map { spec ->
                async {
                    try {
                        val xml = downloadFeedXml(spec.url)
                        val parsed = parseRssXml(xml, spec)
                        Result.success(parsed)
                    } catch (e: Exception) {
                        Result.failure(e)
                    }
                }
            }

            deferredResults.forEach { deferred ->
                val res = deferred.await()
                if (res.isSuccess) {
                    successfulFeeds++
                    allItems.addAll(res.getOrDefault(emptyList()))
                } else {
                    lastError = res.exceptionOrNull()?.localizedMessage
                }
            }
        }

        val deduplicated = allItems
            .distinctBy { normalizeKey(it.title) }
            .sortedByDescending { it.timestampMillis }

        if (successfulFeeds == 0) {
            return@withContext RssFetchResult(
                topics = emptyList(),
                errorMessage = "Nepodarilo sa pripojiť k online zdrojom (${lastError ?: "chyba siete"})."
            )
        }

        if (deduplicated.isEmpty()) {
            return@withContext RssFetchResult(
                topics = emptyList(),
                errorMessage = "Online zdroje nevrátili žiadne aktuálne články."
            )
        }

        RssFetchResult(topics = deduplicated, errorMessage = null)
    }

    private fun downloadFeedXml(url: String): String {
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "AnimaCuraSK/1.0 (+Android RSS Reader)")
            .header("Accept", "application/rss+xml, application/xml, text/xml;q=0.9, */*;q=0.8")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IllegalStateException("HTTP ${response.code}")
            }
            return response.body?.string() ?: throw IllegalStateException("Prázdna odpoveď servera")
        }
    }

    private fun parseRssXml(xmlContent: String, spec: FeedSpec): List<OnlineTopic> {
        val topics = mutableListOf<OnlineTopic>()
        val parser = Xml.newPullParser()
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        parser.setInput(StringReader(xmlContent))

        var insideItem = false
        var currentTag = ""
        var title = ""
        var link = ""
        var pubDate = ""
        var description = ""
        var source = ""

        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    currentTag = parser.name?.lowercase(Locale.ROOT) ?: ""
                    if (currentTag == "item") {
                        insideItem = true
                        title = ""
                        link = ""
                        pubDate = ""
                        description = ""
                        source = ""
                    } else if (insideItem) {
                        when (currentTag) {
                            "title" -> title = safeNextText(parser)
                            "link" -> link = safeNextText(parser)
                            "pubdate" -> pubDate = safeNextText(parser)
                            "description" -> description = safeNextText(parser)
                            "source" -> source = safeNextText(parser)
                        }
                    }
                }

                XmlPullParser.END_TAG -> {
                    val tagName = parser.name?.lowercase(Locale.ROOT) ?: ""
                    if (tagName == "item" && insideItem) {
                        insideItem = false
                        buildTopicOrNull(
                            rawTitle = title,
                            rawLink = link,
                            rawPubDate = pubDate,
                            rawDescription = description,
                            rawSource = source,
                            spec = spec
                        )?.let { topics.add(it) }
                    }
                    currentTag = ""
                }
            }
            eventType = parser.next()
        }

        return topics.take(15)
    }

    private fun safeNextText(parser: XmlPullParser): String {
        return try {
            parser.nextText().trim()
        } catch (_: Exception) {
            ""
        }
    }

    private fun buildTopicOrNull(
        rawTitle: String,
        rawLink: String,
        rawPubDate: String,
        rawDescription: String,
        rawSource: String,
        spec: FeedSpec
    ): OnlineTopic? {
        val cleanSource = cleanHtmlText(rawSource).ifBlank { spec.defaultSource }
        var cleanTitle = cleanHtmlText(rawTitle)
        if (cleanTitle.isBlank() || rawLink.isBlank()) return null

        // Google News RSS titles typically end with " - Source Name"
        val sourceSuffix = " - $cleanSource"
        if (cleanTitle.endsWith(sourceSuffix, ignoreCase = true) && cleanTitle.length > sourceSuffix.length + 10) {
            cleanTitle = cleanTitle.dropLast(sourceSuffix.length).trim()
        }

        if (spec.isGovFeed) {
            val lowerTitle = cleanTitle.lowercase(Locale("sk", "SK"))
            if (govHrNoiseKeywords.any { lowerTitle.contains(it) }) {
                return null
            }
        }

        val timestampMillis = parseRssDateMillis(rawPubDate)
        val formattedDate = formatRealDate(timestampMillis, rawPubDate)

        val cleanedSummaryRaw = cleanHtmlText(rawDescription)
        val finalSummary = if (cleanedSummaryRaw.isBlank() ||
            isRedundantSnippet(cleanedSummaryRaw, cleanTitle, cleanSource)
        ) {
            ""
        } else {
            if (cleanedSummaryRaw.length > 190) {
                cleanedSummaryRaw.take(187).trimEnd() + "..."
            } else {
                cleanedSummaryRaw
            }
        }

        val idSource = "${spec.category}_${rawLink}_${cleanTitle.hashCode()}"
        return OnlineTopic(
            id = idSource,
            title = cleanTitle,
            source = cleanSource,
            link = rawLink,
            pubDateFormatted = formattedDate,
            timestampMillis = timestampMillis,
            category = spec.category,
            summary = finalSummary
        )
    }

    private fun cleanHtmlText(raw: String): String {
        if (raw.isBlank()) return ""
        val decoded = HtmlCompat.fromHtml(raw, HtmlCompat.FROM_HTML_MODE_LEGACY).toString()
        return decoded
            .replace('\u00A0', ' ')
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun isRedundantSnippet(snippet: String, title: String, source: String): Boolean {
        val normSnippet = normalizeKey(snippet)
        val normTitle = normalizeKey(title)
        val normSource = normalizeKey(source)
        if (normSnippet == normTitle) return true
        if (normSnippet.startsWith(normTitle) && normSnippet.length <= normTitle.length + normSource.length + 6) {
            return true
        }
        return false
    }

    private fun normalizeKey(text: String): String {
        return text.lowercase(Locale("sk", "SK"))
            .replace(Regex("[^a-záčďéíľĺňóôŕšťúýž0-9]"), "")
    }

    private fun parseRssDateMillis(rawDate: String): Long {
        if (rawDate.isBlank()) return 0L
        val patterns = listOf(
            "EEE, dd MMM yyyy HH:mm:ss Z",
            "EEE, dd MMM yyyy HH:mm:ss zzz",
            "dd MMM yyyy HH:mm:ss Z",
            "yyyy-MM-dd'T'HH:mm:ssXXX"
        )
        for (pattern in patterns) {
            try {
                val sdf = SimpleDateFormat(pattern, Locale.ENGLISH).apply {
                    isLenient = true
                }
                val parsed = sdf.parse(rawDate)
                if (parsed != null) return parsed.time
            } catch (_: Exception) {
                // try next pattern
            }
        }
        return 0L
    }

    private fun formatRealDate(timestampMillis: Long, fallbackRaw: String): String {
        if (timestampMillis <= 0L) {
            return fallbackRaw.take(16).ifBlank { "Aktuálne" }
        }
        val tz = TimeZone.getTimeZone("Europe/Bratislava")
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale("sk", "SK")).apply {
            timeZone = tz
        }
        val formattedDay = dateFormat.format(Date(timestampMillis))

        val nowCal = Calendar.getInstance(tz)
        val targetCal = Calendar.getInstance(tz).apply { timeInMillis = timestampMillis }

        val sameYear = nowCal.get(Calendar.YEAR) == targetCal.get(Calendar.YEAR)
        val dayDiff = nowCal.get(Calendar.DAY_OF_YEAR) - targetCal.get(Calendar.DAY_OF_YEAR)

        return when {
            sameYear && dayDiff == 0 -> "Dnes • $formattedDay"
            sameYear && dayDiff == 1 -> "Včera • $formattedDay"
            else -> formattedDay
        }
    }
}
