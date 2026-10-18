package com.example.data.model

object RelicRegistry {

    val CRINOID_FOSSIL = RelicSpecimen(
        id = "relic_fossil_horn_coral",
        title = "Devonian Crinoid Stem & Horn Coral",
        era = "386 Million B.C. • Paleozoic Reef",
        material = "Silicified Calcite & Louisville Limestone",
        provenance = "Recovered from the upper limestone shelves along the Falls of the Ohio rapids.",
        siteId = "clark_falls_of_the_ohio",
        siteName = "Falls of the Ohio Fossil Beds",
        lat = 38.2770,
        lng = -85.7628,
        reliefType = ReliefTextureType.CRINOID_FOSSIL,
        rarity = RelicRarity.COMMON,
        primaryColorHex = 0xFF06B6D4,
        inscriptions = listOf(
            "DEVONIAN REEF • 386M B.C.",
            "FALLS OF THE OHIO • LOUISVILLE LIMESTONE",
            "CRINOID STEM • SEA LILY CALYX"
        ),
        curatorNotes = "Silicified invertebrate fossil preserving marine coral colonies and segmented crinoid columnals from the inland Devonian shallow sea.",
        archivalFrequencyKhz = 1040,
        archivalBroadcastTitle = "Field Report: Devonian Bed Stratigraphy (1875)",
        archivalDispatchTranscript = "This is State Geologist E.T. Cox reporting from the lower reef shelves. At low water between Clarksville and Louisville, miles of bare Devonian limestone emerge. We have cataloged twenty-four species of horn corals, including massive colonies of Favosites, their hexagonal cell tubes perfectly silicified against river erosion.",
        speakerName = "Prof. Edward T. Cox",
        speakerRole = "Indiana State Geologist & Paleontologist",
        reliefFeatures = listOf(
            RelicReliefFeature(0.50f, 0.50f, 0.38f, 10f, RelicReliefFeature.FeatureType.CONCENTRIC_RINGS, "Calyx Crown"),
            RelicReliefFeature(0.50f, 0.50f, 0.28f, 7f, RelicReliefFeature.FeatureType.CONCENTRIC_RINGS, "Columnal Wall"),
            RelicReliefFeature(0.50f, 0.50f, 0.16f, 5f, RelicReliefFeature.FeatureType.CONCENTRIC_RINGS, "Lumen Canal"),
            RelicReliefFeature(0.50f, 0.50f, 0.42f, 4f, RelicReliefFeature.FeatureType.INSCRIPTION_ARC, "DEVONIAN • 386M B.C."),
            RelicReliefFeature(0.25f, 0.35f, 0.15f, 6f, RelicReliefFeature.FeatureType.FLORAL_FILIGREE, "Septa Ribs"),
            RelicReliefFeature(0.75f, 0.65f, 0.18f, 6f, RelicReliefFeature.FeatureType.CROSS_HATCH, "Coral Calcite")
        )
    )

