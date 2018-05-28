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
internal class DidntMoveScorerTest {

    lateinit var rsStartTo1: GHResponse
    lateinit var rs1To2: GHResponse
    lateinit var rs2ToFinish: GHResponse
    lateinit var cr: GHResponse

    @BeforeAll
    fun beforeTests() {
        rsStartTo1 = classMockk(GHResponse::class)
        rs1To2 = classMockk(GHResponse::class)
        rs2ToFinish = classMockk(GHResponse::class)
        cr = classMockk(GHResponse::class)
    }

    @Test
    fun scoreMoved() {
        val scorer = DidntMoveScorer(CourseParameters(distance=3000.0, start= GHPoint(1.0, 33.2)))

        every { rsStartTo1.best.points.getLat(0) } returns 53.0
        every { rsStartTo1.best.points.getLon(0) } returns -1.0
        every { rs1To2.best.points.getLat(0) } returns 54.0
        every { rs1To2.best.points.getLon(0) } returns -1.0
        every { rs2ToFinish.best.points.getLat(0) } returns 54.0
        every { rs2ToFinish.best.points.getLon(0) } returns -2.0

        val scores = scorer.score(listOf(rsStartTo1, rs1To2, rs2ToFinish), cr)


        assertEquals(0.0, scores[0])
        assertEquals(0.0, scores[1])

        assertEquals(2, scores.size) // 3 legs = 2 scores
    }
    @Test
    fun scoreDidntMove() {
        val scorer = DidntMoveScorer(CourseParameters(distance=3000.0, start= GHPoint(1.0, 33.2)))

        every { rsStartTo1.best.points.getLat(0) } returns 53.0
        every { rsStartTo1.best.points.getLon(0) } returns -1.0
        every { rs1To2.best.points.getLat(0) } returns 54.0
        every { rs1To2.best.points.getLon(0) } returns -1.0
        every { rs2ToFinish.best.points.getLat(0) } returns 54.0
        every { rs2ToFinish.best.points.getLon(0) } returns -1.0

        val scores = scorer.score(listOf(rsStartTo1, rs1To2, rs2ToFinish), cr)


        assertEquals(0.0, scores[0])
        assertEquals(1.0, scores[1])
    }

    @Test
    fun scoreDidntMoveAtAll() {
        val scorer = DidntMoveScorer(CourseParameters(distance=3000.0, start= GHPoint(1.0, 33.2)))

        every { rsStartTo1.best.points.getLat(0) } returns 53.0
        every { rsStartTo1.best.points.getLon(0) } returns -1.0
        every { rs1To2.best.points.getLat(0) } returns 53.0
        every { rs1To2.best.points.getLon(0) } returns -1.0
        every { rs2ToFinish.best.points.getLat(0) } returns 53.0
        every { rs2ToFinish.best.points.getLon(0) } returns -1.0

        val scores = scorer.score(listOf(rsStartTo1, rs1To2, rs2ToFinish), cr)


        assertEquals(1.0, scores[0])
        assertEquals(1.0, scores[1])
    }
}
