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
internal class CourseFinderTest {

    lateinit var csf: ControlSiteFinder
    lateinit var scorer: CourseScorer

    @BeforeAll
    fun beforeTests() {
        csf = classMockk(ControlSiteFinder::class)
        scorer = classMockk(CourseScorer::class)
    }

    @Test
    fun chooseInitialPoints() {
        val dummyPoint = GHPoint(12.0, 12.0)
        val start = GHPoint(52.988304, -1.203265)

        every {csf.randomBearing} returns 0.05
        every {csf.getCoords(any(), any(), any())} returns dummyPoint
        every {csf.findControlSiteNear(any(), any())} returns dummyPoint
        every {csf.findNearestControlSiteTo(start)} returns start

        val params = CourseParameters(numControls = 10, start = start)
        val finder = CourseFinder(csf, emptyList(), scorer, params)
        val points = finder.chooseInitialPoints(start = start, finish = start, initialPoints = emptyList())
        assertEquals(12, points.size)
        assertEquals( listOf(params.start) + List(params.numControls, {dummyPoint}) + params.finish, points)
    }

    @Test
    fun startTooFarFromFinish() {
        val dummyPoint = GHPoint(12.0, 12.0)
        val start = GHPoint(53.223482, -1.461064)
        val finish = GHPoint(51.511287, -0.113695)

        every {csf.findControlSiteNear(any(), any())} returns dummyPoint
        every {csf.findNearestControlSiteTo(start)} returns start
        every {csf.findNearestControlSiteTo(finish)} returns finish
        val params = CourseParameters(distance = 6000.0, numControls = 8,
                start = start,
                finish = finish)
        val finder = CourseFinder(csf, emptyList(), scorer, params)
        assertFailsWith( InfeasibleProblemException::class, "start is too far away from finish to be mapped"
        ) {finder.chooseInitialPoints(start = params.start, finish = params.finish, initialPoints = emptyList())}
    }
}