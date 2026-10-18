package com.example

import com.example.data.model.ConfidenceLevel
import com.example.data.model.LoreSite
import com.example.service.CrossingState
import com.example.service.GpsQualityFilter
import com.example.service.LocalProximityEngine
import com.example.util.LocationUtils
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Hardened verification suite covering:
 * - Movement-aware trajectory crossing at highway speeds (Simulation A)
 * - Parallel route rejection without crossing (Simulation B)
 * - Turn-around trajectory handling (Simulation C)
 * - Teleport rejection via GpsQualityFilter (Simulation D)
 * - Stationary GPS jitter filtering (Simulation E)
 * - Polygon entry / exit containment (Simulation F)
 * - State machine cooldown and anti-spam transitions (Simulation G)
 */
class ProximityEngineHardeningTest {

    private lateinit var proximityEngine: LocalProximityEngine

    private val testLineSite = LoreSite(
        id = "test_monon_corridor",
        title = "Monon Railroad Historic Cut",
        subtitle = "Pre-1900 Steam Alignment",
        category = "STEAM_AND_RAIL",
        county = "Floyd",
        era = "1880s",
        accessStatus = "Public Right-of-Way",
        fullStory = "Historic Monon railroad cut through the Knobs.",
        sourceNotes = "Floyd County Historical Society records.",
        verificationGrade = "A",
        audioNarration = "Approaching the historic Monon railroad crossing.",
        latitude = 38.3000,
        longitude = -85.8300,
        address = "Floyds Knobs, IN",
        siteType = "LINE",
        // North-South line segment at longitude -85.8300 between lat 38.2900 and 38.3100
        geometryCoordinates = "38.2900,-85.8300;38.3100,-85.8300",
        youJustCrossedEvent = "Crossed the 1882 Monon Railroad Cut",
        geofenceRadiusMeters = 75,
        confidenceLevel = ConfidenceLevel.VERIFIED
    )

    private val testPolygonSite = LoreSite(
        id = "test_rose_island_district",
        title = "Rose Island Resort Enclave",
        subtitle = "Amusement park ruins",
        category = "CURIOSITIES",
        county = "Clark",
        era = "1920s",
        accessStatus = "State Park Access",
        fullStory = "1920s summer resort ruined by the 1937 flood.",
        sourceNotes = "Clark County archival records.",
        verificationGrade = "A",
        audioNarration = "Entering Rose Island historic grounds.",
        latitude = 38.4239,
        longitude = -85.6268,
        address = "Charlestown, IN",
        siteType = "POLYGON",
        // Bounding box around ~38.4200 to 38.4300, -85.6300 to -85.6200
        geometryCoordinates = "38.4200,-85.6300;38.4300,-85.6300;38.4300,-85.6200;38.4200,-85.6200",
        youJustCrossedEvent = "Entered Rose Island Historic District",
        geofenceRadiusMeters = 200,
        confidenceLevel = ConfidenceLevel.VERIFIED
    )

    @Before
    fun setUp() {
        proximityEngine = LocalProximityEngine()
    }

    /**
     * SIMULATION A: Highway Crossing
     * Driving at ~65 mph (~29 m/s) West to East, completely crossing the Monon corridor between fixes.
     * Fix 1 is west of the line: (38.3000, -85.8400)
     * Fix 2 is east of the line: (38.3000, -85.8200)
     * Even though neither fix is within 75m of the line, trajectory segment intersection MUST detect the crossing!
     */
    @Test
    fun simulationA_highwayCrossing_segmentIntersectionFires() {
        val prevLat = 38.3000
        val prevLng = -85.8400 // ~870m West

        val currLat = 38.3000
        val currLng = -85.8200 // ~870m East

        // Distance between fixes is ~1740 meters
        val distBetween = LocationUtils.calculateDistanceMeters(prevLat, prevLng, currLat, currLng)
        assertTrue("Distance should be highway distance", distBetween > 1500)

        // Trajectory test directly
        val crossed = proximityEngine.checkTrajectoryIntersection(
            prevLat = prevLat,
            prevLng = prevLng,
            currLat = currLat,
            currLng = currLng,
            site = testLineSite
        )
        assertTrue("Highway trajectory across the corridor MUST register as crossed", crossed)

        // Evaluate state transitions
        val state = proximityEngine.evaluateCrossingState(
            site = testLineSite,
            currLat = currLat,
            currLng = currLng,
            prevLat = prevLat,
            prevLng = prevLng,
            currentState = CrossingState.APPROACHING
        )
        assertEquals("Crossing state machine must transition to CROSSED", CrossingState.CROSSED, state)
    }

