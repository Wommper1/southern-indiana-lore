package com.example.ui.components

import android.graphics.Bitmap
import androidx.collection.LruCache
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.WaterDamage
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HistoricMapLayers
import com.example.data.model.HistoricTrail
import com.example.data.model.HistoricalBuildingFootprint
import com.example.data.model.HistoricalChangeEvent
import com.example.data.model.LoreCategories
import com.example.data.model.LoreSite
import com.example.data.model.RegionalHistoricTrails
import com.example.data.model.SanbornMicroAreaRepository
import com.example.ui.theme.GhostCrimson
import com.example.ui.theme.KnobstoneForest
import com.example.ui.theme.LanternAmber
import com.example.ui.theme.LanternAmberBright
import com.example.ui.theme.ParchmentPaper
import com.example.ui.theme.RiverMistBlue
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateNight
import com.example.ui.theme.SlateNightElevated
import com.example.util.LocationUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.PI
import kotlin.math.asinh
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.sinh
import kotlin.math.tan

// Global memory cache for map tiles
private val tileBitmapCache = LruCache<String, Bitmap>(128)

@Composable
fun InteractiveLoreMap(
    userLat: Double,
    userLng: Double,
    sites: List<LoreSite>,
    selectedSite: LoreSite?,
    onSiteSelected: (LoreSite) -> Unit,
    modifier: Modifier = Modifier
) {
    // Map center coordinates (defaults to New Albany/Floyd County)
    var centerLat by remember { mutableDoubleStateOf(userLat) }
    var centerLng by remember { mutableDoubleStateOf(userLng) }
    var zoomLevel by remember { mutableFloatStateOf(13.5f) }

    // Map layer toggles for history nerds & explorers
    var show1937FloodLine by remember { mutableStateOf(false) }
    var showBuffaloTrace by remember { mutableStateOf(false) }
    var showTopoContours by remember { mutableStateOf(true) }
    var selectedTrailId by remember { mutableStateOf<String?>(null) }
    var showLayersMenu by remember { mutableStateOf(false) }

    // Historical Time Machine State
    var showTimeMachineSheet by remember { mutableStateOf(false) }
    var timeMachineYear by remember { mutableIntStateOf(1937) }
    var timeMachineOpacity by remember { mutableFloatStateOf(0.75f) }

    // Street-Level Historical Exploration State ("Ghost Buildings" & "Lost Streets")
    var showGhostBuildings by remember { mutableStateOf(true) }
    var showLostStreets by remember { mutableStateOf(true) }
    var selectedFootprint by remember { mutableStateOf<HistoricalBuildingFootprint?>(null) }
    var activeFootprintChangeEvent by remember { mutableStateOf<HistoricalChangeEvent?>(null) }

    val coroutineScope = rememberCoroutineScope()
    val loadedTiles = remember { mutableStateMapOf<String, Bitmap>() }

    // Update center when user location changes significantly or on first load
    LaunchedEffect(userLat, userLng) {
        if (LocationUtils.calculateDistanceMeters(centerLat, centerLng, userLat, userLng) > 10000) {
            centerLat = userLat
            centerLng = userLng
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(24.dp))
            .background(SlateNight)
            .border(1.dp, SlateBorder, RoundedCornerShape(24.dp))
            .testTag("interactive_lore_map")
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        // Convert drag pixels to lat/lng delta based on current zoom
                        val n = 2.0.pow(zoomLevel.toDouble())
                        val dLng = -dragAmount.x * (360.0 / (256.0 * n))
                        val dLat = dragAmount.y * (180.0 / (256.0 * n))
                        centerLng = (centerLng + dLng).coerceIn(-180.0, 180.0)
                        centerLat = (centerLat + dLat).coerceIn(-85.0, 85.0)
                    }
                }
                .pointerInput(sites, centerLat, centerLng, zoomLevel) {
                    detectTapGestures { tapOffset ->
                        val width = size.width
                        val height = size.height
                        val z = zoomLevel.toInt()

                        // 1. Check which site marker was clicked
                        val clickedSite = sites.firstOrNull { site ->
                            val pt = latLngToPixel(
                                site.latitude, site.longitude,
                                centerLat, centerLng,
                                zoomLevel, width.toFloat(), height.toFloat()
                            )
                            val dx = tapOffset.x - pt.x
                            val dy = tapOffset.y - pt.y
                            (dx * dx + dy * dy) <= (32f * 32f)
                        }
                        if (clickedSite != null) {
                            onSiteSelected(clickedSite)
                            return@detectTapGestures
                        }

                        // 2. Check if a Historical Sanborn Footprint ("Ghost Building") was tapped
                        if (showGhostBuildings) {
                            val clickedFootprint = SanbornMicroAreaRepository.historicalFootprints.firstOrNull { fp ->
                                val pt = latLngToPixel(
                                    fp.centerLat, fp.centerLng,
                                    centerLat, centerLng,
                                    zoomLevel, width.toFloat(), height.toFloat()
                                )
                                val dx = tapOffset.x - pt.x
                                val dy = tapOffset.y - pt.y
                                (dx * dx + dy * dy) <= (36f * 36f)
                            }
                            if (clickedFootprint != null) {
                                selectedFootprint = clickedFootprint
                                activeFootprintChangeEvent = SanbornMicroAreaRepository.findChangeEventNear(
                                    clickedFootprint.centerLat,
                                    clickedFootprint.centerLng
                                )
                            }
                        }
                    }
                }
        ) {
            val width = size.width
            val height = size.height
            val z = zoomLevel.toInt().coerceIn(11, 16)

            // Calculate bounding tile range
            val centerTileX = lonToTileX(centerLng, z)
            val centerTileY = latToTileY(centerLat, z)

            val tilesAcross = (width / 256f).toInt() + 2
            val tilesDown = (height / 256f).toInt() + 2

            val minTileX = (centerTileX.toInt() - tilesAcross).coerceAtLeast(0)
            val maxTileX = centerTileX.toInt() + tilesAcross
            val minTileY = (centerTileY.toInt() - tilesDown).coerceAtLeast(0)
            val maxTileY = centerTileY.toInt() + tilesDown

            // Draw vector/raster tiles
            for (tx in minTileX..maxTileX) {
                for (ty in minTileY..maxTileY) {
                    val key = "$z/$tx/$ty"
                    val cachedBitmap = tileBitmapCache.get(key)

                    val tileOriginX = ((tx - centerTileX) * 256.0 + width / 2f).toFloat()
                    val tileOriginY = ((ty - centerTileY) * 256.0 + height / 2f).toFloat()

                    if (cachedBitmap != null) {
                        drawImage(
                            image = cachedBitmap.asImageBitmap(),
                            srcOffset = IntOffset.Zero,
                            srcSize = IntSize(256, 256),
                            dstOffset = IntOffset(tileOriginX.toInt(), tileOriginY.toInt()),
                            dstSize = IntSize(256, 256)
                        )
                    } else {
                        // Draw dark placeholder grid with coordinates
                        drawRect(
                            color = Color(0xFF0F172A),
                            topLeft = Offset(tileOriginX, tileOriginY),
                            size = androidx.compose.ui.geometry.Size(256f, 256f)
                        )
                        drawRect(
                            color = Color(0x2238BDF8),
                            topLeft = Offset(tileOriginX, tileOriginY),
                            size = androidx.compose.ui.geometry.Size(256f, 256f),
                            style = Stroke(1f)
                        )

                        // Request tile asynchronously
                        if (!loadedTiles.containsKey(key)) {
                            loadedTiles[key] = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
                            coroutineScope.launch(Dispatchers.IO) {
                                val bmp = com.example.util.OfflineBasemapRenderer.renderOfflineTile(z, tx, ty)
                                tileBitmapCache.put(key, bmp)
                                withContext(Dispatchers.Main) {
                                    loadedTiles[key] = bmp
                                }
                            }
                        }
                    }

                    // Render 1955 USGS Historical Quadrangle Georeferenced Raster Overlay if active
                    if (timeMachineYear == 1955 && timeMachineOpacity > 0.05f) {
                        val topoKey = "topo_1955/$z/$tx/$ty"
                        val topoBmp = tileBitmapCache.get(topoKey) ?: run {
                            val generated = com.example.util.UsgsHistoricalTopoRasterGenerator.render1955TopoTile(z, tx, ty)
                            if (generated != null) {
                                tileBitmapCache.put(topoKey, generated)
                            }
                            generated
                        }
                        if (topoBmp != null) {
                            drawImage(
                                image = topoBmp.asImageBitmap(),
                                srcOffset = IntOffset.Zero,
                                srcSize = IntSize(256, 256),
                                dstOffset = IntOffset(tileOriginX.toInt(), tileOriginY.toInt()),
                                dstSize = IntSize(256, 256),
                                alpha = timeMachineOpacity
                            )
                        }
                    }

                    // Render 1886 or 1905 Sanborn Fire Insurance Georeferenced Micro-Area Overlay if active
                    if ((timeMachineYear == 1886 || timeMachineYear == 1905) && timeMachineOpacity > 0.05f) {
                        val sanbornKey = "sanborn_${timeMachineYear}/$z/$tx/$ty"
                        val sanbornBmp = tileBitmapCache.get(sanbornKey) ?: run {
                            val generated = com.example.util.SanbornRasterTileGenerator.renderSanbornTile(timeMachineYear, z, tx, ty)
                            if (generated != null) {
                                tileBitmapCache.put(sanbornKey, generated)
                            }
                            generated
                        }
                        if (sanbornBmp != null) {
                            drawImage(
                                image = sanbornBmp.asImageBitmap(),
                                srcOffset = IntOffset.Zero,
                                srcSize = IntSize(256, 256),
                                dstOffset = IntOffset(tileOriginX.toInt(), tileOriginY.toInt()),
                                dstSize = IntSize(256, 256),
                                alpha = timeMachineOpacity
                            )
                        }
                    }
                }
            }

            // Draw Road/River grid accents (Ohio River & Knobstone outline overlay)
            drawCircle(
                color = Color(0x330284C7),
                radius = 300f,
                center = Offset(width / 2f, height + 80f),
                style = Stroke(30f)
            )

            // Draw 1937 Great Flood Inundation Zone Overlay (85.4 ft Crest)
            val renderFlood = show1937FloodLine || (timeMachineYear == 1937 && timeMachineOpacity > 0.05f)
            if (renderFlood) {
                val floodAlpha = if (timeMachineYear == 1937) timeMachineOpacity else 0.75f
                val floodPath = Path()
                HistoricMapLayers.flood1937BoundaryCoordinates.forEachIndexed { index, coord ->
                    val pt = latLngToPixel(coord.first, coord.second, centerLat, centerLng, zoomLevel, width, height)
                    if (index == 0) floodPath.moveTo(pt.x, pt.y) else floodPath.lineTo(pt.x, pt.y)
                }
                floodPath.close()

                // Semi-transparent muddy river flood water modulated by Time Machine opacity
                drawPath(floodPath, color = Color(0x550284C7).copy(alpha = 0.35f * floodAlpha))
                drawPath(floodPath, color = Color(0xCC38BDF8).copy(alpha = 0.8f * floodAlpha), style = Stroke(width = 3f))
            }

            // Draw The Buffalo Trace (Ancient Bison Migration Trail)
            if (showBuffaloTrace) {
                val tracePath = Path()
                HistoricMapLayers.buffaloTraceCoordinates.forEachIndexed { index, coord ->
                    val pt = latLngToPixel(coord.first, coord.second, centerLat, centerLng, zoomLevel, width, height)
                    if (index == 0) tracePath.moveTo(pt.x, pt.y) else tracePath.lineTo(pt.x, pt.y)
                }
                drawPath(tracePath, color = Color(0xDDCA8A04), style = Stroke(width = 4f))
            }

            // Draw Topographic Contour Lines (Knobstone Escarpment 600ft, 800ft, 950ft)
            if (showTopoContours) {
                HistoricMapLayers.knobstoneContours.forEach { contour ->
                    val contourPath = Path()
                    contour.coordinates.forEachIndexed { index, coord ->
                        val pt = latLngToPixel(coord.first, coord.second, centerLat, centerLng, zoomLevel, width, height)
                        if (index == 0) contourPath.moveTo(pt.x, pt.y) else contourPath.lineTo(pt.x, pt.y)
                    }
                    val (contourColor, strokeWidth) = when (contour.elevationFeet) {
                        950 -> Pair(Color(0xFFE2E8F0).copy(alpha = 0.65f), 2.5f) // Ridge peak contour
                        800 -> Pair(Color(0xFF94A3B8).copy(alpha = 0.5f), 1.8f)  // Mid-slope
                        else -> Pair(Color(0xFF64748B).copy(alpha = 0.4f), 1.2f) // Base contour
                    }
                    drawPath(contourPath, color = contourColor, style = Stroke(width = strokeWidth))
                }
            }

            // Draw Selected Curated Historic Trail
            selectedTrailId?.let { trailId ->
                val trail = RegionalHistoricTrails.trails.find { it.id == trailId }
                if (trail != null) {
                    val trailPath = Path()
                    trail.pathCoordinates.forEachIndexed { index, coord ->
                        val pt = latLngToPixel(coord.first, coord.second, centerLat, centerLng, zoomLevel, width, height)
                        if (index == 0) trailPath.moveTo(pt.x, pt.y) else trailPath.lineTo(pt.x, pt.y)
                    }
                    drawPath(trailPath, color = LanternAmberBright, style = Stroke(width = 5f))

                    // Draw waypoint pins
                    trail.waypoints.forEach { wp ->
                        val wpPt = latLngToPixel(wp.latitude, wp.longitude, centerLat, centerLng, zoomLevel, width, height)
                        drawCircle(color = Color(0xFF1E1100), radius = 9f, center = wpPt)
                        drawCircle(color = LanternAmber, radius = 6f, center = wpPt)
                    }
                }
            }

            // Draw site geometry corridors (LINES) and heritage areas (POLYGONS)
            sites.forEach { site ->
                if (site.geometryCoordinates.isNotBlank()) {
                    val coords = LocationUtils.parseCoordinates(site.geometryCoordinates)
                    if (coords.size >= 2) {
                        val geomPath = Path()
                        coords.forEachIndexed { idx, pt ->
                            val pixelPt = latLngToPixel(pt.lat, pt.lng, centerLat, centerLng, zoomLevel, width, height)
                            if (idx == 0) geomPath.moveTo(pixelPt.x, pixelPt.y) else geomPath.lineTo(pixelPt.x, pixelPt.y)
                        }
                        val isLine = site.siteType.equals("LINE", ignoreCase = true)
                        val isPolygon = site.siteType.equals("POLYGON", ignoreCase = true)
                        val isSel = selectedSite?.id == site.id
                        val geomColor = when {
                            site.radarType.equals("UNDERGROUND", ignoreCase = true) -> Color(0xFF06B6D4)
                            site.category == LoreCategories.STEAM_AND_RAIL -> LanternAmberBright
                            site.category == LoreCategories.HAUNTED -> GhostCrimson
                            else -> RiverMistBlue
                        }
                        if (isPolygon) {
                            geomPath.close()
                            drawPath(geomPath, color = geomColor.copy(alpha = if (isSel) 0.35f else 0.18f))
                            drawPath(geomPath, color = geomColor.copy(alpha = 0.8f), style = Stroke(width = if (isSel) 3f else 1.8f))
                        } else if (isLine) {
                            drawPath(geomPath, color = geomColor.copy(alpha = if (isSel) 0.95f else 0.7f), style = Stroke(width = if (isSel) 5f else 3f))
                        }
                    }
                }
            }

            // Draw Lost Streets Lineage Overlays (if enabled)
            if (showLostStreets) {
                SanbornMicroAreaRepository.lostStreets.forEach { lostSt ->
                    val streetPath = Path()
                    val startPt = latLngToPixel(lostSt.startCoord.first, lostSt.startCoord.second, centerLat, centerLng, zoomLevel, width, height)
                    val endPt = latLngToPixel(lostSt.endCoord.first, lostSt.endCoord.second, centerLat, centerLng, zoomLevel, width, height)
                    streetPath.moveTo(startPt.x, startPt.y)
                    streetPath.lineTo(endPt.x, endPt.y)

                    // Draw dashed/glowing historical street alignment
                    drawPath(
                        path = streetPath,
                        color = LanternAmberBright.copy(alpha = 0.85f),
                        style = Stroke(
                            width = 4.5f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 8f), 0f)
                        )
                    )
                }
            }

            // Draw Historical Sanborn Footprints ("Ghost Buildings")
            if (showGhostBuildings) {
                val footprints = SanbornMicroAreaRepository.historicalFootprints
                footprints.forEach { fp ->
                    // Match current time machine year if applicable, or show all when time machine year is Sanborn
                    val isApplicableYear = when (timeMachineYear) {
                        1886 -> fp.sourceYear == 1886
                        1905 -> fp.sourceYear == 1905
                        else -> true
                    }
                    if (isApplicableYear && fp.boundaryPolygon.size >= 3) {
                        val polyPath = Path()
                        fp.boundaryPolygon.forEachIndexed { idx, coord ->
                            val pt = latLngToPixel(coord.first, coord.second, centerLat, centerLng, zoomLevel, width, height)
                            if (idx == 0) polyPath.moveTo(pt.x, pt.y) else polyPath.lineTo(pt.x, pt.y)
                        }
                        polyPath.close()

                        // Color-code footprint by Sanborn building material standard
                        val matColor = Color(fp.material.colorHex)
                        val fillColor = matColor.copy(alpha = 0.40f)
                        val strokeColor = matColor

                        val isTapped = selectedFootprint?.id == fp.id
                        drawPath(polyPath, color = if (isTapped) Color.White.copy(alpha = 0.6f) else fillColor)
                        drawPath(
                            path = polyPath,
                            color = if (isTapped) LanternAmberBright else strokeColor,
                            style = Stroke(width = if (isTapped) 3.5f else 1.8f)
                        )

                        // Center marker / centroid indicator for tap guidance
                        val centerPt = latLngToPixel(fp.centerLat, fp.centerLng, centerLat, centerLng, zoomLevel, width, height)
                        drawCircle(
                            color = strokeColor.copy(alpha = 0.9f),
                            radius = if (isTapped) 6f else 4f,
                            center = centerPt
                        )
                    }
                }
            }

            // Draw Site Markers
            sites.forEach { site ->
                val pt = latLngToPixel(
                    site.latitude, site.longitude,
                    centerLat, centerLng,
                    zoomLevel, width, height
                )

                val markerColor = when {
                    site.radarType.equals("BRIDGE", ignoreCase = true) || site.title.contains("Bridge", ignoreCase = true) || site.title.contains("Trestle", ignoreCase = true) || site.title.contains("Viaduct", ignoreCase = true) -> Color(0xFFF59E0B)
                    site.radarType.equals("UNDERGROUND", ignoreCase = true) -> Color(0xFF06B6D4)
                    site.radarType.equals("CROSSING", ignoreCase = true) -> Color(0xFF38BDF8)
                    site.category == LoreCategories.HAUNTED -> GhostCrimson
                    site.category == LoreCategories.UNDERGROUND_RR -> RiverMistBlue
                    site.category == LoreCategories.STEAM_AND_RAIL -> LanternAmberBright
                    site.category == LoreCategories.CURIOSITIES -> KnobstoneForest
                    else -> LanternAmber
                }

                val isSelected = selectedSite?.id == site.id

                // Glow ring
                if (isSelected) {
                    drawCircle(
                        color = markerColor.copy(alpha = 0.4f),
                        radius = 22f,
                        center = pt
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 16f,
                        center = pt,
                        style = Stroke(2.5f)
                    )
                }

                // Under your feet / karst subterranean outer halo
                if (site.radarType.equals("UNDERGROUND", ignoreCase = true)) {
                    drawCircle(
                        color = Color(0xFF06B6D4).copy(alpha = 0.5f),
                        radius = 14f,
                        center = pt,
                        style = Stroke(1.5f)
                    )
                }

                // Bridge outer span dashed halo
                if (site.radarType.equals("BRIDGE", ignoreCase = true) ||
                    site.title.contains("Bridge", ignoreCase = true) ||
                    site.title.contains("Trestle", ignoreCase = true) ||
                    site.title.contains("Viaduct", ignoreCase = true)) {
                    drawCircle(
                        color = Color(0xFFF59E0B).copy(alpha = 0.65f),
                        radius = 15f,
                        center = pt,
                        style = Stroke(2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f), 0f))
                    )
                }

                // Marker dot
                drawCircle(
                    color = markerColor,
                    radius = if (isSelected) 10f else 8f,
                    center = pt
                )
                drawCircle(
                    color = Color(0xFF0B0F19),
                    radius = 3.5f,
                    center = pt
                )
            }

            // Draw User Location Puck
            val userPt = latLngToPixel(
                userLat, userLng,
                centerLat, centerLng,
                zoomLevel, width, height
            )

            drawCircle(
                color = LanternAmber.copy(alpha = 0.25f),
                radius = 24f,
                center = userPt
            )
            drawCircle(
                color = LanternAmberBright,
                radius = 7f,
                center = userPt
            )
            drawCircle(
                color = Color.White,
                radius = 8f,
                center = userPt,
                style = Stroke(2f)
            )
        }

        // Top Map Overlay Bar & Historic Layer Controls
        Surface(
            color = SlateNightElevated.copy(alpha = 0.95f),
            shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "SOUTHERN INDIANA CARTOGRAPHY",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = LanternAmberBright
                        )
                        Text(
                            text = "${sites.size} Documented Lore Sites • Multi-County Overlays",
                            style = MaterialTheme.typography.bodySmall,
                            color = ParchmentPaper
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        // Historical Time Machine launcher button
                        Surface(
                            color = LanternAmber,
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, LanternAmberBright),
                            modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable {
                                showTimeMachineSheet = true
                            }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    Icons.Default.History,
                                    contentDescription = null,
                                    tint = Color(0xFF1E1100),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "TIME MACHINE ($timeMachineYear)",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFF1E1100)
                                )
                            }
                        }

                        // Toggle Layers Dropdown/Bar
                        Surface(
                            color = if (showLayersMenu) LanternAmber else Color(0x33F59E0B),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, LanternAmber),
                            modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable {
                                showLayersMenu = !showLayersMenu
                            }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    Icons.Default.Layers,
                                    contentDescription = null,
                                    tint = if (showLayersMenu) Color(0xFF1E1100) else LanternAmberBright,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (showLayersMenu) "CLOSE" else "LAYERS",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (showLayersMenu) Color(0xFF1E1100) else LanternAmberBright
                                )
                            }
                        }
                    }
                }

                // Interactive Historical Overlays Selector Bar
                if (showLayersMenu) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = show1937FloodLine,
                            onClick = { show1937FloodLine = !show1937FloodLine },
                            label = { Text("1937 Flood Inundation (Approx)", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF0284C7),
                                selectedLabelColor = Color.White
                            )
                        )

                        FilterChip(
                            selected = showBuffaloTrace,
                            onClick = { showBuffaloTrace = !showBuffaloTrace },
                            label = { Text("Buffalo Trace", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFB45309),
                                selectedLabelColor = Color.White
                            )
                        )

                        FilterChip(
                            selected = showTopoContours,
                            onClick = { showTopoContours = !showTopoContours },
                            label = { Text("Topo Contours", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = KnobstoneForest,
                                selectedLabelColor = Color.White
                            )
                        )
                    }

                    // Ghost Buildings & Lost Streets Filters for Street-Level Discovery
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = showGhostBuildings,
                            onClick = { showGhostBuildings = !showGhostBuildings },
                            label = { Text("🏛️ Ghost Footprints", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = LanternAmber,
                                selectedLabelColor = Color(0xFF1E1100)
                            )
                        )

                        FilterChip(
                            selected = showLostStreets,
                            onClick = { showLostStreets = !showLostStreets },
                            label = { Text("🛣️ Lost Streets", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = RiverMistBlue,
                                selectedLabelColor = Color.White
                            )
                        )
                    }

                    // Trail Selection Dropdown / Selector
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        RegionalHistoricTrails.trails.forEach { trail ->
                            val isSelected = selectedTrailId == trail.id
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedTrailId = if (isSelected) null else trail.id
                                },
                                label = { Text(trail.title.take(18) + "…", fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = LanternAmber,
                                    selectedLabelColor = Color(0xFF1E1100)
                                )
                            )
                        }
                    }
                }
            }
        }

        // Floating Map Controls (Zoom In, Zoom Out, Center on User)
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SmallFloatingActionButton(
                onClick = { zoomLevel = (zoomLevel + 0.8f).coerceAtMost(17f) },
                containerColor = SlateNightElevated,
                contentColor = ParchmentPaper,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Zoom in")
            }

            SmallFloatingActionButton(
                onClick = { zoomLevel = (zoomLevel - 0.8f).coerceAtLeast(11f) },
                containerColor = SlateNightElevated,
                contentColor = ParchmentPaper,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Zoom out")
            }

            FloatingActionButton(
                onClick = {
                    centerLat = userLat
                    centerLng = userLng
                    zoomLevel = 14.5f
                },
                containerColor = LanternAmber,
                contentColor = Color(0xFF1E1100),
                shape = CircleShape
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = "Center on my location")
            }
        }

        // Time Machine Bottom Sheet Modal
        if (showTimeMachineSheet) {
            TimeMachineSheet(
                userLat = userLat,
                userLng = userLng,
                sites = sites,
                selectedYear = timeMachineYear,
                onYearSelected = { timeMachineYear = it },
                opacity = timeMachineOpacity,
                onOpacityChanged = { timeMachineOpacity = it },
                onDismiss = { showTimeMachineSheet = false },
                onSiteSelected = onSiteSelected
            )
        }

        // Street-Level Historical Inquiry Dialog ("What Stood Right Here?")
        if (selectedFootprint != null) {
            WhatStoodHereDialog(
                footprint = selectedFootprint!!,
                changeEvent = activeFootprintChangeEvent,
                onDismiss = {
                    selectedFootprint = null
                    activeFootprintChangeEvent = null
                }
            )
        }
    }
}

