package com.example.data.model

enum class SketchType {
    CLOCK_TOWER,
    RAIL_TUNNEL,
    CAVE_MOUTH,
    TRESTLE_BRIDGE,
    COTTON_MILL,
    BISON_TRACE
}

data class JournalClueEntry(
    val id: String,
    val mysteryId: String,
    val siteId: String,
    val title: String,
    val sketchType: SketchType,
    val handwrittenNote: String,
    val historicalMarginalia: String,
    val riddleQuestion: String,
    val isUnlocked: Boolean = false,
    val userPhotoUri: String? = null,
    val hasCharcoalRubbing: Boolean = false,
    val alignmentTargetDescription: String
)

data class CipherRiddle(
    val id: String,
    val mysteryId: String,
    val title: String,
    val historicalContext: String,
    val ciphertext: String,
    val plaintext: String,
    val shiftKey: Int,
    val hint: String,
    val isSolved: Boolean = false,
    val unlockedSecret: String,
    val targetSiteId: String
)

data class MysteryStoryline(
    val id: String,
    val title: String,
    val tagline: String,
    val historicalEra: String,
    val description: String,
    val clueIds: List<String>,
    val cipherRiddleId: String,
    val artifactTitle: String,
    val rewardBadgeTitle: String,
    val themeColorHex: Long = 0xFFD4AF37
)

object CipherEngine {

    fun decodeCaesar(ciphertext: String, shift: Int): String {
        val normalizedShift = ((shift % 26) + 26) % 26
        return ciphertext.map { char ->
            when (char) {
                in 'A'..'Z' -> {
                    val base = 'A'.code
                    val shifted = (char.code - base - normalizedShift + 26) % 26 + base
                    shifted.toChar()
                }
                in 'a'..'z' -> {
                    val base = 'a'.code
                    val shifted = (char.code - base - normalizedShift + 26) % 26 + base
                    shifted.toChar()
                }
                else -> char
            }
        }.joinToString("")
    }

    fun encodeCaesar(plaintext: String, shift: Int): String {
        val normalizedShift = ((shift % 26) + 26) % 26
        return plaintext.map { char ->
            when (char) {
                in 'A'..'Z' -> {
                    val base = 'A'.code
                    val shifted = (char.code - base + normalizedShift) % 26 + base
                    shifted.toChar()
                }
                in 'a'..'z' -> {
                    val base = 'a'.code
                    val shifted = (char.code - base + normalizedShift) % 26 + base
                    shifted.toChar()
                }
                else -> char
            }
        }.joinToString("")
    }
}

object CuratedMysteries {

    val RENO_CIPHER = CipherRiddle(
        id = "cipher_reno_gold",
        mysteryId = "reno_gold",
        title = "Reno Brothers Telegraph Cipher",
        historicalContext = "Recovered from an 1868 Western Union telegraph tape found hidden inside a hollow railroad tie near the Marshfield train robbery site.",
        ciphertext = "NVSK IHYZ JHJOLK HA ZPSCLY JYLLR ILUK",
        plaintext = "GOLD BARS CACHED AT SILVER CREEK BEND",
        shiftKey = 7,
        hint = "Shift the dial by lucky number seven (G -> N).",
        isSolved = false,
        unlockedSecret = "Coordinates reveal the old Silver Creek railroad crossing where the Adams Express safe was broken.",
        targetSiteId = "marshfield_train_robbery"
    )

    val SALTPETER_CIPHER = CipherRiddle(
        id = "cipher_saltpeter_1812",
        mysteryId = "saltpeter_1812",
        title = "1812 Ordnance Surveyor Cryptogram",
        historicalContext = "Carved with a surveyor's knife into a cedar plank deep inside Wyandotte Cave's subterranean gunpowder works.",
        ciphertext = "HIIT ZSMH GSRRIGXW FIPSA XLI QMPP VEGI",
        plaintext = "DEEP VOID CONNECTS BELOW THE MILL RACE",
        shiftKey = 4,
        hint = "Rotate the brass ring by four notches (D -> H).",
        isSolved = false,
        unlockedSecret = "A mapped limestone conduit links the subterranean saltpeter caverns to the surface karst springs.",
        targetSiteId = "crawford_wyandotte_saltpeter"
    )

