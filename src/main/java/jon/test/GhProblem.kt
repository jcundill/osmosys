package jon.test

import com.graphhopper.GHResponse
import com.graphhopper.util.shapes.GHPoint
import xyz.thepathfinder.simulatedannealing.Problem

data class Params(val distance: Double = 6000.0, val points: Int = 6, val start: GHPoint)

class GhProblem(private val csf: ControlSiteFinder, private val params: Params ) : Problem<GhStep> {

    private val cache = HashMap<Int, Double>()

    override fun initialState(): GhStep {
        val near = csf.findControlSiteNear(params.start)!!
        val ps = chooseInitialPoints(near)

        return GhStep(csf, ps)
    }

    override fun energy(step: GhStep?): Double {
         return when {
            step === null -> 1000.0
             step.response === null -> 1000.0
            step.response!!.hasErrors() -> 1000.0
            else -> {
                when {
                    cache.containsKey(step.hashCode()) -> cache[step.hashCode()]!!
                    else -> {
                        val ans = score(step.response!!)
                        cache[step.hashCode()] = ans
                        ans
                    }
                }
            }
        }
    }

    private fun score(response: GHResponse): Double {
        val points = response.best.points
        val distinct = points.distinct()
        return (points.size.toDouble() - distinct.size.toDouble()) + Math.abs(response.best.distance - params.distance) / 500.0
    }

    private fun chooseInitialPoints(near: GHPoint): List<GHPoint> {
        var ps = listOf(near)

        (1..(params.points - 2)).forEach {
            val rnd = csf.getRandomLocationAtDistance(ps.last(), 500.0)
            if (rnd != null) ps += rnd.best.points.last()
        }

        ps += near

        return ps
    }


}
