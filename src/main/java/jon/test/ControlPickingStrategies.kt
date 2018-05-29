package jon.test


/**
 * here we are dealing with the numbered controls so should answer in the range 1 to the last control.
 * i.e. if there are 10 point on the course - start and finish and 8 controls
 * the we are given a list of 8
 *
 * if we don't like the first one in the list and we only have to choose 1 then we should return 1 rather than 0
 */
object ControlPickingStrategies {

    fun pickRandomly(legScores: List<Double>, num: Int): List<Int> =
            pick(legScores, num, {_ -> true})

    fun pickAboveAverage(legScores: List<Double>, num: Int): List<Int> =
            pick(legScores, num, { x -> x.second > legScores.average() })

    fun pick(legScores: List<Double>, num: Int, selector: (Pair<Int, Double>) -> Boolean): List<Int> {
        val indexedScores = (1..(legScores.size)).zip(legScores)
        val choices = indexedScores.filter { selector(it) }.sortedByDescending { it.second }
        val selectedIndexes = choices.map { it.first }

        return when {
            selectedIndexes.size <= num -> selectedIndexes
            else -> selectedIndexes.take(num)
        }
    }
}