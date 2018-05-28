package jon.test


/**
 * here we are dealing with the numbered controls so should answer in the range 1 to the last control.
 * i.e. if there are 10 point on the course - start and finish and 8 controls
 * the we are given a list of 8
 *
 * if we don't like the first one in the list and we only have to choose 1 then we should return 1 rather than 0
 */
object ControlPickingStrategies {
    fun pickRandomly(legScores: List<Double>, num: Int): List<Int> {

        val choices = (1..(legScores.size)).toList()
        return when {
            num >= legScores.size -> choices
            else -> choices.shuffled().take(num)
        }
    }

    fun pickAboveAverage(legScores: List<Double>, num: Int): List<Int> {

        val mean = legScores.average()
        val choices = legScores.zip((1..(legScores.size))).filter { it.first > mean }.map { it.second }

        return when {
            choices.size <= num -> choices
            else -> choices.shuffled().take(num)
        }
    }
}