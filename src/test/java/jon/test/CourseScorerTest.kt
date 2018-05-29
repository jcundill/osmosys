package jon.test

import com.graphhopper.GHResponse
import com.graphhopper.util.PointList
import com.graphhopper.util.shapes.GHPoint
import io.mockk.classMockk
import io.mockk.every
import jon.test.scorers.FeatureScorer
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class CourseScorerTest {

    lateinit var scorer: CourseScorer
    lateinit var csf: ControlSiteFinder
    val params = CourseParameters(distance=3000.0, start= GHPoint(1.0, 33.2))
    lateinit var mockResponse: GHResponse
    lateinit var step: CourseImprover
    lateinit var controls: List<GHPoint>
    lateinit var mockFS1: FeatureScorer
    lateinit var mockFS2: FeatureScorer

    @BeforeAll
    fun beforeAll() {
        csf = classMockk(ControlSiteFinder::class)
        mockResponse = classMockk(GHResponse::class)
        every { csf.routeRequest(any(), any()) } returns mockResponse
        mockFS1 = classMockk(FeatureScorer::class)
        mockFS2 = classMockk(FeatureScorer::class)

    }

    @BeforeEach
    fun setUp() {
        controls = listOf(GHPoint(1.0, 2.0), GHPoint(1.5, 2.5), GHPoint(1.5, 2.5), GHPoint(1.5, 2.5), GHPoint(1.5, 2.5))
        step = CourseImprover(csf, controls)
        scorer = CourseScorer(csf, listOf(mockFS1, mockFS2), params)

        every {mockFS1.score(any(), any())} returns controls.drop(2).map { 0.5 }
        every {mockFS2.score(any(), any())} returns controls.drop(2).map { 0.5 }
    }

    @AfterEach
    fun tearDown() {
    }

    @Test
    fun scoreHasErrors() {

        every { mockResponse.hasErrors() } returns true
        val score = scorer.score(step)
        assertEquals(10000.0, score)
    }

    @Test
    fun scoreTooBigToMap() {

        every { mockResponse.hasErrors() } returns false
        every { mockResponse.best.points } returns PointList.EMPTY
        every { csf.routeFitsBox(any(), any()) } returns false

        val score = scorer.score(step)
        assertEquals(10000.0, score)
    }

    @Test
    fun scoreNoErrors() {
        every { mockResponse.hasErrors() } returns false
        every { mockResponse.best.points } returns PointList.EMPTY
        every { csf.routeFitsBox(any(), any()) } returns true
        every { csf.findRoutes(any(), any()) } returns mockResponse

        val score = scorer.score(step)
        assertEquals(0.5, score)
        assertNotNull(step.numberedControlScores)
        // there are 5 controls (inc s + f) then there are 3 numbered control scores
        assertEquals(controls.size - 2, step.numberedControlScores.size)
        assertTrue(step.numberedControlScores.all {it == 0.5})
    }

    @Test
    fun scoresAreCorrect() {
        every { mockResponse.hasErrors() } returns false
        every { mockResponse.best.points } returns PointList.EMPTY
        every { csf.routeFitsBox(any(), any()) } returns true
        every { csf.findRoutes(any(), any()) } returns mockResponse

        every {mockFS1.score(any(), any())} returns listOf(0.0) + controls.drop(1).map { 0.2 }
        every {mockFS2.score(any(), any())} returns listOf(0.0) + controls.drop(1).map { 0.4 }

        scorer.score(step)
        assertTrue(step.numberedControlScores.drop(1).all {it > 0.299999 && it < 3.00001})
        assertEquals(0.0, step.numberedControlScores.first())
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
        val ins = listOf(emptyList<Int>())
        val expected = emptyList<Int>()

        val ans = scorer.transpose(ins)

        assertEquals(expected, ans)
    }
}