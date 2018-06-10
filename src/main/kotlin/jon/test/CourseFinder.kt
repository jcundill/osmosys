package jon.test

import com.graphhopper.GHRequest
import com.graphhopper.util.shapes.GHPoint
import com.vividsolutions.jts.geom.Envelope
import jon.test.constraints.CourseConstraint
import xyz.thepathfinder.simulatedannealing.InfeasibleProblemException
import xyz.thepathfinder.simulatedannealing.Problem
import java.util.*

class CourseFinder(
        private val csf: ControlSiteFinder,
        private val constraints: List<CourseConstraint>,
        private val scorer:CourseScorer,
        private val params: CourseParameters) : Problem<CourseImprover> {

    var bad = 0

    override fun initialState(): CourseImprover = CourseImprover(csf, chooseInitialPoints(params.start, params.finish))

    override fun energy(step: CourseImprover?): Double {
        val score = scoreStep(step)
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
                    else -> {
                        val ans = scorer.score(step, courseRoute) * 1000
                        println(ans)
                        ans
                    }
                }
            }
        }
    }

    fun chooseInitialPoints(start: GHPoint, finish: GHPoint): List<GHPoint> {

        val env = Envelope()
        env.expandToInclude(start.lon, start.lat)
        env.expandToInclude(finish.lon, finish.lat)
        val initialControls: List<GHPoint> = if (!canBeMapped(env)) {
            throw InfeasibleProblemException("start is too far away from finish to be mapped")
        } else {
            (1..(params.numControls)).map {
                val p = findMappableControlSiteIn(env, params.boxRadius)
                when (p) {
                    null -> throw InfeasibleProblemException("cannot find sites for initial controls")
                    else -> env.expandToInclude(p.lon, p.lat)
                }
                p!! //add this chap
            }
        }
        return listOf(start) + initialControls + finish

    }

    private fun findMappableControlSiteIn(env: Envelope, radius: Double): GHPoint? {
        val locus = env.centre()
        val centre = GHPoint(locus.y, locus.x)

        return csf.findControlSiteNear(centre, (Random().nextGaussian() + 0.5) * radius)
    }

    private fun canBeMapped(env: Envelope) =
            params.allowedBoxes.any { env.width < it.maxWidth && env.height < it.maxHeight }


}
