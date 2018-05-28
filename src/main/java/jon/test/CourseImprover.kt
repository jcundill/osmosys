package jon.test

import com.graphhopper.util.shapes.GHPoint
import xyz.thepathfinder.simulatedannealing.SearchState

class CourseImprover(private val csf: ControlSiteFinder, val controls: List<GHPoint>) : SearchState<CourseImprover> {

    private val noChoicePicker = ControlPickingStrategies::pickRandomly
    private val hasChoicePicker = ControlPickingStrategies::pickAboveAverage
    private val dummyScores get() = List(controls.size - 2, { 0.5 })

    /**
     * the improver is given the leg scores for the numbered controls only
     */
    var legScores: List<Double> = dummyScores
    var featureScores: List<Double>? = null

    override fun step(): CourseImprover =
            CourseImprover(csf, replaceSelectedControls(findIndexesOfWorst(legScores, controls.size / 3), controls))

    fun replaceSelectedControls(selected: List<Int>, existing: List<GHPoint>): List<GHPoint> =
            selected.filter {it != 0 && it != existing.size - 1}.fold(existing, { current, ctrl ->
                current.subList(0, ctrl) +
                        listOf(csf.findAlternativeControlSiteFor(current[ctrl])) +
                        current.subList(ctrl + 1, current.size)
            })

    /**
     * find some of the numbered controls that we would like to reposition
     * @param num - how many to choose to reposition
     * @param scores - the scores allocated to the numbered controls
     *
     * @return a list in the range 1 .. last numbered control of size num, or less if there aren't num to select
     */
    fun findIndexesOfWorst(scores: List<Double>, num:Int): List<Int> {
        return when {
            allTheSameScore(scores) -> noChoicePicker(scores, num)
            else -> hasChoicePicker(scores, num)

        }
    }

    /**
     * do all the numbered controls have the same score?
     */
    fun allTheSameScore(scores: List<Double>): Boolean {
        return scores.all { it == scores[0] }
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


