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

package org.osmosys.constraints

import com.graphhopper.GHResponse
import com.graphhopper.util.shapes.GHPoint
import io.mockk.every
import io.mockk.mockkClass
import org.osmosys.Course
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.test.BeforeTest
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class CourseLengthConstraintTest {

    lateinit var cr: GHResponse

    @BeforeTest
    fun beforeTests() {
        cr = mockkClass(GHResponse::class)
    }

    @Test
    fun valid() {
        val params = Course(controls = listOf(GHPoint(1.0, 7.0)), requestedDistance = 20.0)
        val constraint = CourseLengthConstraint(params.distance())

        every { cr.best.distance } returns 20.0
        assertTrue(constraint.valid(cr) )

    }

    @Test
    fun tooShort() {
        val params = Course(controls = listOf(GHPoint(1.0, 7.0)), requestedDistance = 2000.0)
        val constraint = CourseLengthConstraint(params.distance())

        every { cr.best.distance } returns 20.0
        assertFalse(constraint.valid(cr) )

    }

    @Test
    fun tooLong() {
        val params = Course(controls = listOf(GHPoint(1.0, 7.0)), requestedDistance = 20.0)
        val constraint = CourseLengthConstraint(params.distance())

        every { cr.best.distance } returns 2000.0
        assertFalse(constraint.valid(cr) )

    }

    @Test
    fun inTolerance() {
        val params = Course(controls = listOf(GHPoint(1.0, 7.0)), requestedDistance = 20.0)
        val constraint = CourseLengthConstraint(params.distance())

        every { cr.best.distance } returns params.distance() + 0.2 * params.distance()
        assertTrue(constraint.valid(cr) )

    }

    @Test
    fun inTolerance2() {
        val params = Course(controls = listOf(GHPoint(1.0, 7.0)), requestedDistance = 20.0)
        val constraint = CourseLengthConstraint(params.distance())

        every { cr.best.distance } returns params.distance() - 0.2 * params.distance()
        assertTrue(constraint.valid(cr) )

    }

}