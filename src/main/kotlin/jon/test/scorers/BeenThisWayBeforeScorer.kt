package jon.test.scorers

import com.graphhopper.GHResponse
import jon.test.CourseParameters
import kotlin.math.min

data class BeenThisWayBeforeScorer(val params: CourseParameters) : FeatureScorer {

    /**
     * works out how much of the leg to this numbered control has been travelled along
     * already.
     * Find the worst duplication in any of the proceeding legs and return that as the score
     * Do not include previous leg - that is captured in the Dog Leg Scorer
     */
    override fun score(routedLegs: List<GHResponse>, routedCourse: GHResponse): List<Double> =
            routedLegs.mapIndexed { idx, leg ->  evaluate(routedLegs.subList(0, idx), leg)}

    private fun evaluate(previousLegs: List<GHResponse>, thisLeg: GHResponse): Double {
        return when {
            previousLegs.size < 2 -> 0.0 // no legs other than the previous
            else -> {
                previousLegs.dropLast(1).map{ l -> compareLegs(l, thisLeg)}.max()!!
            }
        }
    }

    private fun compareLegs(a: GHResponse, b: GHResponse): Double {
        val pointsA = a.best.points.drop(1).dropLast(1)
        val pointsB = b.best.points.drop(1).dropLast(1)
        return pointsB.intersect(pointsA).size.toDouble() / min(pointsB.size.toDouble(), pointsA.size.toDouble())
    }
}
