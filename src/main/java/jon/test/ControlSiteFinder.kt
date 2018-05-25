package jon.test

import com.graphhopper.GHRequest
import com.graphhopper.GHResponse
import com.graphhopper.GraphHopper
import com.graphhopper.routing.util.DefaultEdgeFilter
import com.graphhopper.storage.index.LocationIndex
import com.graphhopper.util.DistancePlaneProjection
import com.graphhopper.util.Parameters
import com.graphhopper.util.shapes.BBox
import com.graphhopper.util.shapes.GHPoint
import java.util.*


/**
 * Created by jcundill on 18/01/2017.
 */
class ControlSiteFinder(val gh: GraphHopper) {

    private val filter = DefaultEdgeFilter(gh.encodingManager.getEncoder("foot"))
    private val rnd = Random(System.currentTimeMillis())

    fun findControlSiteNear(point: GHPoint): GHPoint? = findControlSiteNear(point.lat, point.lon)

    fun routeRequest(req: GHRequest, numAlternatives: Int = 0): GHResponse {
        req.weighting = "shortest"
        if (numAlternatives > 1) {
            req.algorithm = Parameters.Algorithms.ALT_ROUTE
            req.hints.put(Parameters.Algorithms.AltRoute.MAX_SHARE, 0.5)
        }

       return gh.route(req)

    }


    fun getRandomLocationAtDistance(loc: GHPoint, dist: Double): GHResponse? {
        val point = findControlSiteNear(getCoords(loc, randomBearing, dist))

        return when (point) {
            is GHPoint -> routeRequest(GHRequest(loc, point), 2)
            else -> null
        }
    }


    private fun findControlSiteNear(x: Double, y: Double): GHPoint? {
        val qr = gh.locationIndex.findClosest(x, y, filter)
        return if (!qr.isValid) null
        else {
            val point = qr.snappedPoint
            GHPoint(point.lat, point.lon)
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
