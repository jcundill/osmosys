package jon.test

import com.graphhopper.GHRequest
import com.graphhopper.GHResponse
import com.graphhopper.util.PointList
import com.graphhopper.util.shapes.GHPoint
import com.vividsolutions.jts.geom.Envelope
import xyz.thepathfinder.simulatedannealing.Problem

data class Params(val distance: Double = 6000.0, val points: Int = 6, val start: GHPoint) {
    val maxWidth = 0.04187945854565234 * 0.99
    val maxHeight = 0.01638736589702461 * 0.99
}

class GhProblem(private val csf: ControlSiteFinder, private val params: Params) : Problem<GhStep> {

    private val cache = HashMap<Int, Double>()
    val scorer = Scorer(csf, params)
    var hit = 0
    var miss = 0
    var bad = 0


    override fun initialState(): GhStep = GhStep(csf, chooseInitialPoints(params.start))

    override fun energy(step: GhStep?): Double {
        val e =  when {
            step === null -> 10000.0
            else -> {
                val response = csf.routeRequest(GHRequest(step.points))
                when {
                    response.hasErrors() -> 10000.0
                    !csf.routeFitsBox(response.best.points, params.maxWidth, params.maxHeight) -> 10000.0
                    else -> {
                        when {
                            cache.containsKey(step.hashCode()) -> {
                                hit++; return cache[step.hashCode()]!!
                            }
                            else -> {
                                miss++
                                val ans = scorer.score(step) * 1000
                                cache[step.hashCode()] = ans
                                println(ans)
                                ans
                            }
                        }
                    }
                }
            }
        }
        if( e > 1000.0) bad++
        return e
    }



    tailrec fun findMappableControlSite(seed: List<GHPoint> ): GHPoint{
        val possible = csf.findControlSiteNear(seed.last(), 500.0)
        val pl = seed.fold(PointList(), {acc, pt -> acc.add(pt); acc})
        pl.add(possible)
        return if (csf.routeFitsBox(pl, params.maxWidth, params.maxHeight)) possible
        else findMappableControlSite(seed)
    }

    private fun chooseInitialPoints(near: GHPoint): List<GHPoint> {

        val seq = generateSequence(listOf(near)) { it + findMappableControlSite(it) }
        return seq.take(params.points - 1).last() + near

    }


}
