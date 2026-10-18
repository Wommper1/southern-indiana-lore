package com.example.data.archivist

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Historical Archivist Service for Southern Indiana Lore.
 *
 * This is a keyword lookup over a small set of hand-written entries, not a language model: a few
 * well-known sites and a driving-tour suggestion get a curated answer, and anything else gets a
 * general overview. It runs entirely on-device with no network calls or API keys.
 */
class LocalArchivistService {

    suspend fun askArchivist(
        userQuery: String,
        siteContext: String? = null
    ): Result<String> = withContext(Dispatchers.Default) {
        Result.success(getCuratedArchivalResponse(userQuery, siteContext))
    }

    private fun getCuratedArchivalResponse(query: String, siteContext: String?): String {
        val qLower = query.lowercase()
        val sLower = siteContext?.lowercase() ?: ""

        return when {
            qLower.contains("culbertson") || sLower.contains("culbertson") -> {
                """
                🏛️ ARCHIVAL RECORD: Culbertson Mansion (New Albany, Floyd County)
                • Historical Basis: Constructed in 1867-1869 for William S. Culbertson, once described as the richest man in Indiana, by architect James T. Banes at a cost of $120,000. Features 25 rooms and a hand-painted French Second Empire ceiling. Entered the National Register of Historic Places in 1974 and State Historic Site in 1976.
                • Documented Folklore: Local ghost lore tells of anomalous piano melodies and cold spots on the third-floor servant stairs. The lightning strike of 1888 on the carriage house remains a focal point for regional paranormal investigations.
                • Primary Sources: Indiana State Museum Archives; Library of Congress HABS IN-106; Floyd County Deed Books.
                """.trimIndent()
            }
            qLower.contains("underground") || qLower.contains("church") || qLower.contains("freedom") || qLower.contains("clock") || sLower.contains("clock") -> {
                """
                🕊️ ARCHIVAL RECORD: The Town Clock Church & River Crossings (New Albany)
                • Historical Basis: Built in 1852 by the Second Presbyterian Church (now Second Baptist). The 160-foot steeple stood directly opposite Portland (Louisville), serving as a crucial orientation point for escaping freedom seekers crossing the Ohio River. Led by abolitionist pastors including Rev. John Atterbury.
                • Field Documentation: Officially designated as a National Park Service 'Network to Freedom' site. Concealed sub-floor spaces beneath the choir platform provided immediate refuge.
                • Primary Sources: National Park Service Network to Freedom; Pamela R. Peters, 'The Underground Railroad in Floyd County' (2001).
                """.trimIndent()
            }
            qLower.contains("tunnel") || qLower.contains("tunnelton") || sLower.contains("tunnel") -> {
                """
                🚂 ARCHIVAL RECORD: The Big Tunnel (Tunnelton, Lawrence County)
                • Historical Basis: Bored 1,731 feet through solid limestone in 1857 by the Ohio & Mississippi Railroad. Engineering was overseen by frontier rail gangs under perilous conditions with black powder blasting.
                • Documented Folklore: William Logan, a night watchman murdered along the right-of-way in the late 19th century, is central to Indiana railroad lore. Locals report a drifting lantern glow in the tunnel's damp depths.
                • Safety Notice: Active CSX freight line right-of-way. Viewing must strictly remain at legal public perimeter overlooks.
                """.trimIndent()
            }
            qLower.contains("corydon") || qLower.contains("capitol") || qLower.contains("constitution") || sLower.contains("corydon") -> {
                """
                📜 ARCHIVAL RECORD: First Indiana State Capitol & Constitution Elm (Corydon, Harrison County)
                • Historical Basis: Built between 1814-1816 of native blue limestone as the Harrison County courthouse. Served as Indiana's first state capitol from statehood in 1816 until 1825. Forty-three delegates drafted the 1816 constitution under the shade of the Constitution Elm.
                • Primary Sources: Indiana Historical Bureau; National Register of Historic Places #75000019; Harrison County Historical Society.
                """.trimIndent()
            }
            qLower.contains("wyandotte") || qLower.contains("cave") || sLower.contains("wyandotte") -> {
                """
                🦇 ARCHIVAL RECORD: Wyandotte Cave Saltpeter Works (Crawford County)
                • Historical Basis: Mined by Dr. Benjamin Adams for potassium nitrate (saltpeter) for domestic gunpowder during the British naval embargoes of the War of 1812. In 1850, Henry P. Rothrock discovered the vast inner chamber containing 'Rothrock's Cathedral', home to the 135-foot Monument Mountain.
                • Primary Sources: National Natural Landmark documentation; Indiana Geological Survey Reports by E.T. Cox (1871).
                """.trimIndent()
            }
            qLower.contains("devils backbone") || qLower.contains("devil's backbone") || qLower.contains("rose island") || sLower.contains("backbone") || sLower.contains("rose island") -> {
                """
                🏰 ARCHIVAL RECORD: Devil's Backbone & Rose Island (Charlestown, Clark County)
                • Historical Basis: A narrow limestone ridge rising 150 feet above Fourteen Mile Creek and the Ohio River. Site of prehistoric stone fortifications attributed to ancient indigenous cultures, later mythologized in frontier lore as the 'Welsh Indians' or Prince Madoc legend. In the 1920s, the peninsula became the Rose Island Amusement Park until ruined by the 1937 flood.
                • Primary Sources: Indiana Department of Natural Resources (Charlestown State Park records); Indiana Academy of Science surveys.
                """.trimIndent()
            }
            qLower.contains("howard") || qLower.contains("steamboat") || sLower.contains("howard") || sLower.contains("steamboat") -> {
                """
                🚢 ARCHIVAL RECORD: Howard Steamboat Museum & Shipyards (Jeffersonville, Clark County)
                • Historical Basis: The Howard Shipyards, traditionally dated to 1834, built more than 1,100 river vessels, including the legendary J.M. White and World War II naval LSTs. The 1890 22-room Romanesque Revival mansion showcases master hand-carved woodwork by shipyard craftsmen.
                • Primary Sources: Howard National Historic Landmark documentation; Historic American Buildings Survey (HABS).
                """.trimIndent()
            }
            qLower.contains("french lick") || qLower.contains("west baden") || sLower.contains("french lick") || sLower.contains("west baden") -> {
                """
                ⛲ ARCHIVAL RECORD: West Baden Springs & French Lick Mineral Spas (Orange County)
                • Historical Basis: Built in 1902 following a catastrophic fire, West Baden's 200-foot free-span brick dome stood as the world's largest unsupported dome until the Houston Astrodome in 1965. Known as the 'Eighth Wonder of the World', it drew guests for 'Pluto Water' sulfur mineral cures.
                • Primary Sources: National Historic Landmark registration (1987); Historic Landmarks Foundation of Indiana.
                """.trimIndent()
            }
            qLower.contains("division street") || qLower.contains("school") || sLower.contains("division") -> {
                """
                🏫 ARCHIVAL RECORD: Division Street School (New Albany, Floyd County)
                • Historical Basis: Opened in 1884 as an elementary school for segregated African American children following the Indiana Segregation Act of 1869. Operated continuously through desegregation in 1946. Restored as a community education center and museum.
                • Primary Sources: National Register of Historic Places; Floyd County Historical Society.
                """.trimIndent()
            }
            qLower.contains("sleepy hollow") || qLower.contains("borden") || sLower.contains("sleepy hollow") -> {
                """
                🌲 ARCHIVAL RECORD: Sleepy Hollow Ravine & Knobstone Escarpment (Clark County)
                • Historical Basis: Carved through Mississippian Borden Group siltstone and shale. The steep hollows formed natural transit corridors for early pioneer wagon trails and timber hauling in the 1800s.
                • Documented Folklore: Deep ravine shadows give rise to lingering settler tales of spectral travelers, phantom wagon wheels on gravel switchbacks, and localized cold spots.
                • Primary Sources: Clark County Historical Survey; Indiana Geological Survey.
                """.trimIndent()
            }
            qLower.contains("hayswood") || qLower.contains("jail") || sLower.contains("hayswood") -> {
                """
                ⚖️ ARCHIVAL RECORD: Old Harrison County Jail & Hayswood (Corydon)
                • Historical Basis: Built in 1891 of dark coursed limestone and reinforced tool-proof steel cells. Housed regional detainees through significant frontier trials. Hayswood Nature Reserve preserves the surrounding old-growth Indian Creek bottomland.
                • Documented Folklore: Former deputies and county guards recounted eerie footsteps in the solitary cellblock corridor and cold drafts around the iron cell doors.
                • Primary Sources: Harrison County Court Archives; Historic Hoosier Hills Preservation Commission.
                """.trimIndent()
            }
            qLower.contains("tour") || qLower.contains("route") || qLower.contains("suggest") || qLower.contains("drive") -> {
                """
                🚗 RECOMMENDED EXPEDITION ROUTE: The Historic Ohio River & Knobs Corridor
                1. Start in New Albany: Town Clock Church (Network to Freedom) ➔ Culbertson Mansion (Mansion Row) ➔ Division Street School.
                2. Eastward along River: Howard Steamboat Museum (Jeffersonville) ➔ Falls of the Ohio (Clarksville).
                3. Westward along Scenic Highway 62: Corydon Capitol & Constitution Elm ➔ Old Harrison County Jail & Hayswood ➔ Wyandotte Cave.
                4. Northern Ridgeline Spur: Sleepy Hollow Overlook ➔ Lawrence County Big Tunnel (Tunnelton CSX public perimeter).
                
                • Tips: Download offline maps and pack an expedition dossier before heading deep into the Harrison-Crawford limestone valleys.
                """.trimIndent()
            }
            else -> {
                """
                📜 SOUTHERN INDIANA HISTORICAL ARCHIVIST
                Inquiry: "$query"

                • Regional Archival Synopsis: The Ohio River Knobs region (encompassing Floyd, Clark, Harrison, Lawrence, Crawford, and Orange counties) represents one of the most concentrated historic borderlands in the Midwest. 
                From the 1803 Lewis and Clark embarkation at the Falls of the Ohio and the 1816 Constitutional debates in Corydon, to 19th-century steamboat shipbuilding in Jeffersonville and active Underground Railroad crossings along the New Albany riverbank, each site in this app lists its sources, and its confidence badge shows how thoroughly it has been verified.

                • Historical Fact vs Folklore: Look for the 'Confidence Level' badges in the app. Sites backed by official state or national designations are marked accordingly, and many entries are still labeled Unverified, while folkloric traditions highlight the oral heritage passed down through generations of river and knobstone families.
                • Mode: 100% On-Device Offline Archival Database — No external network connection or API keys required.
                """.trimIndent()
            }
        }
    }
}
