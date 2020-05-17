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

package org.osmosys.scorers

import com.graphhopper.GHResponse
import com.graphhopper.PathWrapper
import io.mockk.every
import io.mockk.mockkClass
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.test.BeforeTest

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class DidntMoveScorerTest {

    lateinit var rsStartTo1: GHResponse
    lateinit var rs1To2: GHResponse
    lateinit var rs2ToFinish: GHResponse
    lateinit var cr: PathWrapper

    @BeforeTest
    fun beforeTests() {
        rsStartTo1 = mockkClass(GHResponse::class)
        rs1To2 = mockkClass(GHResponse::class)
        rs2ToFinish = mockkClass(GHResponse::class)
        cr = mockkClass(PathWrapper::class)
    }

    @Test
    fun scoreMoved() {
        val scorer = DidntMoveScorer()

        every { rsStartTo1.best.points.getLat(0) } returns 53.0
        every { rsStartTo1.best.points.getLon(0) } returns -1.0
        every { rs1To2.best.points.getLat(0) } returns 54.0
        every { rs1To2.best.points.getLon(0) } returns -1.0
        every { rs2ToFinish.best.points.getLat(0) } returns 54.0
        every { rs2ToFinish.best.points.getLon(0) } returns -2.0

        val scores = scorer.score(listOf(rsStartTo1, rs1To2, rs2ToFinish))


        assertEquals(0.0, scores[0])
        assertEquals(0.0, scores[1])
        assertEquals(0.0, scores[2])

        assertEquals(3, scores.size) // 3 legs = 3 scores
    }
    @Test
    fun scoreDidntMove() {
        val scorer = DidntMoveScorer()

        every { rsStartTo1.best.points.getLat(0) } returns 53.0
        every { rsStartTo1.best.points.getLon(0) } returns -1.0
        every { rs1To2.best.points.getLat(0) } returns 54.0
        every { rs1To2.best.points.getLon(0) } returns -1.0
        every { rs2ToFinish.best.points.getLat(0) } returns 54.0
        every { rs2ToFinish.best.points.getLon(0) } returns -1.0

        val scores = scorer.score(listOf(rsStartTo1, rs1To2, rs2ToFinish))


        assertEquals(0.0, scores[0])
        assertEquals(0.0, scores[1])
        assertEquals(1.0, scores[2])
    }

    @Test
    fun scoreDidntMoveAtAll() {
        val scorer = DidntMoveScorer()

        every { rsStartTo1.best.points.getLat(0) } returns 53.0
        every { rsStartTo1.best.points.getLon(0) } returns -1.0
        every { rs1To2.best.points.getLat(0) } returns 53.0
        every { rs1To2.best.points.getLon(0) } returns -1.0
        every { rs2ToFinish.best.points.getLat(0) } returns 53.0
        every { rs2ToFinish.best.points.getLon(0) } returns -1.0

        val scores = scorer.score(listOf(rsStartTo1, rs1To2, rs2ToFinish))


        assertEquals(0.0, scores[0])
        assertEquals(1.0, scores[1])
        assertEquals(1.0, scores[2])
    }
}
