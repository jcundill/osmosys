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
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.test.BeforeTest
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class LastControlNearTheFinishScorerTest {

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
    fun lastLegTooLong() {
        every { rsStartTo1.best.distance } returns 100.0
        every { rs1To2.best.distance } returns 100.0
        every { rs2ToFinish.best.distance } returns 200.0
        every { cr.distance } returns 400.0

        val scorer = LastControlNearTheFinishScorer()
        val scores = scorer.score(listOf(rsStartTo1, rs1To2, rs2ToFinish))

        assertEquals(listOf(0.0, 0.0, 1.0), scores) // 3 legs
    }

    @Test
    fun lastLegTooShort() {
        every { rsStartTo1.best.distance } returns 200.0
        every { rs1To2.best.distance } returns 200.0
        every { rs2ToFinish.best.distance } returns 10.0
        every { cr.distance } returns 410.0

        val scorer = LastControlNearTheFinishScorer()
        val scores = scorer.score(listOf(rsStartTo1, rs1To2, rs2ToFinish))

        assertEquals(listOf(0.0, 0.0, 1.0), scores) // 3 legs = 2 numbered controls
    }

    @Test
    fun lastLegOk() {
        every { rsStartTo1.best.distance } returns 200.0
        every { rs1To2.best.distance } returns 200.0
        every { rs2ToFinish.best.distance } returns 60.0
        every { cr.distance } returns 460.0

        val scorer = LastControlNearTheFinishScorer()
        val scores = scorer.score(listOf(rsStartTo1, rs1To2, rs2ToFinish))

        assertEquals(listOf(0.0, 0.0, 0.0), scores)
    }
}