    /**
     * SIMULATION B: Parallel Road
     * Vehicle travels North-South parallel to the Monon corridor at longitude -85.8350 (approx 435m away).
     * The trajectory never intersects the corridor line at -85.8300.
     */
    @Test
    fun simulationB_parallelRoad_noCrossingAlert() {
        val prevLat = 38.2900
        val prevLng = -85.8350

        val currLat = 38.3100
        val currLng = -85.8350

        val crossed = proximityEngine.checkTrajectoryIntersection(
            prevLat = prevLat,
            prevLng = prevLng,
            currLat = currLat,
            currLng = currLng,
            site = testLineSite
        )
        assertFalse("Parallel travel without crossing must NOT trigger corridor intersection", crossed)

        val state = proximityEngine.evaluateCrossingState(
            site = testLineSite,
            currLat = currLat,
            currLng = currLng,
            prevLat = prevLat,
            prevLng = prevLng,
            currentState = CrossingState.FAR
        )
        assertNotEquals("State should not be CROSSED or INSIDE", CrossingState.CROSSED, state)
        assertNotEquals("State should not be INSIDE", CrossingState.INSIDE, state)
    }

    /**
     * SIMULATION C: Turn-Around Trajectory
     * Driver approaches corridor from West (-85.8350) towards -85.8310, does a U-turn and retreats to -85.8360.
     * Never intersects line at -85.8300.
     */
    @Test
    fun simulationC_turnAround_noFalseCrossing() {
        val approachCrossed = proximityEngine.checkTrajectoryIntersection(
            prevLat = 38.3000,
            prevLng = -85.8350,
            currLat = 38.3000,
            currLng = -85.8310,
            site = testLineSite
        )
        assertFalse("Approach before line does not cross", approachCrossed)

        val retreatCrossed = proximityEngine.checkTrajectoryIntersection(
            prevLat = 38.3000,
            prevLng = -85.8310,
            currLat = 38.3000,
            currLng = -85.8360,
            site = testLineSite
        )
        assertFalse("U-turn retreat does not cross", retreatCrossed)
    }

    /**
     * SIMULATION D: GPS Teleport Rejection
     * GpsQualityFilter must reject sudden 50km jumps with impossible velocities (>65 m/s or 145 mph).
     */
    @Test
    fun simulationD_gpsTeleport_rejectedByFilter() {
        val filter = GpsQualityFilter(maxAcceptableAccuracyMeters = 40.0, maxAcceptableSpeedMps = 55.0)

        // Initial valid fix
        val fix1Accepted = filter.isFixReliable(
            lat = 38.2856,
            lng = -85.8241,
            accuracyMeters = 8.0f,
            timeElapsedSec = 1.0
        )
        assertTrue("Normal initial fix must be accepted", fix1Accepted)

        // Teleport 50km away within 1 second (~50,000 m/s)
        val teleportAccepted = filter.isFixReliable(
            lat = 38.7000,
            lng = -85.8241,
            accuracyMeters = 10.0f,
            timeElapsedSec = 1.0
        )
        assertFalse("Teleport fix at 50,000 m/s MUST be rejected", teleportAccepted)

        // Poor accuracy fix (> 40m)
        val poorAccuracyAccepted = filter.isFixReliable(
            lat = 38.2860,
            lng = -85.8240,
            accuracyMeters = 85.0f,
            timeElapsedSec = 2.0
        )
        assertFalse("Fix with 85m accuracy MUST be rejected", poorAccuracyAccepted)
    }

