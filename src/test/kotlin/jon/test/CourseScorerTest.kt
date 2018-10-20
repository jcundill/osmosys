package jon.test

import com.graphhopper.GHResponse
import com.graphhopper.util.PointList
import com.graphhopper.util.shapes.GHPoint
import io.mockk.classMockk
import io.mockk.every
import jon.test.scorers.FeatureScorer
import org.junit.jupiter.api.*
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class CourseScorerTest {

    lateinit var scorer: CourseScorer
    lateinit var csf: ControlSiteFinder
    lateinit var mockResponse: GHResponse
    lateinit var step: CourseImprover
    lateinit var course: Course
    lateinit var mockFS1: FeatureScorer
    lateinit var mockFS2: FeatureScorer
    lateinit var mockFS3: FeatureScorer
    lateinit var mockRoute: GHResponse

    @BeforeAll
    fun beforeAll() {
        csf = classMockk(ControlSiteFinder::class)
        mockResponse = classMockk(GHResponse::class)
        every { csf.routeRequest(any(), any()) } returns mockResponse
        mockFS1 = classMockk(FeatureScorer::class)
        mockFS2 = classMockk(FeatureScorer::class)
        mockFS3 = classMockk(FeatureScorer::class)
        mockRoute = classMockk(GHResponse::class)
        every { mockRoute.best.distance } returns 1000.0
        every { mockResponse.hasAlternatives() } returns true

    }

    @BeforeEach
    fun setUp() {
        course = Course(controls = listOf(GHPoint(1.0, 2.0), GHPoint(1.5, 2.5), GHPoint(1.5, 2.5), GHPoint(1.5, 2.5), GHPoint(1.5, 2.5), GHPoint(1.5, 2.5)))
        course.route = mockRoute.best
        step = CourseImprover(csf, course)
        scorer = CourseScorer(listOf(mockFS1, mockFS2, mockFS3), csf::findRoutes)
    }

    @AfterEach
    fun tearDown() {
    }

    // Constraints

//    @Test
//    fun scoreHasErrors() {
//
//        every { mockRoute.hasErrors() } returns true
//        val score = scorer.score(step, mockRoute)
//        assertEquals(10000.0, score)
//    }
//
//    @Test
//    fun scoreTooBigToMap() {
//
//        every { mockResponse.hasErrors() } returns false
//        every { mockResponse.best.points } returns PointList.EMPTY
//        every { csf.routeFitsBox(any(), any()) } returns false
//
//        val score = scorer.score(step, mockRoute)
//        assertEquals(10000.0, score)
//    }
//
//    @Test
//    fun scoreNoErrors() {
//        every { mockResponse.hasErrors() } returns false
//        every { mockResponse.best.points } returns PointList.EMPTY
//        every { csf.routeFitsBox(any(), any()) } returns true
//        every { csf.findRoutes(any(), any()) } returns mockResponse
//
//        every {mockFS1.score(any(), any())} returns course.drop(2).map { 0.0 }
//        every {mockFS2.score(any(), any())} returns course.drop(2).map { 0.0 }
//        every {mockFS3.score(any(), any())} returns course.drop(2).map { 0.0 }
//
//        val score = scorer.score(step, mockRoute)
//        assertEquals(0.0, score)
//        assertNotNull(step.numberedControlScores)
//        // there are 5 controls (inc s + f) then there are 3 numbered control scores
//        assertEquals(course.size - 2, step.numberedControlScores.size)
//        assertTrue(step.numberedControlScores.all {it == 0.0})
//    }

    @Test
    fun scoresAreCorrect() {
        every { mockResponse.hasErrors() } returns false
        every { mockResponse.best.points } returns PointList.EMPTY
        every { csf.routeFitsBox(any(), any()) } returns true
        every { csf.findRoutes(any(), any()) } returns mockResponse

        /*
                featureScores =
                        1       2       3       4       5       6
                FS1     0.1     0.2     0.1     0.1     0.5     0.0
                FS2     0.2     0.1     0.1     0.4     0.2     0.0
                FS3     0.3     0.1     0.2     0.0     0.0     0.4

                step.numberedControlScores = 0.2, 0.167, 0.167, 0.167, 0.267, 0.167
          */
        val fs1 = listOf( 0.1, 0.2, 0.1, 0.1, 0.5, 0.0)
        val fs2 = listOf(0.2, 0.1, 0.1, 0.4, 0.2, 0.0)
        val fs3 = listOf(0.3, 0.1, 0.2, 0.0, 0.0, 0.4)

        every {mockFS1.score(any(), any())} returns  fs1
        every {mockFS2.score(any(), any())} returns  fs2
        every {mockFS3.score(any(), any())} returns  fs3

        val expectedNumberedControlScores = listOf(
                (fs1[0] + fs2[0] + fs3[0]) / 3,
                (fs1[1] + fs2[1] + fs3[1]) / 3,
                (fs1[2] + fs2[2] + fs3[2]) / 3,
                (fs1[3] + fs2[3] + fs3[3]) / 3,
                (fs1[4] + fs2[4] + fs3[4]) / 3,
                (fs1[5] + fs2[5] + fs3[5]) / 3
        )

        val ans = scorer.score(course)
        assertEquals(expectedNumberedControlScores, course.numberedControlScores)
        assertEquals(expectedNumberedControlScores.average(), ans)
    }


    @Test
    fun transpose() {
        val ins = listOf(listOf(1,2,3), listOf(4,5,6), listOf(7,8,9))
        val expected = listOf(listOf(1,4,7), listOf(2,5,8), listOf(3,6,9))

        val ans = scorer.transpose(ins)
        assertEquals(expected, ans)
    }

    @Test
    fun transpose1() {
        val ins = listOf(listOf(1,2,3))
        val expected = listOf(listOf(1), listOf(2), listOf(3))

        val ans = scorer.transpose(ins)
        assertEquals(expected, ans)
    }

    @Test
    fun transpose0() {
        val ins: List<List<Int>> = listOf(emptyList<Int>())

        val ans: List<List<Int>> = scorer.transpose(ins)
        assertEquals(0, ans.size)
    }
}