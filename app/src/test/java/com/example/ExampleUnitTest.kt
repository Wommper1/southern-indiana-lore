package com.example

import com.example.util.LocationUtils
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun parseCoordinates_parsesSemicolonSeparatedPairs() {
    val input = "38.2856,-85.8241;38.3000,-85.8500"
    val points = LocationUtils.parseCoordinates(input)
    assertEquals(2, points.size)
    assertEquals(38.2856, points[0].lat, 0.0001)
    assertEquals(-85.8241, points[0].lng, 0.0001)
    assertEquals(38.3000, points[1].lat, 0.0001)
    assertEquals(-85.8500, points[1].lng, 0.0001)
  }

  @Test
  fun calculateDistanceToPolyline_nearLineReturnsLowDistance() {
    val points = LocationUtils.parseCoordinates("38.0000,-86.0000;38.1000,-86.0000")
    // User is right on the line at (38.0500, -86.0000)
    val dist = LocationUtils.calculateDistanceToPolylineMeters(38.0500, -86.0000, points)
    assertTrue("Distance should be within 5 meters of line", dist < 5f)
  }
}
