package jon.test

import com.graphhopper.GHRequest
import jon.test.scorers.*

class Scorer(val csf: ControlSiteFinder, val params: Params) {

    private val featureScorers = listOf(
            CourseLengthScorer(params),
            LegLengthScorer(params),
            RouteChoiceScorer(params),
            LegComplexityScorer(params),
            BeenThisWayBeforeScorer(params),
            DidntMoveScorer(params)
    )

    fun score(step: GhStep): Double {
        val response = csf.routeRequest(GHRequest(step.points))
        return when {
            response.hasErrors() -> 10000.0
            !csf.routeFitsBox(response.best.points, params.maxWidth, params.maxHeight) -> 10000.0
            else -> {
                val legs = step.points.windowed(2, 1, false)
                val routes = legs.map{ ab -> csf.findRoutes(ab.first(), ab.last())}
                val scores = featureScorers.map { it.score(routes, response) }

                val avs = legs.mapIndexed { idx, _ ->
                    return  scores.fold(0.0, {acc, s -> acc + s[idx]})/ featureScorers.size.toDouble()
                }

                return avs.fold(0.0, {acc:Double, s:Double -> acc + s}) / avs.size
            }
        }
    }
}