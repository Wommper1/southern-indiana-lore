package com.example.data.model

data class WaxCylinderRecording(
    val id: String,
    val title: String,
    val recordedDate: String,
    val frequencyKhz: Int,
    val eyewitnessName: String,
    val speakerRole: String,
    val siteId: String,
    val mysteryId: String?,
    val transcript: String,
    val historicalContext: String,
    val linkedRelicId: String,
    val cylinderColorHex: Long = 0xFFD97706, // Amber wax
    val isUnlockedByDefault: Boolean = false
)

enum class RelicRarity {
    COMMON,
    RARE,
    LEGENDARY
}

data class HistoricalRelic(
    val id: String,
    val title: String,
    val era: String,
    val material: String,
    val description: String,
    val provenance: String,
    val linkedSiteId: String,
    val linkedMysteryId: String?,
    val rarity: RelicRarity = RelicRarity.RARE,
    val badgeIconName: String,
    val primaryColorHex: Long = 0xFFD4AF37
)

object CuratedOralHistoryArchive {

    // 1. Reno Gang Robbery Messenger
    val CYLINDER_RENO_MESSENGER = WaxCylinderRecording(
        id = "cylinder_reno_messenger",
        title = "Deposition of Adams Express Messenger Miller",
        recordedDate = "May 23, 1868",
        frequencyKhz = 840,
        eyewitnessName = "Messenger A.S. Miller",
        speakerRole = "Adams Express Guard & Car Escort",
        siteId = "marshfield_train_robbery",
        mysteryId = "reno_gold",
        transcript = "It was near ten o'clock at night when the locomotive halted abruptly at the Marshfield wood-station. Three men with blackened faces and navy colts burst the door lock. John Reno ordered me on my knees; when I reached for the express lockbox, they hurled me into the dark brush from the moving baggage car. They broke the safe with cold chisels along the Silver Creek embankment.",
        historicalContext = "Dictated into an early acoustic phonograph horn reconstructed from Western Union witness affidavits filed at the Scott County courthouse.",
        linkedRelicId = "relic_reno_double_eagle",
        cylinderColorHex = 0xFFF59E0B
    )

    // 2. War of 1812 Wyandotte Cavern Gunpowder Works
    val CYLINDER_WYANDOTTE_GUNPOWDER = WaxCylinderRecording(
        id = "cylinder_wyandotte_gunpowder",
        title = "Dr. Samuel Adams' Saltpeter Vat Ledger",
        recordedDate = "October 1812",
        frequencyKhz = 720,
        eyewitnessName = "Dr. Samuel Adams",
        speakerRole = "Surgeon & Ordnance Contractor, 1812 War Works",
        siteId = "crawford_wyandotte_saltpeter",
        mysteryId = "saltpeter_1812",
        transcript = "We burn cedar torches four miles into the great cavern room. The earth is rich with nitre left by thousands of bats through ancient centuries. Sixty pioneer laborers boil the leaching vats over roaring hickory fires night and day. Without this powder, General Harrison's frontier regiments at Fort Meigs would have fallen before British siege artillery.",
        historicalContext = "Excerpted from Dr. Adams' field journals recovered from the Harrison County historical archives.",
        linkedRelicId = "relic_saltpeter_powder_horn",
        cylinderColorHex = 0xFF10B981
    )

    // 3. Town Clock Church Abolitionist Watchman
    val CYLINDER_TOWN_CLOCK_BELFRY = WaxCylinderRecording(
        id = "cylinder_town_clock_belfry",
        title = "Watchman's Signal on the Ohio River Bend",
        recordedDate = "September 14, 1858",
        frequencyKhz = 990,
        eyewitnessName = "Deacon George W. Carter",
        speakerRole = "Second Presbyterian Church Lookout & Conductor",
        siteId = "floyd_town_clock_church",
        mysteryId = "freedom_network",
        transcript = "From the upper louvers, one hundred and sixty feet high, the lights of Portland and Louisville twinkle across the dark waters. When the steam packet docks at eleven, I adjust the bullseye lantern to shine due south toward the Kentucky willow bend. That single amber gleam tells freedom seekers the crossing skiff is manned and the sub-cellar trapdoor is unbolted.",
        historicalContext = "Transcribed from oral testimonies collected by the New Albany Underground Railroad Historical Committee.",
        linkedRelicId = "relic_freedom_star_compass",
        cylinderColorHex = 0xFF38BDF8
    )

