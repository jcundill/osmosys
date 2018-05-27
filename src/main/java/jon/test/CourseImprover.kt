package jon.test

import com.graphhopper.util.shapes.GHPoint
import xyz.thepathfinder.simulatedannealing.SearchState

class CourseImprover(private val csf: ControlSiteFinder, val points: List<GHPoint>) : SearchState<CourseImprover> {

    private val threshold = 0.25

    var legScores: List<Double>? = null

    private val pickRandomly = ControlPickingStrategies::pickRandomly
    private val pickAboveAverage = ControlPickingStrategies::pickAboveAverage

    override fun step(): CourseImprover {
        val worsts = findIndexesOfWorst()
        println(worsts)
        val ps = worsts.fold(points, {_, worst ->
            val p: GHPoint = csf.findAlternativeControlSiteFor(points[worst])
            points.subList(0, worst) + listOf(p) + points.subList(worst + 1, points.size)
        })

        return CourseImprover(csf, ps)
    }

    private fun findIndexesOfWorst(): List<Int> = when (legScores) {
        null -> pickRandomly(DoubleArray(points.size, {0.5}).toList(), points.size / 3)
        else -> {

            val legs = legScores!!
            when {
                allTheSameScore(legs) -> pickRandomly(legs, points.size / 3)
                else -> pickAboveAverage(legs, points.size / 3)
            }

        }
    }

    private fun allTheSameScore(scores: List<Double>): Boolean {
        return scores.drop(1).all { it == scores[1] } //we won't choose the start so don't check it
    }
}


