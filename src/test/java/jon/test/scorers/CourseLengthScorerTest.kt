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
internal class CourseLengthScorerTest {

    lateinit var rsStartTo1: GHResponse
    lateinit var rs1To2: GHResponse
    lateinit var rs2ToFinish: GHResponse
    lateinit var cr: GHResponse

    private val params = CourseParameters(distance = 10.0, start= GHPoint(1.0, 33.2))
    val scorer = CourseLengthScorer(params)


    @BeforeAll
    fun beforeTests() {
        rsStartTo1 = classMockk(GHResponse::class)
        rs1To2 = classMockk(GHResponse::class)
        rs2ToFinish = classMockk(GHResponse::class)
        cr = classMockk(GHResponse::class)
    }

    @Test
    fun algo() {
        val legs = listOf(100.0, 50.0, 50.0)

        val scores = scorer.algo(legs, 200.0)

        assertEquals(listOf( 0.75, 0.5 ), scores) // 3 legs = 2 scores

    }

    @Test
    fun correctLength() {
        val dist = params.distance

        every { cr.best.distance } returns dist
        every { rsStartTo1.best.distance } returns dist * 0.5
        every { rs1To2.best.distance } returns dist * 0.25
        every { rs2ToFinish.best.distance } returns dist * 0.25

        val score = scorer.score(listOf(rsStartTo1, rs1To2, rs2ToFinish), cr)

        assertEquals(0.0, score[0])
        assertEquals(0.0, score[1])
    }

    @Test
    fun tooShort() {
        val dist = params.minAllowedDistance * 0.8

        every { cr.best.distance } returns dist
        every { rsStartTo1.best.distance } returns dist * 0.5
        every { rs1To2.best.distance } returns dist * 0.25
        every { rs2ToFinish.best.distance } returns dist * 0.25

        val score = scorer.score(listOf(rsStartTo1, rs1To2, rs2ToFinish), cr)

        assertEquals(0.25, score[0])
        assertEquals(0.5, score[1])
    }

    @Test
    fun shortButInTolerance() {
        val dist = params.minAllowedDistance * 1.2

        every { cr.best.distance } returns dist
        every { rsStartTo1.best.distance } returns dist * 0.5
        every { rs1To2.best.distance } returns dist * 0.25
        every { rs2ToFinish.best.distance } returns dist * 0.25

        val score = scorer.score(listOf(rsStartTo1, rs1To2, rs2ToFinish), cr)
        assertEquals(0.0, score[0])
        assertEquals(0.0, score[1])
    }

    @Test
    fun tooLong() {
        val dist = params.maxAllowedDistance * 1.2

        every { cr.best.distance } returns dist
        every { rsStartTo1.best.distance } returns dist * 0.5
        every { rs1To2.best.distance } returns dist * 0.25
        every { rs2ToFinish.best.distance } returns dist * 0.25

        val score = scorer.score(listOf(rsStartTo1, rs1To2, rs2ToFinish), cr)

        assertEquals(0.75, score[0])
        assertEquals(0.5, score[1])
    }

    @Test
    fun longButInTolerance() {
        val dist = params.maxAllowedDistance * 0.8

        every { cr.best.distance } returns dist
        every { rsStartTo1.best.distance } returns dist * 0.5
        every { rs1To2.best.distance } returns dist * 0.25
        every { rs2ToFinish.best.distance } returns dist * 0.25

        val score = scorer.score(listOf(rsStartTo1, rs1To2, rs2ToFinish), cr)
        assertEquals(0.0, score[0])
        assertEquals(0.0, score[1])
    }
}