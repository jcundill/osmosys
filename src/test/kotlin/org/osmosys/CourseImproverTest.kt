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

import com.graphhopper.GHResponse
import com.graphhopper.util.shapes.GHPoint
import io.mockk.classMockk
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkClass
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.test.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class CourseImproverTest {

    lateinit var csf: ControlSiteFinder
    lateinit var improver: CourseImprover
    lateinit var mockRoutingResponse: GHResponse

    val replacedPoint = ControlSite(100.0, 100.0)
    val controls = listOf(ControlSite(1.0, 2.0), ControlSite(1.5, 2.5), ControlSite(1.5, 2.5), ControlSite(1.5, 2.5))
    val course = Course(controls = controls)


    @BeforeAll
    fun beforeTests() {
        csf = mockkClass(ControlSiteFinder::class)
        mockRoutingResponse = mockkClass(GHResponse::class)

        every { csf.routeRequest(controls = any()) } returns mockRoutingResponse
        every { csf.findAlternativeControlSiteFor(any()) } returns replacedPoint

    }

    @BeforeEach
    fun setUp() {
        improver = CourseImprover(csf, course)
    }

    @Test
    fun hashCodeWorksSame() {
        val improver2 = CourseImprover(csf, course)
        assertEquals(improver.hashCode(), improver2.hashCode())
    }

    @Test
    fun hashCodeWorksDifferent() {
        val course2 = Course(controls = listOf(ControlSite(1.0, 2.0), ControlSite(2.5, 2.5), ControlSite(1.5, 2.5), ControlSite(1.5, 2.5)))
        val improver2 = CourseImprover(csf, course2)
        assertNotEquals(improver.hashCode(), improver2.hashCode())
    }

    @Test
    fun equalsWorksSame() {
        val improver2 = CourseImprover(csf, course)
        assertEquals(improver, improver2)
    }

    @Test
    fun equalsWorksDifferent() {
        val course2 = Course(controls = listOf(ControlSite(1.0, 2.0), ControlSite(2.5, 2.5), ControlSite(1.5, 2.5), ControlSite(1.5, 2.5)))
        val improver2 = CourseImprover(csf, course2)
        assertNotEquals(improver, improver2)
    }

    // sometimes fails as can choose last
//    @Test
//    fun ifNoLegScoresChooseRandom() {
//
//        val improved = improver.step()
//
//        assertNotNull(improved)
//        assertEquals(controls.size, improved.controls.size)
//        assertEquals(1, improved.controls.filter { it == replacedPoint }.size)
//    }

    @Test
    fun findWorsts() {
        val scores = listOf(0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.1, 0.2, 0.3)
        val idxes = improver.findIndexesOfWorst(scores, 100)
        assertEquals(5, idxes.size )
    }

    @Test
    fun findWorstsWillNotChooseTheStart() {
        val scores = listOf(0.9, 0.2, 0.2, 0.2, 0.2)
        val idxes = improver.findIndexesOfWorst(scores, 1)
        assertEquals(1, idxes.size )
        assertNotEquals(0,idxes[0])
    }

    @Test
    fun findWorstsCannotChooseTheLastLeg() {
        val scores = listOf(0.2, 0.2, 0.2, 0.2, 0.9)
        val idxes = improver.findIndexesOfWorst(scores, 1)
        assertEquals(1, idxes.size )
        assertEquals(4,idxes[0])
    }

    @Test
    fun allTheSameTrue() {
        val scores = listOf(0.2, 0.2, 0.2, 0.2)
        assertTrue(improver.allTheSameScore(scores))
    }

    @Test
    fun allTheSameFalse() {
        val scores = listOf(0.2, 0.3, 0.1, 0.2, 0.9)
        assertFalse(improver.allTheSameScore(scores))
    }

    @Test
    fun allTheSameIncludesTheLastLeg() {
        val scores = listOf(0.2, 0.2, 0.2, 0.2, 0.9)
        assertFalse(improver.allTheSameScore(scores))
    }

    @Test
    fun replaceSelected() {
        val improved = improver.replaceSelectedNumberedControls(listOf(1, 2), controls)

        assertNotEquals(improved[0], replacedPoint)
        assertEquals(improved[1], replacedPoint)
        assertEquals(improved[2], replacedPoint)
        assertNotEquals(improved[3], replacedPoint)

    }

    @Test
    fun replaceSelectedNone() {
        val improved = improver.replaceSelectedNumberedControls(emptyList(), controls)

        assertNotEquals(improved[0], replacedPoint)
        assertNotEquals(improved[1], replacedPoint)
        assertNotEquals(improved[2], replacedPoint)
        assertNotEquals(improved[3], replacedPoint)

    }

    // ye it will - because we can't pass it the start or the finisg
//    @Test
//    fun replaceSelectedWillNotMoveTheStart() {
//        val improved = improver.replaceSelectedNumberedControls(listOf(0, 1), controls)
//
//        assertNotEquals(improved[0], replacedPoint)
//        assertEquals(improved[1], replacedPoint)
//        assertNotEquals(improved[2], replacedPoint)
//        assertNotEquals(improved[3], replacedPoint)
//
//    }
//
//    @Test
//    fun replaceSelectedWillNotMoveTheFinish() {
//        val improved = improver.replaceSelectedNumberedControls(listOf(1, 2, 3), controls)
//
//        assertNotEquals(improved[0], replacedPoint)
//        assertEquals(improved[1], replacedPoint)
//        assertEquals(improved[2], replacedPoint)
//        assertNotEquals(improved[3], replacedPoint)
//
//    }
}