package jon.test.scorers

import com.graphhopper.GHResponse
import com.graphhopper.util.shapes.GHPoint
import io.mockk.classMockk
import io.mockk.every
import jon.test.Params
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class RouteChoiceScorerTest {

    lateinit var rs1: GHResponse
    lateinit var rs2: GHResponse
    lateinit var cr: GHResponse

    @BeforeAll
    fun beforeTests() {
        rs1 = classMockk(GHResponse::class)
        rs2 = classMockk(GHResponse::class)
        cr = classMockk(GHResponse::class)
    }

    @Test
    fun score() {
        val scorer = FollowingLegRouteChoiceScorer(Params(start= GHPoint(1.0, 33.2)))

        every { rs1.hasAlternatives() } returns true
        every { rs2.hasAlternatives() } returns false

        val scores = scorer.score(listOf(rs1, rs2), cr)

        assertEquals(0.0, scores[0])
        assertEquals(1.0, scores[1])
    }
}