// Slippy Map coordinate helper functions
private fun lonToTileX(lon: Double, zoom: Int): Double {
    return (lon + 180.0) / 360.0 * 2.0.pow(zoom.toDouble())
}

private fun latToTileY(lat: Double, zoom: Int): Double {
    val rad = Math.toRadians(lat)
    return (1.0 - asinh(tan(rad)) / PI) / 2.0 * 2.0.pow(zoom.toDouble())
}

private fun latLngToPixel(
    lat: Double, lon: Double,
    centerLat: Double, centerLon: Double,
    zoom: Float,
    viewWidth: Float, viewHeight: Float
): Offset {
    val z = zoom.toDouble()
    val n = 2.0.pow(z)

    val centerTileX = (centerLon + 180.0) / 360.0 * n
    val centerRad = Math.toRadians(centerLat)
    val centerTileY = (1.0 - asinh(tan(centerRad)) / PI) / 2.0 * n

    val targetTileX = (lon + 180.0) / 360.0 * n
    val targetRad = Math.toRadians(lat)
    val targetTileY = (1.0 - asinh(tan(targetRad)) / PI) / 2.0 * n

    val pixelX = ((targetTileX - centerTileX) * 256.0 + viewWidth / 2.0).toFloat()
    val pixelY = ((targetTileY - centerTileY) * 256.0 + viewHeight / 2.0).toFloat()

    return Offset(pixelX, pixelY)
}