    // 4. Duncan Railroad Tunnel Night Drill
    val CYLINDER_DUNCAN_TUNNEL_BLASTER = WaxCylinderRecording(
        id = "cylinder_duncan_tunnel_blaster",
        title = "Night Blasting in Edwardsville Mountain",
        recordedDate = "April 1881",
        frequencyKhz = 650,
        eyewitnessName = "Patrick Callahan",
        speakerRole = "Sandstone Blasting Foreman, Air-Line Railroad",
        siteId = "floyd_duncan_tunnel",
        mysteryId = "reno_gold",
        transcript = "Four thousand two hundred feet of solid Edwardsville limestone and shale. Two hundred men swinging eight-pound sledges by candlelight with sulfur smoke choking the headers. We struck a subterranean spring that nearly flooded the western portal. They say when the wind blows east through the tunnel bore at midnight, you can still hear the steam drills hammering.",
        historicalContext = "Recorded from an interview preserved by the Southern Indiana Railroad Historical Society.",
        linkedRelicId = "relic_tunnel_star_chisel",
        cylinderColorHex = 0xFFF97316
    )

    // 5. Ohio River Steamboat Eclipse
    val CYLINDER_STEAMBOAT_ECLIPSE = WaxCylinderRecording(
        id = "cylinder_steamboat_eclipse",
        title = "Steamboat Eclipse Midnight Speed Record",
        recordedDate = "May 1853",
        frequencyKhz = 1120,
        eyewitnessName = "Captain Edward Sturgeon",
        speakerRole = "Master & Steamboat Pilot, New Albany Shipyards",
        siteId = "floyd_steamboat_eclipse",
        mysteryId = null,
        transcript = "Three hundred and sixty-five feet from stem to stern, built right here in New Albany by Dowerman and Hummel! She was the queen of the western waters. When we raced the A.L. Shotwell from New Orleans to Louisville in four days, nine hours, and thirty minutes, our paddlewheels churned the river to boiling foam. New Albany built the fastest wooden hulls the Mississippi ever carried.",
        historicalContext = "Preserved in the riverboat captains' logs of the Howard Steamboat Museum collection.",
        linkedRelicId = "relic_steamboat_gold_token",
        cylinderColorHex = 0xFFD4AF37,
        isUnlockedByDefault = true
    )

    // 6. Falls of the Ohio Silurian Coral Explorer
    val CYLINDER_FALLS_FOSSILS = WaxCylinderRecording(
        id = "cylinder_falls_fossils",
        title = "Silurian Coral Beds of the Falls",
        recordedDate = "July 1874",
        frequencyKhz = 910,
        eyewitnessName = "Prof. George K. Greene",
        speakerRole = "Pioneer Paleontologist & Geological Surveyor",
        siteId = "clark_falls_of_the_ohio",
        mysteryId = null,
        transcript = "Three hundred and eighty-six million years ago, a warm Devonian sea covered what is now Clarksville. When the river drops in midsummer, walking the limestone flats is like strolling across an ancient ocean floor. The fossil horn corals, crinoid stems, and stromatoporoids are frozen in exquisite detail beneath the roaring rapids of the falls.",
        historicalContext = "Transcribed from Indiana Department of Geology scientific field survey bulletins.",
        linkedRelicId = "relic_fossil_horn_coral",
        cylinderColorHex = 0xFF06B6D4,
        isUnlockedByDefault = true
    )

    // 7. Beck's Mill Historic Waterwheel
    val CYLINDER_BECKS_MILL_RACE = WaxCylinderRecording(
        id = "cylinder_becks_mill_race",
        title = "The French Buhr Millstones of Blue River",
        recordedDate = "August 1864",
        frequencyKhz = 780,
        eyewitnessName = "George Beck Jr.",
        speakerRole = "Master Miller & Millwright",
        siteId = "washington_becks_mill",
        mysteryId = null,
        transcript = "My father George built the first log mill here in 1808 while the territory was still wild wilderness. Our waterwheel turns two run of genuine French Buhr stones brought overland by wagon from Philadelphia. In the drought of sixty-four, when every creek in Washington County dried to dust, the Beck spring still gushed five million gallons a day.",
        historicalContext = "From Beck Family memoirs compiled by the Washington County Historical Society.",
        linkedRelicId = "relic_millstone_chisel",
        cylinderColorHex = 0xFFEAB308
    )

