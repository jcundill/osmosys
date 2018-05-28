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
internal class RouteChoiceScorerTest {

    lateinit var rsStartTo1: GHResponse
    lateinit var rs1ToFinish: GHResponse
    lateinit var cr: GHResponse

    @BeforeAll
    fun beforeTests() {
        rsStartTo1 = classMockk(GHResponse::class)
        rs1ToFinish = classMockk(GHResponse::class)
        cr = classMockk(GHResponse::class)
    }

    @Test
    fun score() {
        val scorer = FollowingLegRouteChoiceScorer(CourseParameters(start= GHPoint(1.0, 33.2)))

        every { rsStartTo1.hasAlternatives() } returns true
        every { rs1ToFinish.hasAlternatives() } returns false

        val scores = scorer.score(listOf(rsStartTo1, rs1ToFinish), cr)

        assertEquals(1.0, scores[0])
    }
}