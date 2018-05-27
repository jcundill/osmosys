package jon.test.scorers

import com.graphhopper.GHResponse
import com.graphhopper.util.shapes.GHPoint
import io.mockk.classMockk
import io.mockk.every
import jon.test.Params
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

    @BeforeAll
    fun beforeTests() {
        rsStartTo1 = classMockk(GHResponse::class)
        rs1ToFinish = classMockk(GHResponse::class)
        cr = classMockk(GHResponse::class)
    }

    @BeforeEach
    fun setUp() {
        scorer = CourseLengthScorer(Params(distance = 10.0, start= GHPoint(1.0, 33.2)))
    }

    @Test
    fun correctLength() {
        every { cr.best.distance } returns 10.0

        val score = scorer.score(listOf(rsStartTo1, rs1ToFinish), cr)

        assertEquals(0.0, score[0])
        assertEquals(0.0, score[1])
    }

    @Test
    fun half() {
        every { cr.best.distance } returns 5.0

        val score = scorer.score(listOf(rsStartTo1, rs1ToFinish), cr)

        assertEquals(0.5, score[0])
        assertEquals(0.5, score[1])
    }

    @Test
    fun double() {
        every { cr.best.distance } returns 20.0

        val score = scorer.score(listOf(rsStartTo1, rs1ToFinish), cr)

        assertEquals(0.5, score[0])
        assertEquals(0.5, score[1])
    }

    @Test
    fun triple() {
        every { cr.best.distance } returns 30.0

        val score = scorer.score(listOf(rsStartTo1, rs1ToFinish), cr)

        assertEquals(0.6666666666666666, score[0])
        assertEquals(0.6666666666666666, score[1])
    }

    @Test
    fun quarter() {
        every { cr.best.distance } returns 2.5

        val score = scorer.score(listOf(rsStartTo1, rs1ToFinish), cr)

        assertEquals(0.75, score[0])
        assertEquals(0.75, score[1])
    }
}