    val STEAMBOAT_MEDALLION = RelicSpecimen(
        id = "relic_steamboat_gold_token",
        title = "1853 Steamboat Eclipse Gilded Medallion",
        era = "1853 • Golden Age of Riverboats",
        material = "Gilded Admiralty Brass & Stamped Pewter",
        provenance = "Recovered from silt deposits near the old New Albany shipyard drydocks.",
        siteId = "floyd_steamboat_eclipse",
        siteName = "Steamboat Eclipse Launch Site",
        lat = 38.2865,
        lng = -85.8234,
        reliefType = ReliefTextureType.STEAMBOAT_MEDALLION,
        rarity = RelicRarity.COMMON,
        primaryColorHex = 0xFFD4AF37,
        inscriptions = listOf(
            "STEAMBOAT ECLIPSE • NEW ALBANY IND",
            "RECORD: N.O. TO LOUISVILLE 4D 9H 30M",
            "DOWERMAN & HUMMEL SHIPBUILDERS 1853"
        ),
        curatorNotes = "Commemorative parlor fare medallion celebrating the fastest wooden sidewheel steamboat ever built on the Ohio River. Length 365 feet.",
        archivalFrequencyKhz = 1120,
        archivalBroadcastTitle = "River Dispatch: Eclipse Midnight Record (1853)",
        archivalDispatchTranscript = "Attention river packets: Captain Edward Sturgeon reports the Eclipse has passed Portland wharf under full steam, establishing the unbroken western water record of four days, nine hours, thirty minutes from New Orleans. Built in New Albany, she has eclipsed all competing craft on the Mississippi.",
        speakerName = "Capt. Edward Sturgeon",
        speakerRole = "Master & Steamboat Pilot, New Albany Shipyards",
        reliefFeatures = listOf(
            RelicReliefFeature(0.50f, 0.50f, 0.40f, 8f, RelicReliefFeature.FeatureType.CONCENTRIC_RINGS, "Rim Bead"),
            RelicReliefFeature(0.50f, 0.50f, 0.35f, 5f, RelicReliefFeature.FeatureType.INSCRIPTION_ARC, "STEAMBOAT ECLIPSE"),
            RelicReliefFeature(0.50f, 0.50f, 0.22f, 6f, RelicReliefFeature.FeatureType.GEAR_TEETH, "Paddlewheel Flange"),
            RelicReliefFeature(0.50f, 0.50f, 0.10f, 8f, RelicReliefFeature.FeatureType.STAR_EMBOSS, "Pilot Star"),
            RelicReliefFeature(0.50f, 0.70f, 0.18f, 4f, RelicReliefFeature.FeatureType.INSCRIPTION_ARC, "NEW ALBANY 1853")
        )
    )

    val RENO_DOUBLE_EAGLE = RelicSpecimen(
        id = "relic_reno_double_eagle",
        title = "1868 Reno Gold Double Eagle",
        era = "1868 • Post-Civil War Outlaw Frontier",
        material = "Solid 90% Pure Mint Gold (20 Dollar Specimen)",
        provenance = "Recovered from a buried iron messenger lockbox along the Silver Creek rail embankment.",
        siteId = "marshfield_train_robbery",
        siteName = "Marshfield Express Robbery Site",
        lat = 38.5601,
        lng = -85.7725,
        reliefType = ReliefTextureType.STEAMBOAT_MEDALLION,
        rarity = RelicRarity.LEGENDARY,
        primaryColorHex = 0xFFF59E0B,
        inscriptions = listOf(
            "UNITED STATES OF AMERICA • TWENTY D.",
            "IN GOD WE TRUST • 1868 • NEW ORLEANS MINT",
            "ADAMS EXPRESS COMPANY VALUABLES LOCK"
        ),
        curatorNotes = "One of the recovered gold pieces from the Marshfield wood-station train robbery, carried out by the notorious Reno Gang in May 1868.",
        archivalFrequencyKhz = 840,
        archivalBroadcastTitle = "Sheriff's Wire: Marshfield Safe Recovery (1868)",
        archivalDispatchTranscript = "Seymour and Jeffersonville telegraph relay: The Adams Express safe broken by cold chisels at Silver Creek has been dragged from the mud. Among the ledger books, three double-eagle gold pieces bearing the 1868 stamp were retrieved. The Pinkerton detective agency remains in pursuit along the Ohio River.",
        speakerName = "Deputy US Marshal J. Thomas",
        speakerRole = "Scott County Sheriff & Pinkerton Escort",
        reliefFeatures = listOf(
            RelicReliefFeature(0.50f, 0.50f, 0.42f, 9f, RelicReliefFeature.FeatureType.CONCENTRIC_RINGS, "Milled Border"),
            RelicReliefFeature(0.50f, 0.45f, 0.25f, 7f, RelicReliefFeature.FeatureType.STAR_EMBOSS, "Heraldic Eagle Shield"),
            RelicReliefFeature(0.50f, 0.50f, 0.32f, 4f, RelicReliefFeature.FeatureType.INSCRIPTION_ARC, "UNITED STATES OF AMERICA"),
            RelicReliefFeature(0.50f, 0.72f, 0.16f, 4f, RelicReliefFeature.FeatureType.INSCRIPTION_ARC, "TWENTY DOLLARS • 1868"),
            RelicReliefFeature(0.50f, 0.26f, 0.08f, 5f, RelicReliefFeature.FeatureType.STAR_EMBOSS, "Glory Sunburst")
        )
    )

