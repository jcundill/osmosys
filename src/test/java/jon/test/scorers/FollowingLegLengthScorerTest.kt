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
internal class FollowingLegLengthScorerTest {

    lateinit var rsStart: GHResponse
    lateinit var rs1: GHResponse
    lateinit var rs2: GHResponse
    lateinit var rsFinish: GHResponse
    lateinit var cr: GHResponse
    lateinit var scorer: FeatureScorer

    @BeforeAll
    fun beforeTests() {
        rsStart = classMockk(GHResponse::class)
        rsFinish = classMockk(GHResponse::class)
        rs1 = classMockk(GHResponse::class)
        rs2 = classMockk(GHResponse::class)
        cr = classMockk(GHResponse::class)

        scorer = FollowingLegLengthScorer(CourseParameters(distance=3000.0, start= GHPoint(1.0, 33.2)))

    }

    @Test
    fun scoreAllInBounds() {
        every { rsStart.best.distance } returns 150.0
        every { rs1.best.distance } returns 150.0
        every { rs2.best.distance } returns 150.0
        every { rsFinish.best.distance } returns 150.0

        val scores = scorer.score(listOf(rsStart, rs1, rs2, rsFinish), cr)

        assertEquals(0.0, scores[0])
        assertEquals(0.0, scores[1])
        assertEquals(0.0, scores[2])
        assertEquals(0.0, scores[3])
    }

    @Test
    fun score1TooCloseToStart() {
        every { rsStart.best.distance } returns 15.0
        every { rs1.best.distance } returns 150.0
        every { rs2.best.distance } returns 150.0
        every { rsFinish.best.distance } returns 150.0

        val scores = scorer.score(listOf(rsStart, rs1, rs2, rsFinish), cr)

        assertEquals(0.0, scores[0])
        assertEquals(0.0, scores[1])
        assertEquals(0.0, scores[2])
        assertEquals(0.0, scores[3])
    }

    @Test
    fun score2TooFarAwayFrom1() {
        every { rsStart.best.distance } returns 150.0
        every { rs1.best.distance } returns 1500.0
        every { rs2.best.distance } returns 150.0
        every { rsFinish.best.distance } returns 150.0

        val scores = scorer.score(listOf(rsStart, rs1, rs2, rsFinish), cr)

        assertEquals(0.0, scores[0])
        assertEquals(1.0, scores[1])
        assertEquals(0.0, scores[2])
        assertEquals(0.0, scores[3])
    }

    @Test
    fun score2TooCloseTo1() {
        every { rsStart.best.distance } returns 150.0
        every { rs1.best.distance } returns 15.0
        every { rs2.best.distance } returns 150.0
        every { rsFinish.best.distance } returns 150.0

        val scores = scorer.score(listOf(rsStart, rs1, rs2, rsFinish), cr)

        assertEquals(0.0, scores[0])
        assertEquals(1.0, scores[1])
        assertEquals(0.0, scores[2])
        assertEquals(0.0, scores[3])
    }

    @Test
    fun scoreAllTooClose() {
        every { rsStart.best.distance } returns 15.0
        every { rs1.best.distance } returns 15.0
        every { rs2.best.distance } returns 15.0
        every { rsFinish.best.distance } returns 15.0

        val scores = scorer.score(listOf(rsStart, rs1, rs2, rsFinish), cr)

        assertEquals(0.0, scores[0])
        assertEquals(1.0, scores[1])
        assertEquals(1.0, scores[2])
        assertEquals(0.0, scores[3])
    }
}