    // 8. Buffalo Trace Courier
    val CYLINDER_BUFFALO_TRACE = WaxCylinderRecording(
        id = "cylinder_buffalo_trace",
        title = "Courier on the Ancient Buffalo Trace",
        recordedDate = "November 1811",
        frequencyKhz = 580,
        eyewitnessName = "Silas McCammon",
        speakerRole = "Territorial Dispatch Rider",
        siteId = "floyd_knobs_ridge_mystery",
        mysteryId = null,
        transcript = "The trace is twenty feet wide, beaten down hard as iron by centuries of bison herds traveling between the Illinois prairies and the Kentucky salt licks. Carrying army dispatches between Vincennes and Clarksville, you stick to the highland ridges. Down in the creek bottoms, a rider can be ambushed before he even smells cedar smoke.",
        historicalContext = "Territorial postal courier memoirs preserved in the Indiana Historical Bureau.",
        linkedRelicId = "relic_frontier_powder_measure",
        cylinderColorHex = 0xFF84CC16
    )

    // 9. Eleutherian Abolitionist College
    val CYLINDER_ELEUTHERIAN_TRUSTEE = WaxCylinderRecording(
        id = "cylinder_eleutherian_trustee",
        title = "Neil's Creek Abolitionist Roll Call",
        recordedDate = "June 1855",
        frequencyKhz = 1040,
        eyewitnessName = "Rev. Thomas Craven",
        speakerRole = "Founder & Trustee, Eleutherian College",
        siteId = "jefferson_eleutherian_college",
        mysteryId = "freedom_network",
        transcript = "We laid every stone of this three-story hall with our own hands upon the high limestone ridge. We took our name from 'eleutheros'—the Greek word for freedom. Here, black and white students, women and men, sit at the very same recitation desks. Men from across the river threatened to burn us to the ground, but we keep our doors open under God's heaven.",
        historicalContext = "Abolitionist congregation minutes held at the Lancaster Historical Collection.",
        linkedRelicId = "relic_abolitionist_seal",
        cylinderColorHex = 0xFFEC4899
    )

    // 10. Culbertson Mansion Grand Salon
    val CYLINDER_CULBERTSON_MANSION = WaxCylinderRecording(
        id = "cylinder_culbertson_mansion",
        title = "River Baron Culbertson's Gilded Ledger",
        recordedDate = "October 1869",
        frequencyKhz = 890,
        eyewitnessName = "William S. Culbertson",
        speakerRole = "Dry Goods Merchant & Indiana Philanthropist",
        siteId = "floyd_culbertson_mansion",
        mysteryId = null,
        transcript = "Twenty-five rooms, hand-painted fresco ceilings, and rosewood balustrades carved by Scottish artisans. When I arrived in New Albany in thirty-five, I had two dollars and a silver watch. The river commerce made this city the wealthiest port in Indiana. A man who prospers on the Ohio must give back: to build children's homes, libraries, and college halls that outlast his name.",
        historicalContext = "Culbertson family correspondence curated by the Indiana State Museum and Historic Sites.",
        linkedRelicId = "relic_culbertson_gilded_key",
        cylinderColorHex = 0xFFA855F7
    )

    val allCylinders = listOf(
        CYLINDER_RENO_MESSENGER,
        CYLINDER_WYANDOTTE_GUNPOWDER,
        CYLINDER_TOWN_CLOCK_BELFRY,
        CYLINDER_DUNCAN_TUNNEL_BLASTER,
        CYLINDER_STEAMBOAT_ECLIPSE,
        CYLINDER_FALLS_FOSSILS,
        CYLINDER_BECKS_MILL_RACE,
        CYLINDER_BUFFALO_TRACE,
        CYLINDER_ELEUTHERIAN_TRUSTEE,
        CYLINDER_CULBERTSON_MANSION
    )

    // --- Curated Historical Relics ---
    val RELIC_RENO_DOUBLE_EAGLE = HistoricalRelic(
        id = "relic_reno_double_eagle",
        title = "1868 Reno Gold Double Eagle",
        era = "1868 Post-Civil War Frontier",
        material = "Solid 90% Pure Mint Gold",
        description = "A heavy \$20 gold piece bearing the New Orleans mint mark, recovered from the abandoned Adams Express messenger safe hidden along Silver Creek.",
        provenance = "Recovered from hollow rail embankment near Marshfield Station",
        linkedSiteId = "marshfield_train_robbery",
        linkedMysteryId = "reno_gold",
        rarity = RelicRarity.LEGENDARY,
        badgeIconName = "MonetizationOn",
        primaryColorHex = 0xFFF59E0B
    )

