package jon.test

import com.graphhopper.GHResponse
import com.graphhopper.util.PointList
import com.graphhopper.util.shapes.GHPoint
import com.vividsolutions.jts.geom.Envelope
import xyz.thepathfinder.simulatedannealing.Problem

data class Params(val distance: Double = 6000.0, val points: Int = 6, val start: GHPoint) {
    val maxWidth = 0.04187945854565234 * 0.9
    val maxHeight = 0.01638736589702461 * 0.9
}

class GhProblem(private val csf: ControlSiteFinder, private val params: Params) : Problem<GhStep> {

    private val cache = HashMap<Int, Double>()
    val env = Envelope()
    var hit = 0
    var miss = 0


    override fun initialState(): GhStep {
        val near = csf.findControlSiteNear(params.start)
        val ps = chooseInitialPoints(near)

        return GhStep(csf, ps)
    }

    override fun energy(step: GhStep?): Double {
        return when {
            step === null -> 10000.0
            step.response === null -> 10000.0
            step.response!!.hasErrors() -> 10000.0
            !routeFitsOnMap(step.response!!.best.points) -> 10000.0
            else -> {
                when {
                    cache.containsKey(step.hashCode()) -> {hit++; return cache[step.hashCode()]!!}
                    else -> {
                        miss++
                        val ans = score(step.response!!)
                        cache[step.hashCode()] = ans
                        ans
                    }
                }
            }
        }
    }

    private fun routeFitsOnMap(points: PointList): Boolean {
        env.setToNull()
        points.forEach { env.expandToInclude(it.lon, it.lat) }
        return env.width < params.maxWidth && env.height < params.maxHeight
    }

    private fun score(response: GHResponse): Double {
        val points = response.best.points
        val distinct = points.distinct()
        return (points.size.toDouble() - distinct.size.toDouble()) + Math.abs(response.best.distance - params.distance) / 500.0
    }

    private fun chooseInitialPoints(near: GHPoint): List<GHPoint> {
        var ps = listOf(near)

        (1..(params.points - 2)).forEach {
            val rnd = csf.findControlSiteNear(ps.last(), 500.0)
            ps += rnd
        }

        ps += near

        return ps
    }


}
