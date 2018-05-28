package jon.test

import com.graphhopper.GHRequest
import jon.test.scorers.*

class CourseScorer(private val csf: ControlSiteFinder, private val featureScorers: List<FeatureScorer>, private val params: CourseParameters) {

    fun score(step: CourseImprover): Double {
        val response = csf.routeRequest(GHRequest(step.controls))
        return when {
            response.hasErrors() -> 10000.0
            !csf.routeFitsBox(response.best.points, params.allowedBoxes) -> 10000.0
            else -> {
                val legs = step.controls.windowed(2, 1, false)
                val routes = legs.map{ ab -> csf.findRoutes(ab.first(), ab.last())}
                val scores = featureScorers.map { it.score(routes, response) }

                val avs = legs.mapIndexed { idx, _ ->
                    scores.fold(0.0, {acc, s -> acc + s[idx]})/ featureScorers.size.toDouble()
                }
                step.legScores = avs
                return avs.fold(0.0, {acc:Double, s:Double -> acc + s}) / avs.size
            }
        }
    }
}