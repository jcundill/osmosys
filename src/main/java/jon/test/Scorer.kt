package jon.test

import com.graphhopper.GHRequest
import com.graphhopper.GHResponse
import com.graphhopper.util.shapes.GHPoint
import jon.test.scorers.*

class Scorer(val csf: ControlSiteFinder, val params: Params) {

    private val courseLengthScorer = CourseLengthScorer(params)
    private val legLengthScorer = LegLengthScorer(params)
    private val routeChoiceScorer = RouteChoiceScorer(params)
    private val legComplexityScorer = LegComplexityScorer(params)
    private val beenThisWayBeforeScorer = BeenThisWayBeforeScorer(params)
    private val didntMoveScorer = DidntMoveScorer(params)

    fun score(step: GhStep): Double {
        val response = csf.routeRequest(GHRequest(step.points))
        return when {
            response.hasErrors() -> 10000.0
            !csf.routeFitsBox(response.best.points, params.maxWidth, params.maxHeight) -> 10000.0
            else -> {
                val legs = step.points.windowed(2, 1, false)
                val routes = legs.map{ ab -> csf.findRoutes(ab.first(), ab.last())}
                val clScores  = courseLengthScorer.score(routes, response)
                val llScores  = legLengthScorer.score(routes, response)
                val rcScores  = routeChoiceScorer.score(routes, response)
                val lcScores = legComplexityScorer.score(routes, response)
                val wbScores = beenThisWayBeforeScorer.score(routes, response)
                val dmScores = didntMoveScorer.score(routes, response)

                val avs = legs.mapIndexed { idx, _ ->
                    return (clScores[idx] + llScores[idx] + rcScores[idx] + lcScores[idx] + wbScores[idx] + dmScores[idx]) / 6.0
                }

                return avs.fold(0.0, {acc:Double, s:Double -> acc + s}) / avs.size
            }
        }
    }
}