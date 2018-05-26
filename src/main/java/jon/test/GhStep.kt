package jon.test

import com.graphhopper.GHRequest
import com.graphhopper.GHResponse
import com.graphhopper.util.shapes.GHPoint
import xyz.thepathfinder.simulatedannealing.SearchState

class GhStep(private val csf: ControlSiteFinder, val points: List<GHPoint>) : SearchState<GhStep> {

    private var _response: GHResponse? = null
    val response: GHResponse?
        get() {
            if (_response == null) {
                _response = csf.routeRequest(GHRequest(points))
            }
            return _response
        }


    override fun step(): GhStep {
        val worst = findIndexOfWorst()
        val p: GHPoint = csf.findControlSiteNear(points[worst], Math.random() * 500.0)
        val ps: List<GHPoint> = points.subList(0, worst) + listOf(p) + points.subList(worst + 1, points.size)


        return GhStep(csf, ps)
    }

    private fun findIndexOfWorst() = (1 + Math.random() * (points.size - 2)).toInt()
}

