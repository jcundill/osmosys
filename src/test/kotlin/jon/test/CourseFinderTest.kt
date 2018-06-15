package jon.test


import com.graphhopper.util.shapes.GHPoint
import io.mockk.classMockk
import io.mockk.every
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import xyz.thepathfinder.simulatedannealing.InfeasibleProblemException
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

        every {csf.randomBearing} returns 0.05
        every {csf.getCoords(any(), any(), any())} returns dummyPoint
        every {csf.findControlSiteNear(any(), any())} returns dummyPoint

        val params = CourseParameters(numControls = 10, start = GHPoint(52.988304, -1.203265))
        val finder = CourseFinder(csf, emptyList(), scorer, params)
        val points = finder.chooseInitialPoints(start = GHPoint(52.988304, -1.203265), finish = GHPoint(52.988304, -1.203265))
        assertEquals(12, points.size)
        assertEquals( listOf(params.start) + List(params.numControls, {dummyPoint}) + params.finish, points)
    }

    @Test
    fun startTooFarFromFinish() {
        val dummyPoint = GHPoint(12.0, 12.0)

        every {csf.findControlSiteNear(any(), any())} returns dummyPoint
        val params = CourseParameters(distance = 6000.0, numControls = 8,
                start = GHPoint(53.223482, -1.461064),
                finish = GHPoint(51.511287, -0.113695))
        val finder = CourseFinder(csf, emptyList(), scorer, params)
        assertFailsWith( InfeasibleProblemException::class, "start is too far away from finish to be mapped",
                {finder.chooseInitialPoints(start = params.start, finish = params.finish)})
    }
}