    /**
     * SIMULATION E: Stationary Jitter
     * Stationary user at (38.2856, -85.8241) experiencing 2-3 meter GPS drift within 1 second.
     * Velocity should be very small (< 1 m/s) and not trigger crossing states.
     */
    @Test
    fun simulationE_stationaryJitter_smoothHandling() {
        val filter = GpsQualityFilter()
        filter.isFixReliable(38.285600, -85.824100, 5.0f, 1.0)

        // 3 meter jitter
        val jitterAccepted = filter.isFixReliable(38.285625, -85.824100, 6.0f, 1.0)
        assertTrue("Minor jitter is within normal bounds", jitterAccepted)

        // State machine stays in FAR or NEAR without spurious CROSSED alerts
        val state = proximityEngine.evaluateCrossingState(
            site = testLineSite,
            currLat = 38.285625,
            currLng = -85.824100,
            prevLat = 38.285600,
            prevLng = -85.824100,
            currentState = CrossingState.FAR
        )
        assertEquals("Stationary jitter far from line remains FAR", CrossingState.FAR, state)
    }

    /**
     * SIMULATION F: Polygon Entry / Inside / Exit
     * User travels from outside Rose Island polygon, into the interior, and out.
     */
    @Test
    fun simulationF_polygonEntryExit_statesCorrect() {
        val outsideLat = 38.4100
        val outsideLng = -85.6250

        val insideLat = 38.4250
        val insideLng = -85.6250

        // Point containment test
        val isInside = LocationUtils.isPointInPolygon(
            insideLat, insideLng,
            LocationUtils.parseCoordinates(testPolygonSite.geometryCoordinates)
        )
        assertTrue("Center point must be inside polygon", isInside)

        // Transition from FAR to INSIDE
        val stateEntered = proximityEngine.evaluateCrossingState(
            site = testPolygonSite,
            currLat = insideLat,
            currLng = insideLng,
            prevLat = outsideLat,
            prevLng = outsideLng,
            currentState = CrossingState.FAR
        )
        assertTrue("Transitioning into polygon results in INSIDE or CROSSED",
            stateEntered == CrossingState.INSIDE || stateEntered == CrossingState.CROSSED)

        // Transitioning from INSIDE to OUTSIDE
        val stateLeft = proximityEngine.evaluateCrossingState(
            site = testPolygonSite,
            currLat = outsideLat,
            currLng = outsideLng,
            prevLat = insideLat,
            prevLng = insideLng,
            currentState = CrossingState.INSIDE
        )
        assertEquals("Leaving polygon transitions to LEAVING or COOLDOWN", CrossingState.LEAVING, stateLeft)
    }

    /**
     * SIMULATION G: State Machine Cooldown (Anti-Spam)
     * Once CROSSED, staying near the corridor should transition to COOLDOWN or NEAR,
     * preventing continuous alert notifications.
     */
    @Test
    fun simulationG_stateMachineCooldown_preventsSpam() {
        // User just crossed
        val crossedState = CrossingState.CROSSED

        // Next fix is 30 meters past line
        val nextState = proximityEngine.evaluateCrossingState(
            site = testLineSite,
            currLat = 38.3000,
            currLng = -85.8295,
            prevLat = 38.3000,
            prevLng = -85.8305,
            currentState = crossedState
        )
        assertEquals("Subsequent fix immediately following CROSSED enters COOLDOWN", CrossingState.COOLDOWN, nextState)

        // Continued movement while in COOLDOWN should stay in COOLDOWN or transition to FAR
        val stayingState = proximityEngine.evaluateCrossingState(
            site = testLineSite,
            currLat = 38.3000,
            currLng = -85.8290,
            prevLat = 38.3000,
            prevLng = -85.8295,
            currentState = CrossingState.COOLDOWN
        )
        assertEquals("Staying near after crossing remains in COOLDOWN", CrossingState.COOLDOWN, stayingState)
    }
}
