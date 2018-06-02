package jon.test

import java.util.*


/**
 * here we are dealing with the numbered controls so should answer in the range 1 to the last control.
 * i.e. if there are 10 point on the course - start and finish and 8 controls
 * the we are given a list of 8
 *
 * if we don't like the first one in the list and we only have to choose 1 then we should return 1 rather than 0
 */
object ControlPickingStrategies {

    val rnd = Random()

    fun pickRandomly(numberedControlScores: List<Double>, num: Int): List<Int> =
            pick(numberedControlScores, num, { _ -> true})

    fun pickWeightedRandom(numberedControlScores: List<Double>, num: Int): List<Int> {
        val prob = rnd.nextDouble() / numberedControlScores.size
        val selector =  {pair:Pair<Int, Double> -> pair.second > prob}
        return pick(numberedControlScores, 100, selector)
   }

    fun pickAboveAverage(numberedControlScores: List<Double>, num: Int): List<Int> =
            pick(numberedControlScores, 100, { x -> x.second > numberedControlScores.average() })

    fun pick(numberedControlScores: List<Double>, num: Int, selector: (Pair<Int, Double>) -> Boolean): List<Int> {
        val indexedControlScores = (1..(numberedControlScores.size)).zip(numberedControlScores)
        val choices = indexedControlScores.filter { selector(it) }.sortedByDescending { it.second }
        val selectedIndexes = choices.map { it.first }

        return when {
            selectedIndexes.size <= num -> selectedIndexes
            else -> selectedIndexes.take(num)
        }
    }
}