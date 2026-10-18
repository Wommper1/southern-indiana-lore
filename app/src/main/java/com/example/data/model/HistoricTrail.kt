package com.example.data.model

data class TrailWaypoint(
    val title: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val siteId: String? = null
)

data class HistoricTrail(
    val id: String,
    val title: String,
    val subtitle: String,
    val era: String,
    val description: String,
    val approximateMiles: Double,
    val pathCoordinates: List<Pair<Double, Double>>, // Lat, Lng polyline
    val waypoints: List<TrailWaypoint>,
    val badgeName: String
)

object RegionalHistoricTrails {
    val trails: List<HistoricTrail> = listOf(
        HistoricTrail(
            id = "underground_railroad_freedom_corridor",
            title = "Underground Railroad Freedom Corridor",
            subtitle = "Falls of the Ohio Crossing to Salem Underground Stations",
            era = "1830–1861 Antebellum Era",
            description = "Trace the daring northward escape network used by freedom seekers crossing the Ohio River from Louisville into New Albany, through the steep Floyds Knobs forest ravines, and into Quaker and abolitionist sanctuaries in Salem and Washington County.",
            approximateMiles = 34.5,
            pathCoordinates = listOf(
                Pair(38.2710, -85.7650), // Portland Wharf Crossing
                Pair(38.2847, -85.8239), // Town Clock Church
                Pair(38.3032, -85.8450), // West Spring St / Falling Run
                Pair(38.3300, -85.8600), // Floyds Knobs Gap
                Pair(38.4100, -85.9000), // Borden / Muddy Fork
                Pair(38.5200, -85.9800), // Pekin Station
                Pair(38.6050, -86.1010)  // Salem Freedom Sanctuary
            ),
            waypoints = listOf(
                TrailWaypoint(
                    title = "Portland Wharf Crossing Point",
                    description = "Skiffs crossed the Ohio River under cover of moonless fogs.",
                    latitude = 38.2710,
                    longitude = -85.7650
                ),
                TrailWaypoint(
                    title = "Town Clock Church Steeple",
                    description = "Lantern beacon and basement hiding sanctuary.",
                    latitude = 38.2847,
                    longitude = -85.8239,
                    siteId = "floyd_town_clock_church"
                ),
                TrailWaypoint(
                    title = "Falling Run Creek Ravine",
                    description = "Concealed passage beneath the Knobs escarpment.",
                    latitude = 38.3032,
                    longitude = -85.8450
                ),
                TrailWaypoint(
                    title = "Salem Quaker Underground Haven",
                    description = "Abolitionist refuge leading toward the Michigan and Canadian border.",
                    latitude = 38.6050,
                    longitude = -86.1010
                )
            ),
            badgeName = "Freedom Trail Scout"
        ),
        HistoricTrail(
            id = "morgans_raid_1863",
            title = "Morgan's Great Raid 1863 Route",
            subtitle = "Confederate Cavalry Invasion of Southern Indiana",
            era = "July 8–11, 1863 Civil War",
            description = "Follow the actual path of Confederate General John Hunt Morgan's 2,400 cavalrymen as they crossed the Ohio River at Mauckport, engaged the Harrison County Home Guard at Corydon, and raided eastward through Salem.",
            approximateMiles = 42.0,
            pathCoordinates = listOf(
                Pair(38.0260, -86.2050), // Brandenburg / Mauckport Crossing
                Pair(38.0772, -86.1770), // Mauckport Pike
                Pair(38.2122, -86.1264), // Corydon Battlefield
                Pair(38.3500, -86.0800), // Palmyra Depot
                Pair(38.5000, -86.0500), // Salem Rail Depot
                Pair(38.5500, -85.9000)  // East toward Vienna / Scottsburg
            ),
            waypoints = listOf(
                TrailWaypoint(
                    title = "Mauckport Ferry Landing",
                    description = "Confederate cavalry commandeered the steam ferries Alice Dean and J.T. McCombs.",
                    latitude = 38.0260,
                    longitude = -86.2050
                ),
                TrailWaypoint(
                    title = "Battle of Corydon Historic Site",
                    description = "The only official pitched Civil War battle fought on Indiana soil.",
                    latitude = 38.2122,
                    longitude = -86.1264,
                    siteId = "harrison_corydon_battlefield"
                ),
                TrailWaypoint(
                    title = "Salem Railroad Depot Burning",
                    description = "Morgan's raiders tore up tracks and demanded ransom from local mill owners.",
                    latitude = 38.5000,
                    longitude = -86.0500
                )
            ),
            badgeName = "Cavalry Scout"
        ),
        HistoricTrail(
            id = "monon_rail_and_tunnels",
            title = "Knobs Iron & Monon Rail Run",
            subtitle = "Engineered Gaps, Timber Trestles & Haunted Cut-Throughs",
            era = "1853–1930s Golden Age of Steam",
            description = "Explore the legendary railroad feats engineered to conquer the rugged Southern Indiana Knobs: from the Monon cut in New Albany up the steep grades of Borden and Tunnelton's massive stone portal.",
            approximateMiles = 58.0,
            pathCoordinates = listOf(
                Pair(38.2910, -85.8320), // Monon New Albany Yards
                Pair(38.3619, -85.9080), // Edwardsville Duncan Tunnel
                Pair(38.4680, -85.9450), // Borden Car Shops
                Pair(38.5900, -86.1500), // Mitchell Rail Crossing
                Pair(38.7617, -86.3422)  // Tunnelton Big Tunnel
            ),
            waypoints = listOf(
                TrailWaypoint(
                    title = "Duncan Tunnel Portal",
                    description = "Indiana's longest rail tunnel (4,295 ft), blasted through Knob shale in 1881.",
                    latitude = 38.3619,
                    longitude = -85.9080,
                    siteId = "floyd_duncan_tunnel"
                ),
                TrailWaypoint(
                    title = "Tunnelton Big Tunnel Portal",
                    description = "1,731 ft B&O rail tunnel built in 1857, famous for night watchman lore.",
                    latitude = 38.7617,
                    longitude = -86.3422,
                    siteId = "lawrence_tunnelton_big_tunnel"
                )
            ),
            badgeName = "Iron Rail Pioneer"
        )
    )
}
