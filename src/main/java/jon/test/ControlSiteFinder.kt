package jon.test

import com.graphhopper.GHRequest
import com.graphhopper.GHResponse
import com.graphhopper.GraphHopper
import com.graphhopper.routing.util.DefaultEdgeFilter
import com.graphhopper.storage.index.QueryResult
import com.graphhopper.util.Parameters
import com.graphhopper.util.shapes.BBox
import com.graphhopper.util.shapes.GHPoint
import java.util.*


/**
 * Created by jcundill on 18/01/2017.
 */
class ControlSiteFinder(val gh: GraphHopper) {

    private val filter = DefaultEdgeFilter(gh.encodingManager.getEncoder("streeto"))
    private val rnd = Random(System.currentTimeMillis())
    private val bbox: BBox = gh.graphHopperStorage.bounds


    fun findControlSiteNear(point: GHPoint, distance: Double = 100.0): GHPoint {
        var node: Pair<Double, Double>? = findControlSiteNearInternal(getCoords(point, randomBearing, distance))
        while (node == null) node = findControlSiteNearInternal(getCoords(point, randomBearing, distance + ((rnd.nextDouble() - 0.5) * distance)))
        return GHPoint(node?.first, node?.second)
    }

    fun routeRequest(req: GHRequest, numAlternatives: Int = 0): GHResponse {
        req.weighting = "shortest"
        if (numAlternatives > 1) {
            req.algorithm = Parameters.Algorithms.ALT_ROUTE
            req.hints.put(Parameters.Algorithms.AltRoute.MAX_SHARE, 0.5)
        }

        return gh.route(req)

    }


    private fun findControlSiteNearInternal(p: GHPoint): Pair<Double, Double>? {
        val qr = gh.locationIndex.findClosest(p.lat, p.lon, filter)
        return when {
            !qr.isValid -> null
            qr.snappedPosition != QueryResult.Position.TOWER -> null
            else -> {
                //qr.calcSnappedPoint(DistanceCalc())
                val point = qr.snappedPoint
                Pair(point.lat, point.lon)
            }
        }
    }

    private fun getCoords(loc: GHPoint, bearing: Double, dist: Double): GHPoint {
        val R = 6378.1 * 1000//Radius of the Earth

        val lat1 = Math.toRadians(loc.lat)
        val lon1 = Math.toRadians(loc.lon)

        val lat2 = Math.asin(Math.sin(lat1) * Math.cos(dist / R) +
                Math.cos(lat1) * Math.sin(dist / R) * Math.cos(bearing))

        val lon2 = lon1 + Math.atan2(Math.sin(bearing) * Math.sin(dist / R) * Math.cos(lat1),
                Math.cos(dist / R) - Math.sin(lat1) * Math.sin(lat2))

        return GHPoint(Math.toDegrees(lat2), Math.toDegrees(lon2))
    }

    private val randomBearing: Double
        get() = 2 * Math.PI * rnd.nextDouble()

}
