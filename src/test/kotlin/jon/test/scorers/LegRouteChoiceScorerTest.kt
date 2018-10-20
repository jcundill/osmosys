package jon.test.scorers

import com.graphhopper.GHResponse
import com.graphhopper.PathWrapper
import io.mockk.classMockk
import io.mockk.every
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class LegRouteChoiceScorerTest {

    lateinit var rsStartTo1: GHResponse
    lateinit var rs1To2: GHResponse
    lateinit var rs2To3: GHResponse
    lateinit var rs3ToFinish: GHResponse
    lateinit var cr: PathWrapper

    @BeforeAll
    fun beforeTests() {
        rsStartTo1 = classMockk(GHResponse::class)
        rs1To2 = classMockk(GHResponse::class)
        rs2To3 = classMockk(GHResponse::class)
        rs3ToFinish = classMockk(GHResponse::class)
        cr = classMockk(PathWrapper::class)
    }

    @Test
    fun scoreNoChoice() {
        val scorer = LegRouteChoiceScorer()

        every { rsStartTo1.hasAlternatives() } returns false
        every { rs1To2.hasAlternatives() } returns false
        every { rs2To3.hasAlternatives() } returns false
        every { rs3ToFinish.hasAlternatives() } returns false

        val scores = scorer.score(listOf(rsStartTo1, rs1To2, rs2To3, rs3ToFinish), cr)


        assertEquals(listOf(1.0,1.0,1.0), scores)
    }

    @Test
    fun scoreSomeWithChoice() {
        val scorer = LegRouteChoiceScorer()

        every { rsStartTo1.hasAlternatives() } returns true
        every { rs1To2.hasAlternatives() } returns false
        every { rs2To3.hasAlternatives() } returns true
        every { rs3ToFinish.hasAlternatives() } returns true

        val scores = scorer.score(listOf(rsStartTo1, rs1To2, rs2To3, rs3ToFinish), cr)


        assertEquals(listOf(0.0,1.0,0.0), scores)
    }


    @Test
    fun score() {
        val scorer = LegRouteChoiceScorer()

        every { rsStartTo1.hasAlternatives() } returns true
        every { rs3ToFinish.hasAlternatives() } returns false

        val scores = scorer.score(listOf(rsStartTo1, rs3ToFinish), cr)

        assertEquals(0.0, scores[0])
    }
}