    val RELIC_SALTPETER_POWDER_HORN = HistoricalRelic(
        id = "relic_saltpeter_powder_horn",
        title = "1812 Carved Saltpeter Powder Horn",
        era = "1812 War of 1812",
        material = "Carved Ox Horn with Brass Spout",
        description = "Scrimshaw-inscribed powder horn etched with a map of Wyandotte Cave's Indigo Room and the 1812 ordnance inspector's star stamp.",
        provenance = "Discovered resting on a dry limestone leaching ledge deep inside Wyandotte Caverns",
        linkedSiteId = "crawford_wyandotte_saltpeter",
        linkedMysteryId = "saltpeter_1812",
        rarity = RelicRarity.LEGENDARY,
        badgeIconName = "OfflineBolt",
        primaryColorHex = 0xFF10B981
    )

    val RELIC_FREEDOM_STAR_COMPASS = HistoricalRelic(
        id = "relic_freedom_star_compass",
        title = "Freedom Star Brass Pocket Compass",
        era = "1858 Antebellum Abolition Era",
        material = "Heavy Polished Brass & Magnetic Steel",
        description = "An antique mariner's pocket compass with a sunburst star engraved into the lid, carried by conductors shuttling refugees from New Albany to Salem.",
        provenance = "Preserved in the sub-cellar secret vault beneath the Town Clock Church belfry",
        linkedSiteId = "floyd_town_clock_church",
        linkedMysteryId = "freedom_network",
        rarity = RelicRarity.LEGENDARY,
        badgeIconName = "Explore",
        primaryColorHex = 0xFF38BDF8
    )

    val RELIC_TUNNEL_STAR_CHISEL = HistoricalRelic(
        id = "relic_tunnel_star_chisel",
        title = "Hand-Forged Star Drill Chisel",
        era = "1881 Industrial Railroad Boom",
        material = "Tempered Tool Steel",
        description = "A four-point hand star drill used by tunnel blasters to bore dynamite blast holes through Edwardsville sandstone inside Duncan Mountain.",
        provenance = "Found near the western drainage conduit of Duncan Tunnel",
        linkedSiteId = "floyd_duncan_tunnel",
        linkedMysteryId = "reno_gold",
        rarity = RelicRarity.RARE,
        badgeIconName = "Build",
        primaryColorHex = 0xFFF97316
    )

    val RELIC_STEAMBOAT_GOLD_TOKEN = HistoricalRelic(
        id = "relic_steamboat_gold_token",
        title = "1853 Steamboat Eclipse Gilded Token",
        era = "1853 Golden Age of Riverboats",
        material = "Gilded Brass Fare Medallion",
        description = "Commemorative parlor fare token stamped with the profile of the record-breaking New Albany sidewheeler Steamboat Eclipse.",
        provenance = "Recovered from silt deposits along the New Albany river wharves",
        linkedSiteId = "floyd_steamboat_eclipse",
        linkedMysteryId = null,
        rarity = RelicRarity.COMMON,
        badgeIconName = "DirectionsBoat",
        primaryColorHex = 0xFFD4AF37
    )

    val RELIC_FOSSIL_HORN_CORAL = HistoricalRelic(
        id = "relic_fossil_horn_coral",
        title = "Petrified Devonian Horn Coral",
        era = "386 Million B.C. Paleozoic Era",
        material = "Crystallized Calcite & Limestone",
        description = "A fossilized rugose horn coral (Streptelasma) washed clean by the Ohio River rapids, preserving pre-dinosaur marine sea life.",
        provenance = "Gathered from the exposed upper limestone ledges at the Falls of the Ohio",
        linkedSiteId = "clark_falls_of_the_ohio",
        linkedMysteryId = null,
        rarity = RelicRarity.COMMON,
        badgeIconName = "Spa",
        primaryColorHex = 0xFF06B6D4
    )

