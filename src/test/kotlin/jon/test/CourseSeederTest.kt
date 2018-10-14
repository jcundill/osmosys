package jon.test


import com.graphhopper.util.shapes.GHPoint
import io.mockk.classMockk
import io.mockk.every
import jon.test.annealing.InfeasibleProblemException
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class CourseSeederTest {

    private lateinit var csf: ControlSiteFinder
    private lateinit var seeder: CourseSeeder


    @BeforeAll
    fun beforeTests() {
        csf = classMockk(ControlSiteFinder::class)
        seeder = CourseSeeder(csf)
    }

    @Test
    fun chooseInitialPoints() {
        val dummyPoint = GHPoint(12.0, 12.0)
        val start = GHPoint(52.988304, -1.203265)

        every { csf.randomBearing } returns 0.05
        every { csf.getCoords(any(), any(), any()) } returns dummyPoint
        every { csf.findControlSiteNear(any(), any()) } returns dummyPoint
        every { csf.findNearestControlSiteTo(start) } returns start

        val points = seeder.chooseInitialPoints(listOf(start, start), 10, 7000.0)
        assertEquals(12, points.size)
        assertEquals(listOf(start) + List(10) { dummyPoint } + start, points)
    }

    @Test
    fun startTooFarFromFinish() {
        val dummyPoint = GHPoint(12.0, 12.0)
        val start = GHPoint(53.223482, -1.461064)
        val finish = GHPoint(51.511287, -0.113695)

        every { csf.randomBearing } returns 0.05
        every { csf.findNearestControlSiteTo(start) } returns start
        every { csf.findNearestControlSiteTo(finish) } returns finish
        every { csf.findControlSiteNear(any(), any()) } returns dummyPoint
        every { csf.getCoords(any(), any(), any()) } returns dummyPoint

        assertFailsWith(InfeasibleProblemException::class, "initial course cannot be mapped") {
            seeder.chooseInitialPoints(listOf(start, finish), 10, 7000.0)
        }
    }

    @Test
    fun chooseInitialPointsWithWayPoint() {
        val dummyPoint = GHPoint(12.0, 12.0)
        val start = GHPoint(52.988304, -1.203265)
        val wpt1 = GHPoint(52.988704, -1.203265)

        every { csf.randomBearing } returns 0.05
        every { csf.getCoords(any(), any(), any()) } returns dummyPoint
        every { csf.findControlSiteNear(any(), any()) } returns dummyPoint
        every { csf.findNearestControlSiteTo(start) } returns start
        every { csf.findNearestControlSiteTo(wpt1) } returns wpt1

        val points = seeder.chooseInitialPoints(listOf(start, wpt1, start), 10, 7000.0)
        assertEquals(12, points.size)
        assertEquals(listOf(start) + List(9) { dummyPoint } + wpt1 + start, points)
    }

    @Test
    fun chooseInitialPointsWithWayPointTooFarAway() {
        val dummyPoint = GHPoint(12.0, 12.0)
        val start = GHPoint(52.988304, -1.203265)
        val wpt1 = GHPoint(51.511287, -0.113695)

        every { csf.randomBearing } returns 0.05
        every { csf.getCoords(any(), any(), any()) } returns dummyPoint
        every { csf.findControlSiteNear(any(), any()) } returns dummyPoint
        every { csf.findNearestControlSiteTo(start) } returns start
        every { csf.findNearestControlSiteTo(wpt1) } returns wpt1

        assertFailsWith(InfeasibleProblemException::class, "initial course cannot be mapped") {
            seeder.chooseInitialPoints(listOf(start, wpt1, start), 10, 7000.0)
        }
    }

}