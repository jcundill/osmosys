package jon.test.scorers

import com.graphhopper.GHResponse
import com.graphhopper.util.shapes.GHPoint
import io.mockk.classMockk
import io.mockk.every
import jon.test.CourseParameters
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class PreviousLegLengthScorerTest {

    lateinit var rs1: GHResponse
    lateinit var rs2: GHResponse
    lateinit var rsStart: GHResponse
    lateinit var rsFinish: GHResponse
    lateinit var cr: GHResponse

    val scorer = PreviousLegLengthScorer(CourseParameters(distance=3000.0, start= GHPoint(1.0, 33.2)))

    @BeforeAll
    fun beforeTests() {
        rs1 = classMockk(GHResponse::class)
        rs2 = classMockk(GHResponse::class)
        rsStart = classMockk(GHResponse::class)
        rsFinish = classMockk(GHResponse::class)
        cr = classMockk(GHResponse::class)
    }

    @Test
    fun scoreAllInBounds() {
        every { rsStart.best.distance } returns 150.0
        every { rs1.best.distance } returns 150.0
        every { rs2.best.distance } returns 150.0
        every { rsFinish.best.distance } returns 150.0

        val scores = scorer.score(legScores(), cr)

        assertEquals(0.0, scores[0])
        assertEquals(0.0, scores[1])
        assertEquals(0.0, scores[2])
    }

    private fun legScores() = listOf(rsStart, rs1, rs2, rsFinish)

    @Test
    fun score1TooCloseTo2() {
        every { rsStart.best.distance } returns 150.0
        every { rs1.best.distance } returns 15.0
        every { rs2.best.distance } returns 150.0
        every { rsFinish.best.distance } returns 150.0

        val scores = scorer.score(legScores(), cr)

        assertEquals(0.0, scores[0])
        assertEquals(1.0, scores[1])
        assertEquals(0.0, scores[2])
    }

    @Test
    fun score1TooFarAwayFrom2() {
        every { rsStart.best.distance } returns 150.0
        every { rs1.best.distance } returns 1500.0
        every { rs2.best.distance } returns 150.0
        every { rsFinish.best.distance } returns 150.0

        val scores = scorer.score(legScores(), cr)

        assertEquals(0.0, scores[0])
        assertEquals(1.0, scores[1])
        assertEquals(0.0, scores[2])
    }

    @Test
    fun scoreAllTooClose() {
        every { rsStart.best.distance } returns 15.0
        every { rs1.best.distance } returns 15.0
        every { rs2.best.distance } returns 15.0
        every { rsFinish.best.distance } returns 15.0

        val scores = scorer.score(legScores(), cr)

        assertEquals(1.0, scores[0])
        assertEquals(1.0, scores[1])
        assertEquals(1.0, scores[2])
    }
}