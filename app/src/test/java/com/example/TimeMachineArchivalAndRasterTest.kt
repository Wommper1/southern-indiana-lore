package com.example

import com.example.data.model.GeoreferenceStatus
import com.example.data.model.MapAlignmentConfidence
import com.example.data.model.SouthernIndianaMapArchive
import com.example.util.OfflineBasemapRenderer
import com.example.util.UsgsHistoricalTopoRasterGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class TimeMachineArchivalAndRasterTest {

    @Test
    fun `verify 1886 Sanborn Library of Congress metadata audit`() {
        val layer1886 = SouthernIndianaMapArchive.layers.first { it.yearStart == 1886 }
        assertEquals("1886", layer1886.displayYear)
        assertEquals(16, layer1886.sheetCount)
        assertEquals("g4094nm.g4094nm_g024351886", layer1886.locDigitalId)
        assertTrue(layer1886.sourceIdentifier.contains("sanborn02435_001"))
        assertEquals("https://loc.gov/item/sanborn02435_001/manifest.json", layer1886.iiifManifestUrl)
        assertEquals(GeoreferenceStatus.VERIFIED_GEOREFERENCED, layer1886.georeferenceStatus)
    }

    @Test
    fun `verify 1905 Sanborn Library of Congress metadata audit`() {
        val layer1905 = SouthernIndianaMapArchive.layers.first { it.yearStart == 1905 }
        assertEquals("1905", layer1905.displayYear)
        assertEquals(69, layer1905.sheetCount)
        assertEquals("g4094nm.g4094nm_g024351905", layer1905.locDigitalId)
        assertTrue(layer1905.sourceIdentifier.contains("sanborn02435_004"))
        assertEquals("https://loc.gov/item/sanborn02435_004/manifest.json", layer1905.iiifManifestUrl)
        assertEquals(GeoreferenceStatus.VERIFIED_GEOREFERENCED, layer1905.georeferenceStatus)
    }

    @Test
    fun `verify Sanborn multi-edition lineage chain`() {
        val lineage = SouthernIndianaMapArchive.newAlbanySanbornLineage
        assertEquals(6, lineage.size)

        val ed1886 = lineage.first { it.year == 1886 }
        assertEquals(16, ed1886.sheetCount)
        assertEquals("sanborn02435_001", ed1886.locItemId)

        val ed1891 = lineage.first { it.year == 1891 }
        assertEquals(32, ed1891.sheetCount)
        assertEquals("sanborn02435_002", ed1891.locItemId)

        val ed1898 = lineage.first { it.year == 1898 }
        assertEquals(39, ed1898.sheetCount)
        assertEquals("sanborn02435_003", ed1898.locItemId)

        val ed1905 = lineage.first { it.year == 1905 }
        assertEquals(69, ed1905.sheetCount)
        assertEquals("sanborn02435_004", ed1905.locItemId)
    }

    @Test
    fun `verify 1955 USGS Quadrangle raster tile generation offline`() {
        // Mathematically computed slippy tile coords for zoom 13 covering downtown New Albany (z=13, x=2143, y=3151)
        val z = 13
        val x = 2143
        val y = 3151

        val intersects = UsgsHistoricalTopoRasterGenerator.intersectsQuad(z, x, y)
        assertTrue("Tile 13/2143/3151 must intersect 1955 USGS New Albany Quad", intersects)

        val rasterTile = UsgsHistoricalTopoRasterGenerator.render1955TopoTile(z, x, y)
        assertNotNull("Raster tile must render offline without network", rasterTile)
        assertEquals(256, rasterTile?.width)
        assertEquals(256, rasterTile?.height)
    }

    @Test
    fun `verify airplane mode offline basemap generator`() {
        // Render offline basemap tile
        val tile = OfflineBasemapRenderer.renderOfflineTile(13, 2143, 3151)
        assertNotNull(tile)
        assertEquals(256, tile.width)
        assertEquals(256, tile.height)
    }

    @Test
    fun `verify 1937 flood layer is marked as approximate geometry`() {
        val floodLayer = SouthernIndianaMapArchive.layers.first { it.yearStart == 1937 }
        assertEquals(GeoreferenceStatus.APPROXIMATE_GEOMETRY, floodLayer.georeferenceStatus)
        assertEquals(MapAlignmentConfidence.APPROXIMATE, floodLayer.alignmentConfidence)
    }

    @Test
    fun `verify Sanborn micro-area repository audited GCP control and RMS error`() {
        val sheets = com.example.data.model.SanbornMicroAreaRepository.sheets
        assertEquals(4, sheets.size)

        val sheet3_1886 = sheets.first { it.editionYear == 1886 && it.sheetNumber == 3 }
        assertEquals("sanborn02435_001", sheet3_1886.archiveIdentifier)
        assertEquals(5, sheet3_1886.gcps.size)
        assertTrue("RMS error must be less than 5m", sheet3_1886.rmsErrorMeters <= 5.0)

        val sheet6_1905 = sheets.first { it.editionYear == 1905 && it.sheetNumber == 6 }
        assertEquals("sanborn02435_004", sheet6_1905.archiveIdentifier)
        assertEquals(6, sheet6_1905.gcps.size)
        assertTrue("RMS error must be less than 4m", sheet6_1905.rmsErrorMeters <= 4.0)
    }

    @Test
    fun `verify street level footprints and change events linkage`() {
        val footprints = com.example.data.model.SanbornMicroAreaRepository.historicalFootprints
        assertTrue("Must contain historical building footprints", footprints.isNotEmpty())

        val scriberHouse = footprints.first { it.buildingName.contains("Scribner") }
        assertEquals("FRAME_WOOD", scriberHouse.constructionMaterial)
        assertEquals("DWELLING", scriberHouse.occupancyType)

        // Find change event near Main & Bank
        val changeEvent = com.example.data.model.SanbornMicroAreaRepository.findChangeEventNear(38.2851, -85.8239)
        assertNotNull("Must find change event at Main & Bank", changeEvent)
        assertEquals("DEMOLISHED_FOR_FLOODWALL", changeEvent?.eventType?.name)
    }

    @Test
    fun `verify Sanborn raster tile generator offline render`() {
        // Tile 14/4286/6302 covers downtown New Albany civic core
        val tile1886 = com.example.util.SanbornRasterTileGenerator.renderSanbornTile(1886, 14, 4286, 6302)
        assertNotNull("1886 Sanborn tile must render offline", tile1886)
        assertEquals(256, tile1886?.width)
        assertEquals(256, tile1886?.height)

        val tile1905 = com.example.util.SanbornRasterTileGenerator.renderSanbornTile(1905, 14, 4286, 6302)
        assertNotNull("1905 Sanborn tile must render offline", tile1905)
        assertEquals(256, tile1905?.width)
        assertEquals(256, tile1905?.height)
    }
}