    val FREEDOM_CIPHER = CipherRiddle(
        id = "cipher_freedom_network",
        mysteryId = "freedom_network",
        title = "Town Clock Celestial North Star Code",
        historicalContext = "Noted in an abolitionist journal from New Albany's Second Presbyterian Church, directing conductors along the Ohio River.",
        ciphertext = "UJWCNAW RW QRPQ KNUOAH OJLNB WXACQ BCJA",
        plaintext = "LANTERN IN HIGH BELFRY FACES NORTH STAR",
        shiftKey = 9,
        hint = "Rotate the dial nine positions eastward (L -> U).",
        isSolved = false,
        unlockedSecret = "The belfry window provided a clear line of sight directly across the river to freedom seekers waiting on the Kentucky shoreline.",
        targetSiteId = "floyd_town_clock_church"
    )

    val allCiphers = listOf(RENO_CIPHER, SALTPETER_CIPHER, FREEDOM_CIPHER)

    val allClues = listOf(
        // Reno Gold Mystery Clues
        JournalClueEntry(
            id = "clue_reno_1",
            mysteryId = "reno_gold",
            siteId = "marshfield_train_robbery",
            title = "The Marshfield Robbery Cut",
            sketchType = SketchType.RAIL_TUNNEL,
            handwrittenNote = "May 22, 1868: The world's first peacetime train robbery. John and Simeon Reno decoupled the locomotive, threw the express messenger out into the darkness, and escaped on horseback with \$96,000 in government bonds and gold coin.",
            historicalMarginalia = "Surveyor sketch of the rail gradient shows an abandoned spur running into the dense timber along Silver Creek.",
            riddleQuestion = "Look for the iron rail grade where the train came to a forced stop on the summit.",
            alignmentTargetDescription = "Align reticle with the railway corridor grade looking north toward Seymour."
        ),
        JournalClueEntry(
            id = "clue_reno_2",
            mysteryId = "reno_gold",
            siteId = "floyd_duncan_tunnel",
            title = "Duncan Mountain Secret Cut",
            sketchType = SketchType.RAIL_TUNNEL,
            handwrittenNote = "The longest railroad tunnel in Indiana (about 4,300 to 4,700 ft, depending on the source). Legend persists that Frank Reno used the dark subterranean ventilation shafts as a clandestine cache before his capture in Canada.",
            historicalMarginalia = "Water seeps through Edwardsville limestone joints. Notice the hand-chiseled sandstone portals from 1881.",
            riddleQuestion = "Examine the western portal stonework for carved initials and surveyor bench marks.",
            alignmentTargetDescription = "Frame the western arched stone portal within the optical lens crosshairs."
        ),
        JournalClueEntry(
            id = "clue_reno_3",
            mysteryId = "reno_gold",
            siteId = "crawford_wyandotte_marengo_karst",
            title = "The Karst Ridge Lookout",
            sketchType = SketchType.CAVE_MOUTH,
            handwrittenNote = "The Reno Gang vanished into the Crawford County karst sinks whenever Pinkerton detectives set out from Louisville. Dense cedar bluffs and sinkholes swallow horse tracks completely.",
            historicalMarginalia = "A limestone cave entrance overlooks the old stage trail leading toward the Ohio River ferry.",
            riddleQuestion = "Identify the cave entrance hidden beneath the limestone outcrop.",
            alignmentTargetDescription = "Align the horizon leveler with the southern ridge bluff overlooking the river basin."
        ),

        // 1812 Saltpeter Mystery Clues
        JournalClueEntry(
            id = "clue_saltpeter_1",
            mysteryId = "saltpeter_1812",
            siteId = "crawford_wyandotte_saltpeter",
            title = "Old Indigo Cave Gunpowder Works",
            sketchType = SketchType.CAVE_MOUTH,
            handwrittenNote = "During the War of 1812, Dr. Samuel Adams mined thousands of pounds of potassium nitrate from this cavern to supply gunpowder for General William Henry Harrison's army.",
            historicalMarginalia = "Cedar leaching vats, petrified soot torches, and hand-cut wooden trackways remain untouched deep within the twilight zone.",
            riddleQuestion = "How did 19th-century miners transport heavy powder barrels through unlit karst passages?",
            alignmentTargetDescription = "Align the reticle with the cavern mouth shadow boundary."
        ),
        JournalClueEntry(
            id = "clue_saltpeter_2",
            mysteryId = "saltpeter_1812",
            siteId = "harrison_squire_boone_caverns",
            title = "Squire Boone's Subterranean Stream",
            sketchType = SketchType.CAVE_MOUTH,
            handwrittenNote = "Discovered in 1790 by Daniel Boone's brother Squire while escaping pursuing hostile warriors. The subterranean stream provided continuous waterpower to turn the pioneer gristmill above.",
            historicalMarginalia = "Rimstone dams and underground waterfalls keep the cave temperature at a constant 54 degrees year-round.",
            riddleQuestion = "Track the surge of the underground river where it emerges from the limestone cliff.",
            alignmentTargetDescription = "Frame the historic waterwheel against the rushing limestone spring."
        ),
        JournalClueEntry(
            id = "clue_saltpeter_3",
            mysteryId = "saltpeter_1812",
            siteId = "crawford_marengo_cave",
            title = "The Pillar of the Constitution",
            sketchType = SketchType.CAVE_MOUTH,
            handwrittenNote = "Discovered in 1883 by two local school children, Orris and Blanche Hiestand. Massive underground halls harbor subterranean rivers that link to the mysterious Lost River aquifer.",
            historicalMarginalia = "The limestone layers date back 330 million years to the Mississippian marine sea.",
            riddleQuestion = "Observe the vertical joint fractures in the ceiling showing prehistoric water flow.",
            alignmentTargetDescription = "Align the spirit level with the horizontal ceiling strata of the main passage."
        ),

        // Underground Freedom Signals Clues
        JournalClueEntry(
            id = "clue_freedom_1",
            mysteryId = "freedom_network",
            siteId = "floyd_town_clock_church",
            title = "The Sentinel Town Clock Belfry",
            sketchType = SketchType.CLOCK_TOWER,
            handwrittenNote = "1852: Built by the Second Presbyterian Church abolitionist congregation. The 160-foot steeple towered over the Ohio River wharves, housing a concealed lantern that guided freedom seekers at night.",
            historicalMarginalia = "Secret sub-cellar chambers beneath the sanctuary provided refuge before refugees were shuttled north toward Salem.",
            riddleQuestion = "Spot the upper belfry louvered openings facing directly south across the Ohio River bend.",
            alignmentTargetDescription = "Center the tall clock tower belfry spire inside the gold viewfinder reticle."
        ),
        JournalClueEntry(
            id = "clue_freedom_2",
            mysteryId = "freedom_network",
            siteId = "washington_salem_underground_railroad",
            title = "The Salem Quaker Freedom Station",
            sketchType = SketchType.BISON_TRACE,
            handwrittenNote = "Quaker settlements in Washington County formed an unbroken underground corridor. Freedom seekers moving by night along the old Buffalo Trace were concealed in false-bottom wagons and root cellars.",
            historicalMarginalia = "Symbols stitched into quilts hanging on clotheslines (Wagon Wheel, Flying Geese, North Star) gave coded instructions on safe travel windows.",
            riddleQuestion = "Notice how the historical road follows ancient highland ridges to avoid muddy river valleys.",
            alignmentTargetDescription = "Align the crosshair along the historic ridge trail looking toward the Blue River."
        ),
        JournalClueEntry(
            id = "clue_freedom_3",
            mysteryId = "freedom_network",
            siteId = "jefferson_eleutherian_college",
            title = "Eleutherian Integrated Sanctuary",
            sketchType = SketchType.COTTON_MILL,
            handwrittenNote = "Founded in 1848 by the Neil's Creek Anti-Slavery Baptist Church. This stone college admitted students regardless of race or gender, operating openly as an abolitionist haven high above the Ohio River hills.",
            historicalMarginalia = "The three-story fieldstone hall sits atop a defensive bluff with panoramic sightlines over the river approach roads.",
            riddleQuestion = "Inspect the massive limestone blocks quarried and dressed entirely by local abolitionist volunteers.",
            alignmentTargetDescription = "Frame the three-story stone facade and cupola within the antique viewfinder brackets."
        )
    )

