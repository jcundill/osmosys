package jon.test.constraints

import com.graphhopper.GHResponse
import com.graphhopper.util.shapes.GHPoint
import io.mockk.classMockk
import io.mockk.every
import jon.test.CourseParameters
import org.junit.jupiter.api.BeforeAll

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class CourseLengthConstraintTest {

    lateinit var cr: GHResponse

    @BeforeAll
    fun beforeTests() {
        cr = classMockk(GHResponse::class)
    }

    @Test
    fun valid() {
        val params = CourseParameters(start = GHPoint(1.0, 7.0), distance = 20.0)
        val constraint = CourseLengthConstraint(params.minAllowedDistance, params.maxAllowedDistance)

        every { cr.best.distance } returns 20.0
        assertTrue(constraint.valid(cr) )

    }

    @Test
    fun tooShort() {
        val params = CourseParameters(start = GHPoint(1.0, 7.0), distance = 2000.0)
        val constraint = CourseLengthConstraint(params.minAllowedDistance, params.maxAllowedDistance)

        every { cr.best.distance } returns 20.0
        assertFalse(constraint.valid(cr) )

    }

    @Test
    fun tooLong() {
        val params = CourseParameters(start = GHPoint(1.0, 7.0), distance = 20.0)
        val constraint = CourseLengthConstraint(params.minAllowedDistance, params.maxAllowedDistance)

        every { cr.best.distance } returns 2000.0
        assertFalse(constraint.valid(cr) )

    }

    @Test
    fun inTolerance() {
        val params = CourseParameters(start = GHPoint(1.0, 7.0), distance = 20.0)
        val constraint = CourseLengthConstraint(params.minAllowedDistance, params.maxAllowedDistance)

        every { cr.best.distance } returns params.maxAllowedDistance
        assertTrue(constraint.valid(cr) )

    }

    @Test
    fun inTolerance2() {
        val params = CourseParameters(start = GHPoint(1.0, 7.0), distance = 20.0)
        val constraint = CourseLengthConstraint(params.minAllowedDistance, params.maxAllowedDistance)

        every { cr.best.distance } returns params.minAllowedDistance
        assertTrue(constraint.valid(cr) )

    }

}