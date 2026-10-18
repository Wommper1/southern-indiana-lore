package com.example.data.model

object HistoricMapLayers {
    // 1937 Great Ohio River Flood Inundation Zone (APPROXIMATE GEOMETRY)
    // NOTE: 85.4 ft is the river stage gage reading at McAlpine Upper Gage (Louisville/Clarksville),
    // NOT a flat geographic flood elevation across New Albany. This polygon is a hand-digitized visual approximation.
    val flood1937BoundaryCoordinates: List<Pair<Double, Double>> = listOf(
        Pair(38.2700, -85.8450), // West New Albany floodwall zone
        Pair(38.2800, -85.8300), // Downtown New Albany Main & Market (flooded to 10-15 ft)
        Pair(38.2850, -85.8150), // East New Albany / Silver Creek confluence
        Pair(38.2840, -85.7950), // Clarksville riverfront
        Pair(38.2750, -85.7500), // Jeffersonville Spring St / riverfront
        Pair(38.3000, -85.7100), // Port Fulton shipyards
        Pair(38.3400, -85.6600), // Utica flats
        Pair(38.4200, -85.6300), // Rose Island submerged peninsula
        Pair(38.3300, -85.6400),
        Pair(38.2600, -85.7200),
        Pair(38.2500, -85.7700),
        Pair(38.2600, -85.8200),
        Pair(38.2700, -85.8450)
    )

    // The Buffalo Trace (Ancient Bison Highway across Southern Indiana)
    val buffaloTraceCoordinates: List<Pair<Double, Double>> = listOf(
        Pair(38.2831, -85.7876), // Falls of the Ohio (Rocky Ford crossing)
        Pair(38.3100, -85.8400), // New Albany Knobs foothills
        Pair(38.3500, -85.9100), // Edwardsville / Floyds Knobs gap
        Pair(38.3800, -86.0200), // Greenville ridge
        Pair(38.4100, -86.1500), // Hardinsburg
        Pair(38.4600, -86.3500), // Paoli mineral springs
        Pair(38.5200, -86.6000), // French Lick valley
        Pair(38.6800, -87.5200)  // Northwest to Vincennes
    )

    // Knobstone Escarpment Topographic Contour Lines (600ft, 800ft, 950ft ridgeline contours)
    // Runs north-south dividing the Ohio River lowlands from the interior highlands
    data class TopoContour(
        val elevationFeet: Int,
        val label: String,
        val coordinates: List<Pair<Double, Double>>
    )

    val knobstoneContours: List<TopoContour> = listOf(
        TopoContour(
            elevationFeet = 600,
            label = "600 ft Base Contour",
            coordinates = listOf(
                Pair(38.2350, -85.8900),
                Pair(38.2600, -85.8750),
                Pair(38.2900, -85.8600),
                Pair(38.3200, -85.8500),
                Pair(38.3600, -85.8400),
                Pair(38.4100, -85.8300),
                Pair(38.4700, -85.8200),
                Pair(38.5400, -85.8100)
            )
        ),
        TopoContour(
            elevationFeet = 800,
            label = "800 ft Mid-Slope Contour",
            coordinates = listOf(
                Pair(38.2400, -85.9050),
                Pair(38.2700, -85.8900),
                Pair(38.3000, -85.8750),
                Pair(38.3300, -85.8650),
                Pair(38.3750, -85.8550),
                Pair(38.4250, -85.8450),
                Pair(38.4850, -85.8350),
                Pair(38.5550, -85.8250)
            )
        ),
        TopoContour(
            elevationFeet = 950,
            label = "950 ft Knobstone Ridge Crest",
            coordinates = listOf(
                Pair(38.2450, -85.9200),
                Pair(38.2800, -85.9050),
                Pair(38.3150, -85.8900),
                Pair(38.3450, -85.8800),
                Pair(38.3900, -85.8700),
                Pair(38.4400, -85.8600),
                Pair(38.5000, -85.8500),
                Pair(38.5700, -85.8400)
            )
        )
    )
}