    val MYSTERY_RENO_GOLD = MysteryStoryline(
        id = "reno_gold",
        title = "The Reno Gang Express Gold Cache",
        tagline = "Stashed Gold & Sealed Tunnels of America's First Train Robbers",
        historicalEra = "1868 Post-Civil War Industrial Frontier",
        description = "Trace the shadowy trail of Frank, John, and Simeon Reno. From the historic Marshfield ambush and the subterranean darkness of Duncan Tunnel to limestone caverns in Crawford County, uncover the encrypted telegraph dispatches that point to their unrecovered gold cache.",
        clueIds = listOf("clue_reno_1", "clue_reno_2", "clue_reno_3"),
        cipherRiddleId = "cipher_reno_gold",
        artifactTitle = "Reno Golden Sovereign & Western Union Tape",
        rewardBadgeTitle = "Master Train Detective",
        themeColorHex = 0xFFF59E0B // Amber Gold
    )

    val MYSTERY_SALTPETER_POWDER = MysteryStoryline(
        id = "saltpeter_1812",
        title = "The 1812 Secret Gunpowder Caverns",
        tagline = "Saltpeter Vats, Lost Rivers & Prehistoric Karst Secrets",
        historicalEra = "1812 War of 1812 & Pioneer Settlement",
        description = "Venture deep into the Mississippian limestone karst vaults that armed Harrison's army during the War of 1812. Decode the ordnance surveyor's carved cipher to discover how subterranean rivers powered America's earliest frontier defense works.",
        clueIds = listOf("clue_saltpeter_1", "clue_saltpeter_2", "clue_saltpeter_3"),
        cipherRiddleId = "cipher_saltpeter_1812",
        artifactTitle = "1812 Saltpeter Powder Horn & Surveyor Plaque",
        rewardBadgeTitle = "Karst Spelunker Historian",
        themeColorHex = 0xFF10B981 // Emerald Forest
    )

    val MYSTERY_FREEDOM_NETWORK = MysteryStoryline(
        id = "freedom_network",
        title = "The Underground Freedom Star & Lantern Code",
        tagline = "Coded Belfries, Quaker Corridors & The Heroic Skyline",
        historicalEra = "1850–1865 Antebellum Abolition Era",
        description = "Discover the real-world symbology that led hundreds of freedom seekers north across the Ohio River. Decode the celestial lantern signal of the Town Clock Church and the quilt symbols of Salem's Quaker stations to reveal the clandestine escape corridor.",
        clueIds = listOf("clue_freedom_1", "clue_freedom_2", "clue_freedom_3"),
        cipherRiddleId = "cipher_freedom_network",
        artifactTitle = "Freedom Star Compass & Abolitionist Ledger",
        rewardBadgeTitle = "Freedom Trail Guardian",
        themeColorHex = 0xFF38BDF8 // River Mist Cyan
    )

    val allMysteries = listOf(
        MYSTERY_RENO_GOLD,
        MYSTERY_SALTPETER_POWDER,
        MYSTERY_FREEDOM_NETWORK
    )
}
