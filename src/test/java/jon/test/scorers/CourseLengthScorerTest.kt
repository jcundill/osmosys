package jon.test.scorers

import com.graphhopper.GHResponse
import com.graphhopper.util.shapes.GHPoint
import io.mockk.classMockk
import io.mockk.every
import jon.test.CourseParameters
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class CourseLengthScorerTest {

    lateinit var rsStartTo1: GHResponse
    lateinit var rs1ToFinish: GHResponse
    lateinit var cr: GHResponse
    lateinit var scorer: FeatureScorer

    private val params = CourseParameters(distance = 10.0, start= GHPoint(1.0, 33.2))

    @BeforeAll
    fun beforeTests() {
        rsStartTo1 = classMockk(GHResponse::class)
        rs1ToFinish = classMockk(GHResponse::class)
        cr = classMockk(GHResponse::class)
    }

    @BeforeEach
    fun setUp() {
        scorer = CourseLengthScorer(params)
    }

    @Test
    fun correctLength() {
        every { cr.best.distance } returns 10.0

        val score = scorer.score(listOf(rsStartTo1, rs1ToFinish), cr)

        assertEquals(0.0, score[0])
        assertEquals(0.0, score[1])
    }

    @Test
    fun tooShort() {
        every { cr.best.distance } returns params.minAllowedDistance * 0.8

        val score = scorer.score(listOf(rsStartTo1, rs1ToFinish), cr)

        assertEquals(1.0, score[0])
        assertEquals(1.0, score[1])
    }

    @Test
    fun shortButInTolerance() {
        every { cr.best.distance } returns params.minAllowedDistance * 1.2

        val score = scorer.score(listOf(rsStartTo1, rs1ToFinish), cr)

        assertEquals(0.0, score[0])
        assertEquals(0.0, score[1])
    }

    @Test
    fun tooLong() {
        every { cr.best.distance } returns params.maxAllowedDistance * 1.2

        val score = scorer.score(listOf(rsStartTo1, rs1ToFinish), cr)

        assertEquals(1.0, score[0])
        assertEquals(1.0, score[1])
    }

    @Test
    fun longButInTolerance() {
        every { cr.best.distance } returns params.maxAllowedDistance * 0.8

        val score = scorer.score(listOf(rsStartTo1, rs1ToFinish), cr)

        assertEquals(0.0, score[0])
        assertEquals(0.0, score[1])
    }
}