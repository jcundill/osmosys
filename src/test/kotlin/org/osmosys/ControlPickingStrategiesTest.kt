/*
 *
 *     Copyright (c) 2017-2020 Jon Cundill.
 *
 *     Permission is hereby granted, free of charge, to any person obtaining
 *     a copy of this software and associated documentation files (the "Software"),
 *     to deal in the Software without restriction, including without limitation
 *     the rights to use, copy, modify, merge, publish, distribute, sublicense,
 *     and/or sell copies of the Software, and to permit persons to whom the Software
 *     is furnished to do so, subject to the following conditions:
 *
 *     The above copyright notice and this permission notice shall be included in
 *     all copies or substantial portions of the Software.
 *
 *     THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *     EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 *     OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 *     IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 *     CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 *     TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE
 *     OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 *
 */

package org.osmosys

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class ControlPickingStrategiesTest{


    @Test
    fun pick() {
        val selector: (Pair<Int, Double>) -> Boolean = { (idx, score) -> score == 0.8 || idx == 1}
        val ans = ControlPickingStrategies.pick(listOf(0.1,0.2,0.3,0.4,0.5,0.6,0.7,0.8), 4, selector)
        assertEquals(2, ans.size)
        assertTrue(ans.contains(1))
        assertTrue(ans.contains(8))
    }

    @Test
    fun random() {
        val ans = ControlPickingStrategies.pickRandomly(listOf(0.1,0.2,0.3,0.4,0.5,0.6,0.7,0.8), 4)
        assert( ans.size <= 4) // sometimes don't get enough randoms
        assertFalse(ans.contains(0))
        assertFalse(ans.contains(9)) // 8 legs = 9 controls
    }
    @Test
    fun randomNone() {
        val ans = ControlPickingStrategies.pickRandomly(listOf(0.1,0.2,0.3,0.4,0.5,0.6,0.7,0.8), 0)
        assertEquals(0, ans.size)
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