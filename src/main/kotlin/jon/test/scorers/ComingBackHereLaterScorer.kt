package jon.test.scorers

import com.graphhopper.GHResponse
import com.graphhopper.PathWrapper
import com.graphhopper.util.shapes.GHPoint
import com.graphhopper.util.shapes.GHPoint3D
import jon.test.improvers.dist

class ComingBackHereLaterScorer : FeatureScorer {

    /**
     * works out if we run through a future control on this leg
     * and scores it badly if we do
     */
    override fun score(routedLegs: List<GHResponse>, routedCourse: PathWrapper): List<Double> {
        val start = routedCourse.points.first()
        val finish = routedCourse.points.last()
        return listOf(0.0) + routedLegs.drop(1).mapIndexed { idx, leg -> evaluate(routedLegs.subList(idx + 2, routedLegs.size), leg, start, finish) }
    }

    private fun evaluate(futureLegs: List<GHResponse>, thisLeg: GHResponse, start: GHPoint, finish: GHPoint): Double {
        return when {
            futureLegs.isEmpty() -> 0.0 // no further legs
            else -> {
                val remainingControls = futureLegs.map { it.best.points.last() } + start + finish // don't go back through the start
                when {
                    thisLeg.best.points.any { goesTooCloseToAFutureControl(remainingControls, it) } -> 1.0
                    else -> 0.0
                }
            }
        }
    }

    private fun goesTooCloseToAFutureControl(ctrls: List<GHPoint>, p: GHPoint3D) =
            ctrls.any { c -> dist(p, c) < 50.0 }
}