    val SALTPETER_POWDER_HORN = RelicSpecimen(
        id = "relic_saltpeter_powder_horn",
        title = "1812 Carved Saltpeter Powder Horn",
        era = "1812 • War of 1812 Frontier Defense",
        material = "Scrimshaw Oxhorn & Lathed Brass Spout",
        provenance = "Discovered resting on a dry subterranean leaching vat ledge inside Wyandotte Cave.",
        siteId = "crawford_wyandotte_saltpeter",
        siteName = "Wyandotte Cave Saltpeter Works",
        lat = 38.2272,
        lng = -86.2974,
        reliefType = ReliefTextureType.SALTPETER_POWDER_HORN,
        rarity = RelicRarity.LEGENDARY,
        primaryColorHex = 0xFF10B981,
        inscriptions = listOf(
            "WYANDOTTE NITRE WORKS • OCT 1812",
            "FOR GEN. HARRISON'S FRONTIER ORDNANCE",
            "DR. SAMUEL ADAMS SURGEON & CONTRACTOR"
        ),
        curatorNotes = "Carved oxhorn scrimshaw etched with cavern passages and a star inspection mark. Provided black powder ingredients for American frontier troops.",
        archivalFrequencyKhz = 720,
        archivalBroadcastTitle = "Frontier Dispatch: Wyandotte Saltpeter Vats (1812)",
        archivalDispatchTranscript = "This is Dr. Benjamin Adams from the dark chambers of Wyandotte. Our ox teams have hauled five thousand pounds of potassium nitrate crystals through the forest trace to Harrison's depot at Corydon. The cavern vats burn night and day; the frontier regiments will have powder for their field pieces.",
        speakerName = "Dr. Benjamin Adams",
        speakerRole = "Surgeon & Ordnance Contractor, 1812 War Works",
        reliefFeatures = listOf(
            RelicReliefFeature(0.50f, 0.50f, 0.40f, 7f, RelicReliefFeature.FeatureType.HORN_OUTLINE, "Curved Horn Profile"),
            RelicReliefFeature(0.35f, 0.45f, 0.16f, 5f, RelicReliefFeature.FeatureType.STAR_EMBOSS, "Ordnance Star"),
            RelicReliefFeature(0.60f, 0.55f, 0.20f, 4f, RelicReliefFeature.FeatureType.CROSS_HATCH, "Indigo Room Map"),
            RelicReliefFeature(0.50f, 0.32f, 0.25f, 4f, RelicReliefFeature.FeatureType.INSCRIPTION_ARC, "WYANDOTTE NITRE 1812")
        )
    )

    val FREEDOM_STAR_COMPASS = RelicSpecimen(
        id = "relic_freedom_star_compass",
        title = "Freedom Star Brass Pocket Compass",
        era = "1858 • Antebellum Abolition Network",
        material = "Burnished Brass Casing & Jeweled Needle",
        provenance = "Discovered in the sub-cellar chimney void of the Town Clock Church.",
        siteId = "floyd_town_clock_church",
        siteName = "Town Clock Church Belfry",
        lat = 38.2861,
        lng = -85.8242,
        reliefType = ReliefTextureType.FREEDOM_COMPASS,
        rarity = RelicRarity.LEGENDARY,
        primaryColorHex = 0xFF38BDF8,
        inscriptions = listOf(
            "TOWN CLOCK BELFRY • NEW ALBANY 1858",
            "FOLLOW THE DRINKING GOURD • NORTH STAR",
            "LIBERTY TO ALL CITIZENS"
        ),
        curatorNotes = "Antique pocket compass carried by Underground Railroad conductors guiding passengers across the Ohio River under midnight cover.",
        archivalFrequencyKhz = 990,
        archivalBroadcastTitle = "Watchman's Signal: Midnight Ohio Crossing (1858)",
        archivalDispatchTranscript = "Belfry observation log: The clock chimes eleven across the river fog. Through the western louvers, the amber bullseye lantern has been set due south toward Portland wharf. Three lanterns signal from the Kentucky willow bar; the crossing skiff is underway toward our cellar door.",
        speakerName = "Deacon George W. Carter",
        speakerRole = "Second Presbyterian Church Lookout & Conductor",
        reliefFeatures = listOf(
            RelicReliefFeature(0.50f, 0.50f, 0.42f, 10f, RelicReliefFeature.FeatureType.CONCENTRIC_RINGS, "Brass Casing Rim"),
            RelicReliefFeature(0.50f, 0.50f, 0.32f, 6f, RelicReliefFeature.FeatureType.COMPASS_ROSE, "Eight-Point Rose"),
            RelicReliefFeature(0.50f, 0.50f, 0.12f, 8f, RelicReliefFeature.FeatureType.STAR_EMBOSS, "Polaris North Star"),
            RelicReliefFeature(0.50f, 0.50f, 0.36f, 4f, RelicReliefFeature.FeatureType.INSCRIPTION_ARC, "FOLLOW THE DRINKING GOURD")
        )
    )

