package jon.test.scorers

import com.graphhopper.util.shapes.GHPoint
import jon.test.CourseParameters
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class DogLegScorerTest {

    private val scorer = DogLegScorer(CourseParameters(distance = 3000.0, start = GHPoint(1.0, 33.2)))


    @Test
    fun dogLegs() {
        val a2b = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10) // 3 from 10 common
        val b2c = listOf(10, 1, 2, 3, 11, 12, 13) // 3 from 6 common
        val c2d = listOf(110, 21, 22, 23, 211, 212, 213) // unique
        val d2e = listOf(310, 31, 32, 33, 311, 312, 313) // unique
        val scores = scorer.dogLegs(listOf(a2b, b2c, c2d, d2e))
        assertEquals(3, scores.size)
        assertEquals(listOf(0.5, 0.0, 0.0), scores)
    }

    @Test
    fun dogLegsOneLeg() {
        val a2b = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10) // 3 from 10 common
        val scores = scorer.dogLegs(listOf(a2b))
        assertEquals(0, scores.size)
    }

    @Test
    fun dogLegScoreNoInCommon() {
        val a2b = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
        val b2c = listOf(10, 11, 12, 13, 14, 15)
        val score = scorer.dogLegScore(a2b, b2c)
        assertEquals(0.0, score)
    }

    @Test
    fun dogLegScoreAllInCommon() {
        val a2b = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
        val b2c = listOf(10, 1, 2, 3, 4, 5)
        val score = scorer.dogLegScore(a2b, b2c)
        assertEquals(1.0, score)
    }

    @Test
    fun dogLegScoreAnsIsWorstRatio() {
        val a2b = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10) // 3 from 10 common
        val b2c = listOf(10, 1, 2, 3, 11, 12, 13) // 3 from 6 common
        val score = scorer.dogLegScore(a2b, b2c)
        assertEquals(0.5, score)
    }
}