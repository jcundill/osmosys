package jon.test

import com.graphhopper.GHRequest
import com.graphhopper.util.shapes.GHPoint
import com.vividsolutions.jts.geom.Envelope
import jon.test.annealing.InfeasibleProblemException
import jon.test.annealing.Problem
import jon.test.constraints.CourseConstraint
import jon.test.mapping.MapFitter
import jon.test.improvers.dist2d


class CourseFinder(
        private val csf: ControlSiteFinder,
        private val constraints: List<CourseConstraint>,
        private val scorer: CourseScorer,
        private val params: CourseParameters) : Problem<CourseImprover> {

    var bad = 0

    override fun initialState(): CourseImprover = CourseImprover(csf, chooseInitialPoints(params.start, params.finish, params.initialPoints))

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

    fun chooseInitialPoints(start: GHPoint, finish: GHPoint, initialPoints: List<GHPoint>): List<GHPoint> {
        val startPoint = csf.findNearestControlSiteTo(start)
        val finishPoint = csf.findNearestControlSiteTo(finish)
        when {
            startPoint == null -> throw InfeasibleProblemException("no control point near the start")
            finishPoint == null -> throw InfeasibleProblemException("no control point near the finish")
            else -> {
                val env = Envelope()
                env.expandToInclude(startPoint.lon, startPoint.lat)
                env.expandToInclude(finishPoint.lon, finishPoint.lat)
                val chosenControls = initialPoints.map{csf.findControlSiteNear(it, 100.0)}
                chosenControls.forEach {
                    env.expandToInclude(it.lon, it.lat)
                }
                val initialControls: List<GHPoint> = if (!canBeMapped(env)) {
                    throw InfeasibleProblemException("start is too far away from finish to be mapped")
                } else {
                    val fudgeFactor = 5.0 / params.numControls
                    val radius = fudgeFactor * params.distance / (2 * Math.PI)

                    val bearing = csf.randomBearing
                    val angle = (2 * Math.PI) / params.numControls

                    val envCentre = GHPoint(env.centre().y, env.centre().x)
                    val circleCentre = csf.getCoords(envCentre, Math.PI + bearing, radius)

                    // if the env is really small (only a start perhaps)
                    // treat it as being on the radius of the circle
                    // otherwise build an initial circle around its centre
                    val w = dist2d.calcDist(env.minY, env.minX, env.maxY, env.maxX)
                    val circleCentre = when {
                        w < 1000 -> csf.getCoords(envCentre,  Math.PI + bearing, radius)
                        else -> envCentre
                    }

                    val positions = (1 .. (params.numControls - initialPoints.size)).map { num ->
                        csf.getCoords(circleCentre, (num * angle) + bearing, radius)
                    }
                    positions.map { csf.findControlSiteNear(it, radius / 5.0) }
                }
                return listOf(startPoint) + initialControls + chosenControls + finishPoint
            }
        }
    }

    private fun canBeMapped(env: Envelope) =
            MapFitter().getForEnvelope(env) != null


}