    val TUNNEL_STAR_CHISEL = RelicSpecimen(
        id = "relic_tunnel_star_chisel",
        title = "Duncan Mountain Star Drill Chisel",
        era = "1881 • Industrial Mountain Blasting",
        material = "Tempered Tool Steel & Cast Octagonal Shank",
        provenance = "Recovered from the flooded drainage culvert of Duncan Railroad Tunnel.",
        siteId = "floyd_duncan_tunnel",
        siteName = "Duncan Railroad Tunnel",
        lat = 38.3188,
        lng = -85.9221,
        reliefType = ReliefTextureType.TUNNEL_STAR_CHISEL,
        rarity = RelicRarity.RARE,
        primaryColorHex = 0xFFF97316,
        inscriptions = listOf(
            "AIR-LINE RAILROAD • EDWARDVILLE TUNNEL",
            "4,295 FEET SOLID LIMESTONE BORE",
            "APRIL 1881 • 8-LB SLEDGE HAMMER"
        ),
        curatorNotes = "Four-point hand star drill bit used by Irish and Italian miners to bore dynamite shot holes through Edwardsville sandstone.",
        archivalFrequencyKhz = 650,
        archivalBroadcastTitle = "Blasting Foreman's Log: Mountain Header (1881)",
        archivalDispatchTranscript = "Header shift report: Two hundred men holding drills under eight-pound sledges. The sulfur smoke from black powder clears slowly through the ventilation shaft. We broke through four thousand feet today; the Air-Line locomotive will steam through Edwardsville mountain before winter.",
        speakerName = "Patrick Callahan",
        speakerRole = "Sandstone Blasting Foreman, Air-Line Railroad",
        reliefFeatures = listOf(
            RelicReliefFeature(0.50f, 0.50f, 0.40f, 8f, RelicReliefFeature.FeatureType.CHISEL_BLADE, "Cruciform Bit"),
            RelicReliefFeature(0.50f, 0.50f, 0.25f, 6f, RelicReliefFeature.FeatureType.STAR_EMBOSS, "Four-Point Star"),
            RelicReliefFeature(0.50f, 0.50f, 0.35f, 4f, RelicReliefFeature.FeatureType.INSCRIPTION_ARC, "AIR-LINE RAILROAD 1881"),
            RelicReliefFeature(0.50f, 0.70f, 0.15f, 5f, RelicReliefFeature.FeatureType.CROSS_HATCH, "Shank Knurling")
        )
    )

