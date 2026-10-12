package com.kaii.photos.repositories

import com.kaii.photos.data.datasources.LatestNewsDataSource
import com.kaii.photos.data.parsers.HTMLToLnmParser
import com.kaii.photos.data.parsers.LnmParser
import com.kaii.photos.data.providers.AppVersionProvider
import com.kaii.photos.domain.news.News
import javax.inject.Inject

class LatestNewsRepository @Inject constructor(
    private val dataSource: LatestNewsDataSource,
    private val hTMLToLnmParser: HTMLToLnmParser,
    private val lnmParser: LnmParser,
    private val versionProvider: AppVersionProvider
) {
    suspend fun getNews(): List<News> {
        val markdown = dataSource.fetch() ?: return emptyList()
        val lnm = hTMLToLnmParser.parse(markdown)

        return lnm.lineSequence().mapIndexedNotNull { id, line ->
            if (line.isNotBlank()) lnmParser.parseLine(line, id) else null
        }.toList()
    }

    suspend fun hasUpdate(): Boolean {
        val latestVersion = dataSource.getLatestVersion() ?: return false
        return compareVersions(versionProvider.getCurrentVersionString(), latestVersion) < 0
    }

    private fun compareVersions(current: String, latest: String): Int {
        val currentParts = current.removePrefix("v").substringBefore("-")
            .split(".").map { it.toIntOrNull() ?: 0 }
        val latestParts = latest.removePrefix("v").substringBefore("-")
            .split(".").map { it.toIntOrNull() ?: 0 }

        for (index in 0 until maxOf(currentParts.size, latestParts.size)) {
            val currentPart = currentParts.getOrElse(index) { 0 }
            val latestPart = latestParts.getOrElse(index) { 0 }
            if (currentPart != latestPart) return currentPart.compareTo(latestPart)
        }
        return 0
    }
}
