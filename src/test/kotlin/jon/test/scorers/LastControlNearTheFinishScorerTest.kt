package jon.test.scorers

import com.graphhopper.GHResponse
import io.mockk.classMockk
import io.mockk.every
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class LastControlNearTheFinishScorerTest {

    lateinit var rsStartTo1: GHResponse
    lateinit var rs1To2: GHResponse
    lateinit var rs2ToFinish: GHResponse
    lateinit var cr: GHResponse

    @BeforeAll
    fun beforeTests() {
        rsStartTo1 = classMockk(GHResponse::class)
        rs1To2 = classMockk(GHResponse::class)
        rs2ToFinish = classMockk(GHResponse::class)
        cr = classMockk(GHResponse::class)
    }


    @Test
    fun lastLegTooLong() {
        every { rsStartTo1.best.distance } returns 100.0
        every { rs1To2.best.distance } returns 100.0
        every { rs2ToFinish.best.distance } returns 200.0
        every { cr.best.distance } returns 400.0

        val scorer = LastControlNearTheFinishScorer()
        val scores = scorer.score(listOf(rsStartTo1, rs1To2, rs2ToFinish), cr)

        assertEquals(listOf(0.0, 1.0), scores) // 3 legs = 2 numbered controls
    }

    @Test
    fun lastLegTooShort() {
        every { rsStartTo1.best.distance } returns 200.0
        every { rs1To2.best.distance } returns 200.0
        every { rs2ToFinish.best.distance } returns 10.0
        every { cr.best.distance } returns 410.0

        val scorer = LastControlNearTheFinishScorer()
        val scores = scorer.score(listOf(rsStartTo1, rs1To2, rs2ToFinish), cr)

        assertEquals(listOf(0.0, 1.0), scores) // 3 legs = 2 numbered controls
    }

    @Test
    fun lastLegOk() {
        every { rsStartTo1.best.distance } returns 200.0
        every { rs1To2.best.distance } returns 200.0
        every { rs2ToFinish.best.distance } returns 60.0
        every { cr.best.distance } returns 460.0

        val scorer = LastControlNearTheFinishScorer()
        val scores = scorer.score(listOf(rsStartTo1, rs1To2, rs2ToFinish), cr)

        assertEquals(listOf(0.0, 0.0), scores)
    }
}