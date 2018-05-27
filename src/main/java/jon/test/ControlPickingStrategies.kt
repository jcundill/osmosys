package jon.test


object ControlPickingStrategies {
    fun pickRandomly(legScores: List<Double>, num: Int): List<Int> {

        val choices = (1..(legScores.size - 1)).toList()
        return when {
            num >= legScores.size -> choices
            else -> choices.shuffled().take(num)
        }
    }

    fun pickAboveAverage(legScores: List<Double>, num: Int): List<Int> {

        val mean = legScores.average()
        val badIndexes = legScores.drop(1).zip((1..(legScores.size - 1))).filter { it.first > mean }.map { it.second }

        return when {
            badIndexes.size <= num -> badIndexes
            else -> badIndexes.shuffled().take(num)
        }
    }
}