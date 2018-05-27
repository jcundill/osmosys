package jon.test

import com.graphhopper.GHResponse
import com.graphhopper.util.shapes.GHPoint
import io.mockk.classMockk
import io.mockk.every
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.test.assertNotNull

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class CourseImproverTest {

    lateinit var csf: ControlSiteFinder
    lateinit var improver: CourseImprover
    lateinit var mockRoutingResponse: GHResponse
    lateinit var controls: List<GHPoint>

    @BeforeAll
    fun beforeTests() {
        csf = classMockk(ControlSiteFinder::class)
        mockRoutingResponse = classMockk(GHResponse::class)
        every { csf.routeRequest(any(), any()) } returns mockRoutingResponse
        every { csf.findAlternativeControlSiteFor(any()) } returns GHPoint()
    }

    @BeforeEach
    fun setUp() {
        controls = listOf(GHPoint(1.0, 2.0), GHPoint(1.5, 2.5), GHPoint(1.5, 2.5), GHPoint(1.5, 2.5))
        improver = CourseImprover(csf, controls )
    }

    @Test
    fun ifNoLegScoresChooseRandom() {
        val improved = improver.step()
        assertNotNull(improved)
    }
}