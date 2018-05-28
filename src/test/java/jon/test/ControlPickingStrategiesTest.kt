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
        assertFalse(ans.contains(9)) // 8 legs = 9 controls
    }
    @Test
    fun randomNone() {
        val ans = ControlPickingStrategies.pickRandomly(listOf(0.1,0.2,0.3,0.4,0.5,0.6,0.7,0.8), 0)
        assertEquals(0, ans.size)
    }

    @Test
    fun randomTooMany() {
        val ans = ControlPickingStrategies.pickRandomly(listOf(0.1,0.2,0.3,0.4,0.5,0.6,0.7,0.8), 10)
        assertEquals(8, ans.size) //not offered the last in the scores and can't pick the start
        assertFalse(ans.contains(0))
        assertTrue(ans.contains(8))
        assertFalse(ans.contains(9))
    }

    @Test
    fun threshold() {
        val scores = listOf(0.1,0.2,0.3,0.4,0.5,0.6,0.7,0.8)
        val av = scores.average()
        val above = scores.drop(1).filter { it > av }
        val ans = ControlPickingStrategies.pickAboveAverage(scores, 4)

        assertEquals(above.size, ans.size)
        assertFalse(ans.contains(0))
        assertTrue(ans.contains(8))
        ans.forEach {
            assertTrue(above.contains(scores[it - 1]))
        }
    }

    @Test
    fun thresholdNone() {
        val ans = ControlPickingStrategies.pickRandomly(listOf(0.1,0.2,0.3,0.4,0.5,0.6,0.7,0.8), 0)
        assertEquals(0, ans.size)
    }

    @Test
    fun thresholdTooMany() {
        val scores = listOf(0.1,0.2,0.3,0.4,0.5,0.6,0.7,0.8) // 8 leg scores so 7 numbered controls
        val av = scores.average()
        val above = scores.filter { it > av }
        val ans = ControlPickingStrategies.pickAboveAverage(scores, 200)
        assertEquals(above.size, ans.size) //not offered the last in the scores and can't pick the start
        assertFalse(ans.contains(0))
        assertFalse(ans.contains(1))
        assertTrue(ans.contains(8))
        assertFalse(ans.contains(9))
        ans.forEach {
            assertTrue(above.contains(scores[it - 1]))
        }
     }


}