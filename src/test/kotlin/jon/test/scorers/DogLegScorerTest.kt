package jon.test.scorers

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class DogLegScorerTest {

    private val scorer = DogLegScorer()


    @Test
    fun dogLegs() {
        val startTo1 = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10) // 3 from 10 common
        val l1To2 = listOf(10, 1, 2, 3, 11, 12, 13) // 3 from 6 common
        val l2To3 = listOf(110, 21, 22, 23, 211, 212, 213) // unique
        val l3ToFinish = listOf(310, 31, 32, 33, 311, 312, 313) // unique
        val scores = scorer.dogLegs(listOf(startTo1, l1To2, l2To3, l3ToFinish))
        assertEquals(3, scores.size)
        assertEquals(listOf(0.0, 0.5, 0.0), scores)
    }

    @Test
    fun dogLegsNoNumberedControls() {
        val startDirectlyToFinish = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10) // 3 from 10 common
        val scores = scorer.dogLegs(listOf(startDirectlyToFinish))
        assertEquals(0, scores.size)
    }

    @Test
    fun dogLegsOneNumberedControl() {
        val startTo1 = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
        val l1ToFinish = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).reversed()

        // first leg can't be a dog leg and we can't move the finish so that is not evaluated
        val scores = scorer.dogLegs(listOf(startTo1, l1ToFinish))
        assertEquals(1, scores.size)
        assertEquals(0.0, scores[0]) // control 1 is NOT a dog leg in this course
    }

    @Test
    fun dogLegsTwoNumberedControlsDogLegToFinish() {
        val startTo1 = listOf(100,101,102,1)
        val l1To2 = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
        val l2ToFinish = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).reversed()

        // first leg can't be a dog leg and we can't move the finish so that is not evaluated
        val scores = scorer.dogLegs(listOf(startTo1, l1To2, l2ToFinish))
        assertEquals(2, scores.size) // 2 controls -- 2 scores
        assertEquals(listOf(0.0, 0.0), scores) // control 2 is NOT a dog leg in this course
    }

    @Test
    fun dogLegsTwoNumberedControlsDogLegInMiddle() {
        val  startTo1 = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
        val l1To2 = listOf(4, 5, 6, 7, 8, 9, 10).reversed()
        val l2ToFinish = listOf(4,101,102,1)

        // first leg can't be a dog leg and we can't move the finish so that is not evaluated
        val scores = scorer.dogLegs(listOf(startTo1, l1To2, l2ToFinish))
        assertEquals(2, scores.size) // 2 controls -- 2 scores
        assertEquals(listOf(0.0, 1.0), scores) // control 2 is  a dog leg in this course
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

    @Test
    fun controlsInSamePlace() {
        val a2b = listOf(10)
        val b2c = listOf(10, 1, 2, 3, 11, 12, 13)
        val score = scorer.dogLegScore(a2b, b2c)
        assertEquals(1.0, score)

    }
}