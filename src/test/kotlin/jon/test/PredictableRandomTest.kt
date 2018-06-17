package jon.test

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class PredictableRandomTest {

   // @Test
    fun testSeq() {
        val rnd = PredictableRandom

        (1..10000).forEach {
            println( rnd.nextDouble())
        }
    }
}