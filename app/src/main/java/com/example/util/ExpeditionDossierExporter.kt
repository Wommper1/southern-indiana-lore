package com.example.util

import android.content.Context
import android.content.Intent
import com.example.data.model.LoreSite
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Version 1.0 Production Field Expedition Dossier & GPX Exporter.
 *
 * Allows researchers, field investigators, and folklore enthusiasts to export:
 * 1. Formatted Markdown Field Logbooks (.md)
 * 2. Standard GPS Exchange Format (.gpx 1.1) for Garmin, OsmAnd, QGIS, and Google Earth.
 */
object ExpeditionDossierExporter {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
    private val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)

    /**
     * Compiles historical sites and field notes into a rich Markdown dossier.
     */
    fun generateMarkdownDossier(
        sites: List<LoreSite>,
        title: String = "Southern Indiana Lore Expedition Dossier"
    ): String {
        val now = dateFormat.format(Date())
        val totalSites = sites.size
        val visitedSites = sites.count { it.isVisited || it.encounterCount > 0 }
        val customSites = sites.count { it.isCustom }

        val sb = StringBuilder()
        sb.append("# $title\n\n")
        sb.append("**Production**: Alex Shade Productions — Southern Indiana Lore v1.0.1  \n")
        sb.append("**Generated**: $now  \n")
        sb.append("**Total Mapped POIs**: $totalSites  \n")
        sb.append("**Sites Encountered/Visited**: $visitedSites  \n")
        if (customSites > 0) {
            sb.append("**Custom Field Discoveries**: $customSites  \n")
        }
        sb.append("\n---\n\n")

        sb.append("## Field Discovery Summary\n\n")
        sb.append("| Site Name | County | Category | Coordinates | Status |\n")
        sb.append("| :--- | :--- | :--- | :--- | :--- |\n")
        for (site in sites) {
            val status = when {
                site.isVisited -> "Visited"
                site.encounterCount > 0 -> "Passed (${site.encounterCount}x)"
                else -> "Documented"
            }
            val coords = String.format(Locale.US, "%.5f, %.5f", site.latitude, site.longitude)
            sb.append("| **${escapeMd(site.title)}** | ${escapeMd(site.county)} | ${escapeMd(site.category)} | `$coords` | $status |\n")
        }
        sb.append("\n---\n\n")

        sb.append("## Detailed Field Records\n\n")
        for ((index, site) in sites.withIndex()) {
            sb.append("### ${index + 1}. ${site.title}\n\n")
            sb.append("- **Category**: ${site.category} (${site.county})\n")
            sb.append("- **Era**: ${site.era}\n")
            sb.append(String.format(Locale.US, "- **Coordinates**: `%.6f, %.6f` (Trigger Radius: %dm)\n", site.latitude, site.longitude, site.geofenceRadiusMeters))
            sb.append("- **Access Level**: ${site.accessStatus} / ${site.accessLevel}\n")

            if (site.isVisited || site.encounterCount > 0) {
                val visitedDate = if (site.lastVisitedTimestamp > 0) {
                    dateFormat.format(Date(site.lastVisitedTimestamp))
                } else "Recorded on Tour"
                sb.append("- **Field Visit Date**: $visitedDate (Total Encounters: ${site.encounterCount})\n")
            }

            if (site.userNotes.isNotBlank()) {
                sb.append("\n> **Field Investigator Notes**:\n> ${site.userNotes.replace("\n", "\n> ")}\n\n")
            }

            if (site.driveStory.isNotBlank()) {
                sb.append("\n**Drive Dispatch**: *\"${site.driveStory}\"*\n\n")
            }

            if (site.verifiedHistory.isNotBlank()) {
                sb.append("**Verified Historical Record**:\n${site.verifiedHistory}\n\n")
            }

            if (site.folkloreTradition.isNotBlank()) {
                sb.append("**Folklore & Oral Tradition**:\n${site.folkloreTradition}\n\n")
            }

            if (site.whatMayBeUnderground.isNotBlank() || site.undergroundConfidence.isNotBlank()) {
                sb.append("**Subterranean & Karst Features**:\n")
                if (site.undergroundConfidence.isNotBlank()) {
                    sb.append("- Classification: *${site.undergroundConfidence}*\n")
                }
                if (site.whatMayBeUnderground.isNotBlank()) {
                    sb.append("- ${site.whatMayBeUnderground}\n")
                }
                sb.append("\n")
            }

            if (site.sources.isNotBlank() || site.sourceNotes.isNotBlank()) {
                sb.append("**Archival Citations**:\n")
                if (site.sources.isNotBlank()) sb.append("- Sources: ${site.sources}\n")
                if (site.sourceNotes.isNotBlank()) sb.append("- Notes: ${site.sourceNotes}\n")
                sb.append("\n")
            }

            sb.append("---\n\n")
        }

        sb.append("*(Exported automatically from Southern Indiana Lore v1.0.1 by Alex Shade Productions)*\n")
        return sb.toString()
    }

    /**
     * Serializes historical sites into a standard GPX 1.1 XML file.
     */
    fun generateGpxTrack(
        sites: List<LoreSite>,
        trackName: String = "Southern Indiana Lore Waypoints"
    ): String {
        val nowIso = isoFormat.format(Date())
        val sb = StringBuilder()

        sb.append("""<?xml version="1.0" encoding="UTF-8"?>""").append("\n")
        sb.append("""<gpx version="1.1" creator="Southern Indiana Lore v1.0.1 - Alex Shade Productions"""")
        sb.append(""" xmlns="http://www.topografix.com/GPX/1/1"""")
        sb.append(""" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"""")
        sb.append(""" xsi:schemaLocation="http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd">""").append("\n")

        sb.append("  <metadata>\n")
        sb.append("    <name>${escapeXml(trackName)}</name>\n")
        sb.append("    <desc>Location-aware historical markers and folklore POIs across Southern Indiana</desc>\n")
        sb.append("    <author>\n")
        sb.append("      <name>Alex Shade Productions</name>\n")
        sb.append("    </author>\n")
        sb.append("    <time>$nowIso</time>\n")
        sb.append("  </metadata>\n\n")

        for (site in sites) {
            sb.append(String.format(Locale.US, """  <wpt lat="%.6f" lon="%.6f">""", site.latitude, site.longitude)).append("\n")
            sb.append("    <name>${escapeXml(site.title)}</name>\n")
            
            val desc = buildString {
                append("[${site.category}] ${site.county} (${site.era}). ")
                if (site.driveStory.isNotBlank()) append(site.driveStory).append(" ")
                if (site.userNotes.isNotBlank()) append("Field Notes: ").append(site.userNotes).append(" ")
            }.trim()
            
            sb.append("    <desc>${escapeXml(desc)}</desc>\n")
            sb.append("    <type>${escapeXml(site.category)}</type>\n")
            sb.append("    <sym>Historic Marker</sym>\n")
            sb.append("  </wpt>\n")
        }

        sb.append("</gpx>\n")
        return sb.toString()
    }

    /**
     * Dispatches content via the Android system Share sheet.
     */
    fun shareContent(
        context: Context,
        content: String,
        subject: String,
        mimeType: String = "text/plain"
    ) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, content)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val chooser = Intent.createChooser(intent, subject).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }

    private fun escapeMd(text: String): String {
        return text.replace("|", "\\|").replace("\n", " ")
    }

    private fun escapeXml(text: String): String {
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }
}
