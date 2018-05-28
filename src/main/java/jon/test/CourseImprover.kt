package jon.test

import com.graphhopper.util.shapes.GHPoint
import xyz.thepathfinder.simulatedannealing.SearchState

class CourseImprover(private val csf: ControlSiteFinder, val controls: List<GHPoint>) : SearchState<CourseImprover> {

    private val noChoicePicker = ControlPickingStrategies::pickRandomly
    private val hasChoicePicker = ControlPickingStrategies::pickAboveAverage
    private val dummyScores get() = DoubleArray(controls.size, { 0.5 }).toList()

    var legScores: List<Double> = dummyScores
    var featureScores: List<Double>? = null

    override fun step(): CourseImprover =
            CourseImprover(csf, replaceSelectedControls(findIndexesOfWorst(legScores, controls.size / 3), controls))

    fun replaceSelectedControls(selected: List<Int>, existing: List<GHPoint>): List<GHPoint> =
            selected.fold(existing, { current, ctrl ->
                current.subList(0, ctrl) +
                        listOf(csf.findAlternativeControlSiteFor(current[ctrl])) +
                        current.subList(ctrl + 1, current.size)
            })

    fun findIndexesOfWorst(scores: List<Double>, num:Int): List<Int> {
        return when {
            allTheSameScore(scores) -> noChoicePicker(scores, num)
            else -> hasChoicePicker(scores, num)

        }
    }

    fun allTheSameScore(scores: List<Double>): Boolean {
        return scores.drop(1).all { it == scores[1] } //we won't choose the start so don't check it
    }

    override fun equals(other: Any?): Boolean {
        return when(other) {
            is CourseImprover -> other.controls == this.controls
            else -> false
        }
    }

    override fun hashCode(): Int {
        return 31 * controls.hashCode()
    }
}


