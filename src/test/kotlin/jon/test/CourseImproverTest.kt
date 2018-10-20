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
    val course = Course(controls = controls)


    @BeforeAll
    fun beforeTests() {
        csf = classMockk(ControlSiteFinder::class)
        mockRoutingResponse = classMockk(GHResponse::class)

        every { csf.routeRequest(any(), any()) } returns mockRoutingResponse
        every { csf.findAlternativeControlSiteFor(any()) } returns replacedPoint

    }

    @BeforeEach
    fun setUp() {
        improver = CourseImprover(csf, course)
    }

    @Test
    fun hashCodeWorksSame() {
        val improver2 = CourseImprover(csf, course)
        assertEquals(improver.hashCode(), improver2.hashCode())
    }

    @Test
    fun hashCodeWorksDifferent() {
        val course2 = Course(controls = listOf(GHPoint(1.0, 2.0), GHPoint(2.5, 2.5), GHPoint(1.5, 2.5), GHPoint(1.5, 2.5)))
        val improver2 = CourseImprover(csf, course2)
        assertNotEquals(improver.hashCode(), improver2.hashCode())
    }

    @Test
    fun equalsWorksSame() {
        val improver2 = CourseImprover(csf, course)
        assertEquals(improver, improver2)
    }

    @Test
    fun equalsWorksDifferent() {
        val course2 = Course(controls = listOf(GHPoint(1.0, 2.0), GHPoint(2.5, 2.5), GHPoint(1.5, 2.5), GHPoint(1.5, 2.5)))
        val improver2 = CourseImprover(csf, course2)
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
        val idxes = improver.findIndexesOfWorst(scores, 100)
        assertEquals(5, idxes.size )
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
        assertEquals(5,idxes[0])
    }

    @Test
    fun maxWorstsDoesNotIncludeTheStart() {
        val scores = listOf(0.2, 0.2, 0.2, 0.2)
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
    fun allTheSameIncludesTheLastLeg() {
        val scores = listOf(0.2, 0.2, 0.2, 0.2, 0.9)
        assertFalse(improver.allTheSameScore(scores))
    }

    @Test
    fun replaceSelected() {
        val improved = improver.replaceSelectedNumberedControls(listOf(1, 2), controls)

        assertNotEquals(improved[0], replacedPoint)
        assertEquals(improved[1], replacedPoint)
        assertEquals(improved[2], replacedPoint)
        assertNotEquals(improved[3], replacedPoint)

    }

    @Test
    fun replaceSelectedNone() {
        val improved = improver.replaceSelectedNumberedControls(emptyList(), controls)

        assertNotEquals(improved[0], replacedPoint)
        assertNotEquals(improved[1], replacedPoint)
        assertNotEquals(improved[2], replacedPoint)
        assertNotEquals(improved[3], replacedPoint)

    }

    // ye it will - because we can't pass it the start or the finisg
//    @Test
//    fun replaceSelectedWillNotMoveTheStart() {
//        val improved = improver.replaceSelectedNumberedControls(listOf(0, 1), controls)
//
//        assertNotEquals(improved[0], replacedPoint)
//        assertEquals(improved[1], replacedPoint)
//        assertNotEquals(improved[2], replacedPoint)
//        assertNotEquals(improved[3], replacedPoint)
//
//    }
//
//    @Test
//    fun replaceSelectedWillNotMoveTheFinish() {
//        val improved = improver.replaceSelectedNumberedControls(listOf(1, 2, 3), controls)
//
//        assertNotEquals(improved[0], replacedPoint)
//        assertEquals(improved[1], replacedPoint)
//        assertEquals(improved[2], replacedPoint)
//        assertNotEquals(improved[3], replacedPoint)
//
//    }
}