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

    lateinit var rsStartTo1: GHResponse
    lateinit var rs1To2: GHResponse
    lateinit var rs2To3: GHResponse
    lateinit var rs3ToFinish: GHResponse
    lateinit var cr: GHResponse
    lateinit var scorer: FeatureScorer

    @BeforeAll
    fun beforeTests() {
        rsStartTo1 = classMockk(GHResponse::class)
        rs3ToFinish = classMockk(GHResponse::class)
        rs1To2 = classMockk(GHResponse::class)
        rs2To3 = classMockk(GHResponse::class)
        cr = classMockk(GHResponse::class)

        scorer = FollowingLegLengthScorer(CourseParameters(distance=3000.0, start= GHPoint(1.0, 33.2)))

    }

    @Test
    fun scoreAllInBounds() {
        every { rsStartTo1.best.distance } returns 150.0
        every { rs1To2.best.distance } returns 150.0
        every { rs2To3.best.distance } returns 150.0
        every { rs3ToFinish.best.distance } returns 150.0

        val scores = scorer.score(listOf(rsStartTo1, rs1To2, rs2To3, rs3ToFinish), cr)

        assertEquals(0.0, scores[0])
        assertEquals(0.0, scores[1])
        assertEquals(0.0, scores[2])
    }

    @Test
    fun score1TooCloseToStart() {
        every { rsStartTo1.best.distance } returns 15.0
        every { rs1To2.best.distance } returns 150.0
        every { rs2To3.best.distance } returns 150.0
        every { rs3ToFinish.best.distance } returns 150.0

        val scores = scorer.score(listOf(rsStartTo1, rs1To2, rs2To3, rs3ToFinish), cr)

        assertEquals(0.0, scores[0]) // following leg, we are not interested in moving the start
        assertEquals(0.0, scores[1])
        assertEquals(0.0, scores[2])
    }

    @Test
    fun score2TooFarAwayFrom1() {
        every { rsStartTo1.best.distance } returns 150.0
        every { rs1To2.best.distance } returns 1500.0
        every { rs2To3.best.distance } returns 150.0
        every { rs3ToFinish.best.distance } returns 150.0

        val scores = scorer.score(listOf(rsStartTo1, rs1To2, rs2To3, rs3ToFinish), cr)

        assertEquals(1.0, scores[0])
        assertEquals(0.0, scores[1])
        assertEquals(0.0, scores[2])
    }

    @Test
    fun score2TooCloseTo1() {
        every { rsStartTo1.best.distance } returns 150.0
        every { rs1To2.best.distance } returns 15.0
        every { rs2To3.best.distance } returns 150.0
        every { rs3ToFinish.best.distance } returns 150.0

        val scores = scorer.score(listOf(rsStartTo1, rs1To2, rs2To3, rs3ToFinish), cr)

        assertEquals(1.0, scores[0])
        assertEquals(0.0, scores[1])
        assertEquals(0.0, scores[2])
    }

    @Test
    fun scoreAllTooClose() {
        every { rsStartTo1.best.distance } returns 15.0
        every { rs1To2.best.distance } returns 15.0
        every { rs2To3.best.distance } returns 15.0
        every { rs3ToFinish.best.distance } returns 15.0

        val scores = scorer.score(listOf(rsStartTo1, rs1To2, rs2To3, rs3ToFinish), cr)

        assertEquals(1.0, scores[0])
        assertEquals(1.0, scores[1])
        assertEquals(1.0, scores[2])
    }
}