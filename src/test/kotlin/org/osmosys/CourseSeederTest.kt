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
import com.graphhopper.PathWrapper
import com.graphhopper.util.shapes.GHPoint
import io.mockk.every
import io.mockk.mockkClass
import org.osmosys.annealing.InfeasibleProblemException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class CourseSeederTest {

    private lateinit var csf: ControlSiteFinder
    private lateinit var seeder: CourseSeeder
    private lateinit var mockResponse: GHResponse


    @BeforeTest
    fun beforeTests() {
        mockResponse = mockkClass(GHResponse::class)
        csf = mockkClass(ControlSiteFinder::class)
        every { csf.routeRequest(any()) } returns mockResponse
        seeder = CourseSeeder(csf)
    }

//    @Test
//    fun chooseInitialPoints() {
//        val dummyPoint = GHPoint(12.0, 12.0)
//        val start = GHPoint(52.988304, -1.203265)
//
//        every { csf.randomBearing } returns 0.05
//        every { csf.getCoords(any(), any(), any()) } returns dummyPoint
//        every { csf.findControlSiteNear(any(), any()) } returns dummyPoint
//        every { csf.findNearestControlSiteTo(start) } returns start
//        val pw = PathWrapper()
//
//        every { mockResponse.best } returns
//
//
//        val points = seeder.chooseInitialPoints(listOf(start, start), 10, 7000.0)
//        assertEquals(12, points.size)
//        assertEquals(listOf(start) + List(10) { dummyPoint } + start, points)
//    }

    @Test
    fun startTooFarFromFinish() {
        val dummyPoint = GHPoint(12.0, 12.0)
        val start = GHPoint(53.223482, -1.461064)
        val finish = GHPoint(51.511287, -0.113695)

        every { csf.randomBearing } returns 0.05
        every { csf.findNearestControlSiteTo(start) } returns start
        every { csf.findNearestControlSiteTo(finish) } returns finish
        every { csf.findControlSiteNear(any(), any()) } returns dummyPoint
        every { csf.getCoords(any(), any(), any()) } returns dummyPoint

        assertFailsWith(InfeasibleProblemException::class, "initial course cannot be mapped") {
            seeder.chooseInitialPoints(listOf(start, finish), 10, 7000.0)
        }
    }

//    @Test
//    fun chooseInitialPointsWithWayPoint() {
//        val dummyPoint = GHPoint(12.0, 12.0)
//        val start = GHPoint(52.988304, -1.203265)
//        val wpt1 = GHPoint(52.988704, -1.203265)
//
//        every { csf.randomBearing } returns 0.05
//        every { csf.getCoords(any(), any(), any()) } returns dummyPoint
//        every { csf.findControlSiteNear(any(), any()) } returns dummyPoint
//        every { csf.findNearestControlSiteTo(start) } returns start
//        every { csf.findNearestControlSiteTo(wpt1) } returns wpt1
//
//        val points = seeder.chooseInitialPoints(listOf(start, wpt1, start), 10, 7000.0)
//        assertEquals(12, points.size)
//        assertEquals(listOf(start) + List(9) { dummyPoint } + wpt1 + start, points)
//    }

    @Test
    fun chooseInitialPointsWithWayPointTooFarAway() {
        val dummyPoint = GHPoint(12.0, 12.0)
        val start = GHPoint(52.988304, -1.203265)
        val wpt1 = GHPoint(51.511287, -0.113695)

        every { csf.randomBearing } returns 0.05
        every { csf.getCoords(any(), any(), any()) } returns dummyPoint
        every { csf.findControlSiteNear(any(), any()) } returns dummyPoint
        every { csf.findNearestControlSiteTo(start) } returns start
        every { csf.findNearestControlSiteTo(wpt1) } returns wpt1

        assertFailsWith(InfeasibleProblemException::class, "initial course cannot be mapped") {
            seeder.chooseInitialPoints(listOf(start, wpt1, start), 10, 7000.0)
        }
    }

}