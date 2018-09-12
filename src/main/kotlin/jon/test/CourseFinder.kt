package jon.test

import com.graphhopper.GHRequest
import com.graphhopper.util.shapes.GHPoint
import com.vividsolutions.jts.geom.Envelope
import jon.test.annealing.InfeasibleProblemException
import jon.test.annealing.Problem
import jon.test.constraints.CourseConstraint
import jon.test.mapping.MapFitter

class CourseFinder(
        private val csf: ControlSiteFinder,
        private val constraints: List<CourseConstraint>,
        private val scorer: CourseScorer,
        private val params: CourseParameters) : Problem<CourseImprover> {

    var bad = 0

    override fun initialState(): CourseImprover = CourseImprover(csf, chooseInitialPoints(params.start, params.finish))

    override fun energy(searchState: CourseImprover): Double {
        val score = scoreStep(searchState)
        if (score > 1000.0) bad++
        return score
    }

    private fun scoreStep(step: CourseImprover?): Double {
        return when {
            step === null -> 10000.0
            else -> {
                val courseRoute = csf.routeRequest(GHRequest(step.controls))
                when {
                    constraints.any { !it.valid(courseRoute) } -> 10000.0
                    else -> scorer.score(step, courseRoute) * 1000
                }
            }
        }
    }

    fun chooseInitialPoints(start: GHPoint, finish: GHPoint): List<GHPoint> {
        val startPoint = csf.findNearestControlSiteTo(start)
        val finishPoint = csf.findNearestControlSiteTo(finish)
        when {
            startPoint == null -> throw InfeasibleProblemException("no control point near the start")
            finishPoint == null -> throw InfeasibleProblemException("no control point near the finish")
            else -> {
                val env = Envelope()
                env.expandToInclude(startPoint.lon, startPoint.lat)
                env.expandToInclude(finishPoint.lon, finishPoint.lat)
                val initialControls: List<GHPoint> = if (!canBeMapped(env)) {
                    throw InfeasibleProblemException("start is too far away from finish to be mapped")
                } else {
                    val fudgeFactor = 5.0 / params.numControls
                    val radius = fudgeFactor * params.distance / (2 * Math.PI)

                    val bearing = csf.randomBearing
                    val angle = (2 * Math.PI) / params.numControls

                    val envCentre = GHPoint(env.centre().y, env.centre().x)
                    val circleCentre = csf.getCoords(envCentre, Math.PI + bearing, radius)

                    val positions = (1..params.numControls).map { num ->
                        csf.getCoords(circleCentre, (num * angle) + bearing, radius)
                    }
                    positions.map { csf.findControlSiteNear(it, radius / 5.0) }
                }
                return listOf(startPoint) + initialControls + finishPoint
            }
        }
    }

    private fun canBeMapped(env: Envelope) =
            MapFitter().getForEnvelope(env) != null


}
