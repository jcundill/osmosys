package jon.test

import com.graphhopper.GHRequest
import jon.test.scorers.*

class CourseScorer(private val csf: ControlSiteFinder, private val featureScorers: List<FeatureScorer>, private val params: CourseParameters) {

    fun score(step: CourseImprover): Double {
        // route the whole course
        val courseRoute = csf.routeRequest(GHRequest(step.controls))
        return when {
            courseRoute.hasErrors() -> 10000.0
            !csf.routeFitsBox(courseRoute.best.points, params.allowedBoxes) -> 10000.0
            else -> {
                // score all the legs individually
                // needed currently for alternative routes
                val legs = step.controls.windowed(2, 1, false)
                val legRoutes = legs.map{ ab -> csf.findRoutes(ab.first(), ab.last())}
                val scores: List<List<Double>> = featureScorers.map { it.score(legRoutes, courseRoute) }

                val featureScores = scores.map {it.average()} // don't include the start when seeing how this feature scored

                val avs = legs.mapIndexed { idx, _ ->
                    scores.fold(0.0, {acc, s -> acc + s[idx]})/ featureScorers.size.toDouble()
                }
                step.legScores = avs
                step.featureScores = featureScores
                return avs.fold(0.0, {acc:Double, s:Double -> acc + s}) / avs.size
            }
        }
    }
}