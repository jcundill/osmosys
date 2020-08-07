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
internal class LegLengthScorerTest {

    lateinit var rs1To2: GHResponse
    lateinit var rs2To3: GHResponse
    lateinit var rsStartTo1: GHResponse
    lateinit var rs3ToFinish: GHResponse
    lateinit var cr: PathWrapper

    val scorer = LegLengthScorer()
    lateinit var legScores: List<GHResponse>

    @BeforeTest
    fun beforeTests() {
        rs1To2 = mockkClass(GHResponse::class)
        rs2To3 = mockkClass(GHResponse::class)
        rsStartTo1 = mockkClass(GHResponse::class)
        rs3ToFinish = mockkClass(GHResponse::class)
        cr = mockkClass(PathWrapper::class)
        legScores = listOf(rsStartTo1, rs1To2, rs2To3, rs3ToFinish)
        every { cr.distance } returns 600.0
    }

    @Test
    fun scoreAllInBounds() {
        every { rsStartTo1.best.distance } returns 150.0
        every { rs1To2.best.distance } returns 150.0
        every { rs2To3.best.distance } returns 150.0
        every { rs3ToFinish.best.distance } returns 150.0

        val scores = scorer.score(legScores)

        assertEquals(listOf(0.0, 0.0, 0.0, 0.0), scores)
   }

    @Test
    fun score1TooCloseTo2() {
        every { rsStartTo1.best.distance } returns 150.0
        every { rs1To2.best.distance } returns 15.0
        every { rs2To3.best.distance } returns 150.0
        every { rs3ToFinish.best.distance } returns 150.0

        val scores = scorer.score(legScores)

        assertEquals(listOf(0.0, 1.0, 0.0, 0.0), scores)
    }

    @Test
    fun score1TooFarAwayFrom2() {
        every { rsStartTo1.best.distance } returns 150.0
        every { rs1To2.best.distance } returns 1500.0
        every { rs2To3.best.distance } returns 150.0
        every { rs3ToFinish.best.distance } returns 150.0

        val scores = scorer.score(legScores)

        assertEquals(listOf(0.0, 1.0, 0.0, 0.0), scores)
    }

    @Test
    fun scoreAllTooClose() {
        every { rsStartTo1.best.distance } returns 15.0
        every { rs1To2.best.distance } returns 15.0
        every { rs2To3.best.distance } returns 15.0
        every { rs3ToFinish.best.distance } returns 15.0

        val scores = scorer.score(legScores)

        assertEquals(listOf(1.0, 1.0, 1.0, 1.0), scores)
    }
}