    val FRENCH_BUHR_HAMMER = RelicSpecimen(
        id = "relic_millstone_chisel",
        title = "Pioneer French Buhr Dressing Hammer",
        era = "1864 • Civil War Pioneer Milling",
        material = "Hand-Forged Tool Iron & Hickory Wedge",
        provenance = "Found within the millwright chest at Beck's Mill in Washington County.",
        siteId = "washington_becks_mill",
        siteName = "Beck's Mill Grist Site",
        lat = 38.5670,
        lng = -86.1558,
        reliefType = ReliefTextureType.FRENCH_BUHR_MILLSTONE,
        rarity = RelicRarity.RARE,
        primaryColorHex = 0xFFEAB308,
        inscriptions = listOf(
            "BECKS MILL • ESTABLISHED 1808",
            "FRENCH BUHR QUARTZ DRESSING HAMMER",
            "FINE CORNMEAL & MIDDLINGS"
        ),
        curatorNotes = "Specialized dressing pick used to recut the sharp grinding furrows in imported French quartz millstones driven by the Mill Creek waterwheel.",
        archivalFrequencyKhz = 940,
        archivalBroadcastTitle = "Miller's Journal: Millstone Furrow Sharpening (1864)",
        archivalDispatchTranscript = "George Beck journal entry: When grinding red winter wheat for the Union army regiments, the stone face wears smooth within forty bushels. Every Saturday evening, we raise the runner stone with block and tackle and pick twelve new furrows into the quartz crystal face.",
        speakerName = "George Beck",
        speakerRole = "Master Miller & Pioneer Millwright",
        reliefFeatures = listOf(
            RelicReliefFeature(0.50f, 0.50f, 0.42f, 8f, RelicReliefFeature.FeatureType.CONCENTRIC_RINGS, "Millstone Rim"),
            RelicReliefFeature(0.50f, 0.50f, 0.32f, 6f, RelicReliefFeature.FeatureType.GEAR_TEETH, "Grinding Furrows"),
            RelicReliefFeature(0.50f, 0.50f, 0.18f, 7f, RelicReliefFeature.FeatureType.STAR_EMBOSS, "Iron Eye Bushing"),
            RelicReliefFeature(0.50f, 0.30f, 0.22f, 4f, RelicReliefFeature.FeatureType.INSCRIPTION_ARC, "BECK'S MILL • 1808")
        )
    )

    val CULBERTSON_KEY = RelicSpecimen(
        id = "relic_culbertson_gilded_key",
        title = "Culbertson Carriage House Gilded Key",
        era = "1869 • Gilded Age River Baron",
        material = "Ornate Chased Brass with Gold Vermeil",
        provenance = "Preserved in the private vault of the Culbertson Mansion, Main Street New Albany.",
        siteId = "floyd_culbertson_mansion",
        siteName = "Culbertson Mansion Historic Site",
        lat = 38.2863,
        lng = -85.8197,
        reliefType = ReliefTextureType.CULBERTSON_GILDED_KEY,
        rarity = RelicRarity.RARE,
        primaryColorHex = 0xFFA855F7,
        inscriptions = listOf(
            "CULBERTSON MANSION • NEW ALBANY 1869",
            "MAIN ST. CARRIAGE HOUSE & VAULT",
            "SCOTTISH CARVED ROSEWOOD"
        ),
        curatorNotes = "Ornate master skeleton key featuring pierced clover bow and stepped wards, recovered from William Culbertson's dry goods archives.",
        archivalFrequencyKhz = 890,
        archivalBroadcastTitle = "Culbertson Estate Ledger: Mansion Dedication (1869)",
        archivalDispatchTranscript = "William S. Culbertson address: Twenty-five rooms under slate mansard roofs. Our river commerce made this port prosperous beyond all Indiana towns. What wealth we gather from Ohio riverboats must build enduring monuments: orphans' homes, civic colleges, and halls of open learning.",
        speakerName = "William S. Culbertson",
        speakerRole = "Dry Goods Merchant & Indiana Philanthropist",
        reliefFeatures = listOf(
            RelicReliefFeature(0.50f, 0.28f, 0.20f, 7f, RelicReliefFeature.FeatureType.FLORAL_FILIGREE, "Pierced Bow Filigree"),
            RelicReliefFeature(0.50f, 0.50f, 0.12f, 8f, RelicReliefFeature.FeatureType.CONCENTRIC_RINGS, "Key Collar"),
            RelicReliefFeature(0.50f, 0.65f, 0.22f, 6f, RelicReliefFeature.FeatureType.KEY_BIT, "Stepped Bit Wards"),
            RelicReliefFeature(0.50f, 0.85f, 0.14f, 4f, RelicReliefFeature.FeatureType.INSCRIPTION_ARC, "CULBERTSON 1869")
        )
    )

    val allSpecimens: List<RelicSpecimen> = listOf(
        CRINOID_FOSSIL,
        STEAMBOAT_MEDALLION,
        RENO_DOUBLE_EAGLE,
        SALTPETER_POWDER_HORN,
        FREEDOM_STAR_COMPASS,
        TUNNEL_STAR_CHISEL,
        FRENCH_BUHR_HAMMER,
        CULBERTSON_KEY
    )

    fun getById(id: String): RelicSpecimen {
        return allSpecimens.find { it.id == id } ?: STEAMBOAT_MEDALLION
    }
}