    val RELIC_MILLSTONE_CHISEL = HistoricalRelic(
        id = "relic_millstone_chisel",
        title = "Pioneer French Buhr Dressing Hammer",
        era = "1864 Civil War Era Pioneer Craft",
        material = "Forged Iron with Hickory Handle",
        description = "A specialized pick hammer used by pioneer millers to sharpen the cutting furrows of quartz French Buhr millstones at Beck's Mill.",
        provenance = "Preserved in the original millwright toolbox at Beck's Mill",
        linkedSiteId = "washington_becks_mill",
        linkedMysteryId = null,
        rarity = RelicRarity.RARE,
        badgeIconName = "Architecture",
        primaryColorHex = 0xFFEAB308
    )

    val RELIC_FRONTIER_POWDER_MEASURE = HistoricalRelic(
        id = "relic_frontier_powder_measure",
        title = "Engraved Frontier Powder Measure",
        era = "1811 Indiana Territorial Frontier",
        material = "Carved Antler Tine",
        description = "A graduated black powder measure used by dispatch couriers and rangers riding the ancient Buffalo Trace between Vincennes and Louisville.",
        provenance = "Recovered near an 1811 stone surveyor bench mark on the Knobs ridge",
        linkedSiteId = "floyd_knobs_ridge_mystery",
        linkedMysteryId = null,
        rarity = RelicRarity.RARE,
        badgeIconName = "Straighten",
        primaryColorHex = 0xFF84CC16
    )

    val RELIC_ABOLITIONIST_SEAL = HistoricalRelic(
        id = "relic_abolitionist_seal",
        title = "Abolitionist Anti-Slavery Hand Stamp",
        era = "1855 Antebellum Era",
        material = "Carved Boxwood & Lead Seal",
        description = "An authentic embossed seal used on anti-slavery pamphlets and tuition receipts for Eleutherian College students.",
        provenance = "Lancaster abolitionist archives collection",
        linkedSiteId = "jefferson_eleutherian_college",
        linkedMysteryId = "freedom_network",
        rarity = RelicRarity.RARE,
        badgeIconName = "VerifiedUser",
        primaryColorHex = 0xFFEC4899
    )

    val RELIC_CULBERTSON_GILDED_KEY = HistoricalRelic(
        id = "relic_culbertson_gilded_key",
        title = "Culbertson Carriage House Gilded Key",
        era = "1869 Gilded Age",
        material = "Gold-Plated Brass",
        description = "An ornate skeleton key for the Culbertson Mansion carriage house and silver vault.",
        provenance = "Recovered from the Culbertson estate private archives",
        linkedSiteId = "floyd_culbertson_mansion",
        linkedMysteryId = null,
        rarity = RelicRarity.RARE,
        badgeIconName = "Key",
        primaryColorHex = 0xFFA855F7
    )

    val allRelics = listOf(
        RELIC_RENO_DOUBLE_EAGLE,
        RELIC_SALTPETER_POWDER_HORN,
        RELIC_FREEDOM_STAR_COMPASS,
        RELIC_TUNNEL_STAR_CHISEL,
        RELIC_STEAMBOAT_GOLD_TOKEN,
        RELIC_FOSSIL_HORN_CORAL,
        RELIC_MILLSTONE_CHISEL,
        RELIC_FRONTIER_POWDER_MEASURE,
        RELIC_ABOLITIONIST_SEAL,
        RELIC_CULBERTSON_GILDED_KEY
    )

    fun isCylinderUnlocked(
        cylinder: WaxCylinderRecording,
        visitedSiteIds: Set<String>,
        solvedCipherIds: Set<String>
    ): Boolean {
        if (cylinder.isUnlockedByDefault) return true
        if (visitedSiteIds.contains(cylinder.siteId)) return true
        if (cylinder.mysteryId != null) {
            val matchingCipher = CuratedMysteries.allCiphers.firstOrNull { it.mysteryId == cylinder.mysteryId }
            if (matchingCipher != null && solvedCipherIds.contains(matchingCipher.id)) {
                return true
            }
        }
        return false
    }

    fun isRelicUnlocked(
        relic: HistoricalRelic,
        visitedSiteIds: Set<String>,
        solvedCipherIds: Set<String>
    ): Boolean {
        if (visitedSiteIds.contains(relic.linkedSiteId)) return true
        if (relic.linkedMysteryId != null) {
            val matchingCipher = CuratedMysteries.allCiphers.firstOrNull { it.mysteryId == relic.linkedMysteryId }
            if (matchingCipher != null && solvedCipherIds.contains(matchingCipher.id)) {
                return true
            }
        }
        return false
    }
}
