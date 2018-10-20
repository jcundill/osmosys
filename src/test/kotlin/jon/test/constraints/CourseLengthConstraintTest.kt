package jon.test.constraints

import com.graphhopper.GHResponse
import com.graphhopper.util.shapes.GHPoint
import io.mockk.classMockk
import io.mockk.every
import jon.test.Course
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
        val params = Course(controls = listOf(GHPoint(1.0, 7.0)), requestedDistance = 20.0)
        val constraint = CourseLengthConstraint(params.requestedDistance)

        every { cr.best.distance } returns 20.0
        assertTrue(constraint.valid(cr) )

    }

    @Test
    fun tooShort() {
        val params = Course(controls = listOf(GHPoint(1.0, 7.0)), requestedDistance = 2000.0)
        val constraint = CourseLengthConstraint(params.requestedDistance)

        every { cr.best.distance } returns 20.0
        assertFalse(constraint.valid(cr) )

    }

    @Test
    fun tooLong() {
        val params = Course(controls = listOf(GHPoint(1.0, 7.0)), requestedDistance = 20.0)
        val constraint = CourseLengthConstraint(params.requestedDistance)

        every { cr.best.distance } returns 2000.0
        assertFalse(constraint.valid(cr) )

    }

    @Test
    fun inTolerance() {
        val params = Course(controls = listOf(GHPoint(1.0, 7.0)), requestedDistance = 20.0)
        val constraint = CourseLengthConstraint(params.requestedDistance)

        every { cr.best.distance } returns params.requestedDistance + 0.2 * params.requestedDistance
        assertTrue(constraint.valid(cr) )

    }

    @Test
    fun inTolerance2() {
        val params = Course(controls = listOf(GHPoint(1.0, 7.0)), requestedDistance = 20.0)
        val constraint = CourseLengthConstraint(params.requestedDistance)

        every { cr.best.distance } returns params.requestedDistance - 0.2 * params.requestedDistance
        assertTrue(constraint.valid(cr) )

    }

}