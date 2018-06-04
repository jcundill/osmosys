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
internal class LegLengthScorerTest {

    lateinit var rs1To2: GHResponse
    lateinit var rs2To3: GHResponse
    lateinit var rsStartTo1: GHResponse
    lateinit var rs3ToFinish: GHResponse
    lateinit var cr: GHResponse

    val scorer = LegLengthScorer(CourseParameters(distance=3000.0, start= GHPoint(1.0, 33.2)))
    lateinit var legScores: List<GHResponse>

    @BeforeAll
    fun beforeTests() {
        rs1To2 = classMockk(GHResponse::class)
        rs2To3 = classMockk(GHResponse::class)
        rsStartTo1 = classMockk(GHResponse::class)
        rs3ToFinish = classMockk(GHResponse::class)
        cr = classMockk(GHResponse::class)
        legScores = listOf(rsStartTo1, rs1To2, rs2To3, rs3ToFinish)
    }

    @Test
    fun scoreAllInBounds() {
        every { rsStartTo1.best.distance } returns 150.0
        every { rs1To2.best.distance } returns 150.0
        every { rs2To3.best.distance } returns 150.0
        every { rs3ToFinish.best.distance } returns 150.0

        val scores = scorer.score(legScores, cr)

        assertEquals(0.0, scores[0])
        assertEquals(0.0, scores[1])
        assertEquals(0.0, scores[2])
    }

    @Test
    fun score1TooCloseTo2() {
        every { rsStartTo1.best.distance } returns 150.0
        every { rs1To2.best.distance } returns 15.0
        every { rs2To3.best.distance } returns 150.0
        every { rs3ToFinish.best.distance } returns 150.0

        val scores = scorer.score(legScores, cr)

        assertEquals(0.0, scores[0])
        assertEquals(1.0, scores[1])
        assertEquals(0.0, scores[2])
    }

    @Test
    fun score1TooFarAwayFrom2() {
        every { rsStartTo1.best.distance } returns 150.0
        every { rs1To2.best.distance } returns 1500.0
        every { rs2To3.best.distance } returns 150.0
        every { rs3ToFinish.best.distance } returns 150.0

        val scores = scorer.score(legScores, cr)

        assertEquals(0.0, scores[0])
        assertEquals(1.0, scores[1])
        assertEquals(0.0, scores[2])
    }

    @Test
    fun scoreAllTooClose() {
        every { rsStartTo1.best.distance } returns 15.0
        every { rs1To2.best.distance } returns 15.0
        every { rs2To3.best.distance } returns 15.0
        every { rs3ToFinish.best.distance } returns 15.0

        val scores = scorer.score(legScores, cr)

        assertEquals(1.0, scores[0])
        assertEquals(1.0, scores[1])
        assertEquals(1.0, scores[2])
    }
}