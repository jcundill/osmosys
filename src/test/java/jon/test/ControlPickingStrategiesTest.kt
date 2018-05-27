package jon.test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class ControlPickingStrategiesTest{

    @Test
    fun random() {
        val ans = ControlPickingStrategies.pickRandomly(listOf(0.1,0.2,0.3,0.4,0.5,0.6,0.7,0.8), 4)
        assertEquals(4, ans.size)
        assertFalse(ans.contains(0))
        assertFalse(ans.contains(8))
    }
    @Test
    fun randomNone() {
        val ans = ControlPickingStrategies.pickRandomly(listOf(0.1,0.2,0.3,0.4,0.5,0.6,0.7,0.8), 0)
        assertEquals(0, ans.size)
    }

    @Test
    fun randomTooMany() {
        val ans = ControlPickingStrategies.pickRandomly(listOf(0.1,0.2,0.3,0.4,0.5,0.6,0.7,0.8), 10)
        assertEquals(7, ans.size) //not offered the last in the scores and can't pick the start
        assertFalse(ans.contains(0))
        assertFalse(ans.contains(8))
    }

    @Test
    fun threshold() {
        val scores = listOf(0.1,0.2,0.3,0.4,0.5,0.6,0.7,0.8)
        val av = scores.average()
        val above = scores.filter { it > av }
        val ans = ControlPickingStrategies.pickAboveAverage(scores, 4)

        assertEquals(4, ans.size)
        assertFalse(ans.contains(0))
        assertFalse(ans.contains(8))
        ans.forEach {
            assertTrue(above.contains(scores[it]))
        }
    }

    @Test
    fun thresholdNone() {
        val ans = ControlPickingStrategies.pickRandomly(listOf(0.1,0.2,0.3,0.4,0.5,0.6,0.7,0.8), 0)
        assertEquals(0, ans.size)
    }

    @Test
    fun thresholdTooMany() {
        val scores = listOf(0.1,0.2,0.3,0.4,0.5,0.6,0.7,0.8)
        val av = scores.average()
        val above = scores.filter { it > av }
        val ans = ControlPickingStrategies.pickAboveAverage(scores, 10)
        assertEquals(4, ans.size) //not offered the last in the scores and can't pick the start
        assertFalse(ans.contains(0))
        assertFalse(ans.contains(8))
        ans.forEach {
            assertTrue(above.contains(scores[it]))
        }
     }


}