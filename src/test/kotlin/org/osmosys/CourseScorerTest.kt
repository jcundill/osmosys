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
import com.graphhopper.util.PointList
import io.mockk.every
import io.mockk.mockkClass
import org.osmosys.scorers.LegScorer
import org.junit.jupiter.api.*
import kotlin.test.BeforeTest
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class CourseScorerTest {

    private lateinit var scorer: CourseScorer
    private lateinit var csf: ControlSiteFinder
    private lateinit var mockResponse: GHResponse
//    private lateinit var step: CourseImprover
    private lateinit var course: Course
    private lateinit var mockFS1: LegScorer
    private lateinit var mockFS2: LegScorer
    private lateinit var mockFS3: LegScorer
    private lateinit var mockRoute: GHResponse

    @BeforeTest
    fun beforeAll() {
        csf = mockkClass(ControlSiteFinder::class)
        mockResponse = mockkClass(GHResponse::class)
        every { csf.routeRequest(controls = any()) } returns mockResponse
        mockFS1 = mockkClass(LegScorer::class)
        mockFS2 = mockkClass(LegScorer::class)
        mockFS3 = mockkClass(LegScorer::class)
        mockRoute = mockkClass(GHResponse::class)
        every { mockRoute.best.distance } returns 1000.0
        every { mockResponse.hasAlternatives() } returns true
        every { mockFS1.weighting } returns 1.0
        every { mockFS2.weighting } returns 1.0
        every { mockFS3.weighting } returns 1.0

    }

    @BeforeEach
    fun setUp() {
        course = Course(controls = listOf(ControlSite(1.0, 2.0), ControlSite(1.5, 2.5), ControlSite(1.5, 2.5), ControlSite(1.5, 2.5), ControlSite(1.5, 2.5), ControlSite(1.5, 2.5)))
        course.route = mockRoute.best
//        step = CourseImprover(csf, course)
        scorer = CourseScorer(listOf(mockFS1, mockFS2, mockFS3), csf::findRoutes)
    }

    @AfterEach
    fun tearDown() {
    }

    // Constraints

//    @Test
//    fun scoreHasErrors() {
//
//        every { mockRoute.hasErrors() } returns true
//        val score = scorer.score(step, mockRoute)
//        assertEquals(10000.0, score)
//    }
//
//    @Test
//    fun scoreTooBigToMap() {
//
//        every { mockResponse.hasErrors() } returns false
//        every { mockResponse.best.points } returns PointList.EMPTY
//        every { csf.routeFitsBox(any(), any()) } returns false
//
//        val score = scorer.score(step, mockRoute)
//        assertEquals(10000.0, score)
//    }
//
//    @Test
//    fun scoreNoErrors() {
//        every { mockResponse.hasErrors() } returns false
//        every { mockResponse.best.points } returns PointList.EMPTY
//        every { csf.routeFitsBox(any(), any()) } returns true
//        every { csf.findRoutes(any(), any()) } returns mockResponse
//
//        every {mockFS1.score(any(), any())} returns course.drop(2).map { 0.0 }
//        every {mockFS2.score(any(), any())} returns course.drop(2).map { 0.0 }
//        every {mockFS3.score(any(), any())} returns course.drop(2).map { 0.0 }
//
//        val score = scorer.score(step, mockRoute)
//        assertEquals(0.0, score)
//        assertNotNull(step.numberedControlScores)
//        // there are 5 controls (inc s + f) then there are 3 numbered control scores
//        assertEquals(course.size - 2, step.numberedControlScores.size)
//        assertTrue(step.numberedControlScores.all {it == 0.0})
//    }

    @Test
    fun scoresAreCorrect() {
        every { mockResponse.hasErrors() } returns false
        every { mockResponse.best.points } returns PointList.EMPTY
        every { csf.routeFitsBox(any(), any()) } returns true
        every { csf.findRoutes(any(), any()) } returns mockResponse

        /*
                featureScores =
                        1       2       3       4       5       6
                FS1     0.1     0.2     0.1     0.1     0.5     0.0
                FS2     0.2     0.1     0.1     0.4     0.2     0.0
                FS3     0.3     0.1     0.2     0.0     0.0     0.4

                step.numberedControlScores = 0.2, 0.167, 0.167, 0.167, 0.267, 0.167
          */
        val fs1 = listOf( 0.1, 0.2, 0.1, 0.1, 0.5, 0.0)
        val fs2 = listOf(0.2, 0.1, 0.1, 0.4, 0.2, 0.0)
        val fs3 = listOf(0.3, 0.1, 0.2, 0.0, 0.0, 0.4)

        every {mockFS1.score(any())} returns  fs1
        every {mockFS2.score(any())} returns  fs2
        every {mockFS3.score(any())} returns  fs3

        val expectedNumberedControlScores = listOf(
                (fs1[0] + fs2[0] + fs3[0]) / 3,
                (fs1[1] + fs2[1] + fs3[1]) / 3,
                (fs1[2] + fs2[2] + fs3[2]) / 3,
                (fs1[3] + fs2[3] + fs3[3]) / 3,
                (fs1[4] + fs2[4] + fs3[4]) / 3,
                (fs1[5] + fs2[5] + fs3[5]) / 3
        )

        val ans = scorer.score(course)
        assertEquals(expectedNumberedControlScores, course.legScores)
        assertEquals(expectedNumberedControlScores.average(), ans)
    }


    @Test
    fun transpose() {
        val ins = listOf(listOf(1,2,3), listOf(4,5,6), listOf(7,8,9))
        val expected = listOf(listOf(1,4,7), listOf(2,5,8), listOf(3,6,9))

        val ans = scorer.transpose(ins)
        assertEquals(expected, ans)
    }

    @Test
    fun transpose1() {
        val ins = listOf(listOf(1,2,3))
        val expected = listOf(listOf(1), listOf(2), listOf(3))

        val ans = scorer.transpose(ins)
        assertEquals(expected, ans)
    }

    @Test
    fun transpose0() {
        val ins: List<List<Int>> = listOf(emptyList())

        val ans: List<List<Int>> = scorer.transpose(ins)
        assertEquals(0, ans.size)
    }
}