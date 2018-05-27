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
    val params = Params(distance=3000.0, start= GHPoint(1.0, 33.2))
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

        every {mockFS1.score(any(), any())} returns controls.map { 0.5 }
        every {mockFS2.score(any(), any())} returns controls.map { 0.5 }
    }

    @AfterEach
    fun tearDown() {
    }

    @Test
    fun scoreHasErrors() {

        every { mockResponse.hasErrors() } returns true
        val score = scorer.score(step)
        assertEquals(10000.0, score)
        assertNull(step.legScores)
    }

    @Test
    fun scoreTooBigToMap() {

        every { mockResponse.hasErrors() } returns false
        every { mockResponse.best.points } returns PointList.EMPTY
        every { csf.routeFitsBox(any(), any(), any()) } returns false

        val score = scorer.score(step)
        assertEquals(10000.0, score)
        assertNull(step.legScores)
    }

    @Test
    fun scoreNoErrors() {
        every { mockResponse.hasErrors() } returns false
        every { mockResponse.best.points } returns PointList.EMPTY
        every { csf.routeFitsBox(any(), any(), any()) } returns true
        every { csf.findRoutes(any(), any()) } returns mockResponse

        val score = scorer.score(step)
        assertEquals(0.5, score)
        assertNotNull(step.legScores)
        assertEquals(controls.size - 1, step.legScores!!.size)
        assertTrue(step.legScores!!.all {it == 0.5})
    }

    @Test
    fun scoresAreCorrect() {
        every { mockResponse.hasErrors() } returns false
        every { mockResponse.best.points } returns PointList.EMPTY
        every { csf.routeFitsBox(any(), any(), any()) } returns true
        every { csf.findRoutes(any(), any()) } returns mockResponse

        every {mockFS1.score(any(), any())} returns listOf(0.0) + controls.drop(1).map { 0.2 }
        every {mockFS2.score(any(), any())} returns listOf(0.0) + controls.drop(1).map { 0.4 }

        scorer.score(step)
        assertTrue(step.legScores!!.drop(1).all {it > 0.299999 && it < 3.00001})
        assertEquals(0.0, step.legScores!!.first())
    }
}