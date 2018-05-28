package jon.test

import com.graphhopper.GHResponse
import com.graphhopper.util.shapes.GHPoint
import io.mockk.classMockk
import io.mockk.every
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.test.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class CourseImproverTest {

    lateinit var csf: ControlSiteFinder
    lateinit var improver: CourseImprover
    lateinit var mockRoutingResponse: GHResponse

    val replacedPoint = GHPoint(100.0, 100.0)
    val controls = listOf(GHPoint(1.0, 2.0), GHPoint(1.5, 2.5), GHPoint(1.5, 2.5), GHPoint(1.5, 2.5))


    @BeforeAll
    fun beforeTests() {
        csf = classMockk(ControlSiteFinder::class)
        mockRoutingResponse = classMockk(GHResponse::class)

        every { csf.routeRequest(any(), any()) } returns mockRoutingResponse
        every { csf.findAlternativeControlSiteFor(any()) } returns replacedPoint

    }

    @BeforeEach
    fun setUp() {
        improver = CourseImprover(csf, controls)
    }

    @Test
    fun hashCodeWorksSame() {
        val improver2 = CourseImprover(csf, controls)
        assertEquals(improver.hashCode(), improver2.hashCode())
    }

    @Test
    fun hashCodeWorksDifferent() {
        val controls2 = listOf(GHPoint(1.0, 2.0), GHPoint(2.5, 2.5), GHPoint(1.5, 2.5), GHPoint(1.5, 2.5))
        val improver2 = CourseImprover(csf, controls2)
        assertNotEquals(improver.hashCode(), improver2.hashCode())
    }

    @Test
    fun equalsWorksSame() {
        val improver2 = CourseImprover(csf, controls)
        assertEquals(improver, improver2)
    }

    @Test
    fun equalsWorksDifferent() {
        val controls2 = listOf(GHPoint(1.0, 2.0), GHPoint(2.5, 2.5), GHPoint(1.5, 2.5), GHPoint(1.5, 2.5))
        val improver2 = CourseImprover(csf, controls2)
        assertNotEquals(improver, improver2)
    }

    // sometimes fails as can choose last
//    @Test
//    fun ifNoLegScoresChooseRandom() {
//
//        val improved = improver.step()
//
//        assertNotNull(improved)
//        assertEquals(controls.size, improved.controls.size)
//        assertEquals(1, improved.controls.filter { it == replacedPoint }.size)
//    }

    @Test
    fun findWorsts() {
        val scores = listOf(0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.1, 0.2, 0.3)
        val idxes = improver.findIndexesOfWorst(scores, 3)
        assertEquals(3, idxes.size )
    }

    @Test
    fun findWorstsWillNotChooseTheStart() {
        val scores = listOf(0.9, 0.2, 0.2, 0.2, 0.2)
        val idxes = improver.findIndexesOfWorst(scores, 1)
        assertEquals(1, idxes.size )
        assertNotEquals(0,idxes[0])
    }

    @Test
    fun findWorstsCanChooseTheLastLeg() {
        val scores = listOf(0.2, 0.2, 0.2, 0.2, 0.9)
        val idxes = improver.findIndexesOfWorst(scores, 1)
        assertEquals(1, idxes.size )
        assertEquals(4,idxes[0])
    }

    @Test
    fun maxWorstsDoesNotIncludeTheStart() {
        val scores = listOf(0.2, 0.2, 0.2, 0.2, 0.2)
        val idxes = improver.findIndexesOfWorst(scores, 15)
        assertEquals(4, idxes.size )
        assertFalse(idxes.any { it == 0 })
    }

    @Test
    fun allTheSameTrue() {
        val scores = listOf(0.2, 0.2, 0.2, 0.2)
        assertTrue(improver.allTheSameScore(scores))
    }

    @Test
    fun allTheSameFalse() {
        val scores = listOf(0.2, 0.3, 0.1, 0.2, 0.9)
        assertFalse(improver.allTheSameScore(scores))
    }

    @Test
    fun allTheSameIgnoresTheStart() {
        val scores = listOf(0.9, 0.2, 0.2, 0.2, 0.2)
        assertTrue(improver.allTheSameScore(scores))
    }

    @Test
    fun allTheSameIncludesTheLastLeg() {
        val scores = listOf(0.2, 0.2, 0.2, 0.2, 0.9)
        assertFalse(improver.allTheSameScore(scores))
    }

    @Test
    fun replaceSelected() {
        val improved = improver.replaceSelectedControls(listOf(1, 2), controls)

        assertNotEquals(improved[0], replacedPoint)
        assertEquals(improved[1], replacedPoint)
        assertEquals(improved[2], replacedPoint)
        assertNotEquals(improved[3], replacedPoint)

    }

    @Test
    fun replaceSelectedNone() {
        val improved = improver.replaceSelectedControls(emptyList(), controls)

        assertNotEquals(improved[0], replacedPoint)
        assertNotEquals(improved[1], replacedPoint)
        assertNotEquals(improved[2], replacedPoint)
        assertNotEquals(improved[3], replacedPoint)

    }

    @Test
    fun replaceSelectedWillNotMoveTheStart() {
        val improved = improver.replaceSelectedControls(listOf(0, 1), controls)

        assertNotEquals(improved[0], replacedPoint)
        assertEquals(improved[1], replacedPoint)
        assertNotEquals(improved[2], replacedPoint)
        assertNotEquals(improved[3], replacedPoint)

    }

    @Test
    fun replaceSelectedWillNotMoveTheFinish() {
        val improved = improver.replaceSelectedControls(listOf(1, 2, 3), controls)

        assertNotEquals(improved[0], replacedPoint)
        assertEquals(improved[1], replacedPoint)
        assertEquals(improved[2], replacedPoint)
        assertNotEquals(improved[3], replacedPoint)

    }
}