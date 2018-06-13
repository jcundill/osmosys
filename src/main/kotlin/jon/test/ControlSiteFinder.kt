package jon.test

import com.graphhopper.GHRequest
import com.graphhopper.GHResponse
import com.graphhopper.GraphHopper
import com.graphhopper.routing.util.DefaultEdgeFilter
import com.graphhopper.storage.index.QueryResult
import com.graphhopper.util.Parameters
import com.graphhopper.util.PointList
import com.graphhopper.util.shapes.GHPoint
import com.vividsolutions.jts.geom.Envelope
import java.util.*
import kotlin.collections.HashMap


/**
 * Created by jcundill on 18/01/2017.
 */
class ControlSiteFinder(private val gh: GraphHopper) {

    private val filter = DefaultEdgeFilter(gh.encodingManager.getEncoder("streeto"))
    private val rnd = Random(System.currentTimeMillis())

    private val env = Envelope()

    private val routedLegCache = HashMap<Pair<GHPoint, GHPoint>, GHResponse>()
    var hit = 0
    var miss = 0


    fun findControlSiteNear(point: GHPoint, distance: Double = 500.0): GHPoint {
        var node = findControlSiteNearInternal(getCoords(point, randomBearing, distance))
        while (node == null){
            node = findControlSiteNearInternal(getCoords(point, randomBearing, distance + ((rnd.nextDouble() - 0.5) * distance)))
        }
        return node
    }
    fun findAlternativeControlSiteFor(point: GHPoint, distance: Double = 500.0): GHPoint {
        var node = findControlSiteNearInternal(getCoords(point, randomBearing,  Math.random() * distance))
        while (node == null || node == point ) {
            node = findControlSiteNearInternal(getCoords(point, randomBearing,  Math.random() * distance))

        }
        return node
    }

    fun routeRequest(req: GHRequest, numAlternatives: Int = 0): GHResponse {
        req.weighting = "shortest"
        if (numAlternatives > 1) {
            req.algorithm = Parameters.Algorithms.ALT_ROUTE
            req.hints.put(Parameters.Algorithms.AltRoute.MAX_SHARE, 0.5)
        }

        return gh.route(req)

    }

    fun findRoutes(from: GHPoint, to: GHPoint): GHResponse {
        val p = Pair(from, to)
        return when {
            routedLegCache.containsKey(p) -> {
                hit++
                routedLegCache[p]!!
            }
            else -> {
                miss++
                val req = GHRequest(from, to)
                req.weighting = "shortest"
                req.algorithm = Parameters.Algorithms.ALT_ROUTE
                req.hints.put(Parameters.Algorithms.AltRoute.MAX_SHARE, 0.8)
                val resp = gh.route(req)
                routedLegCache[p] = resp
                resp
            }
        }
    }


    private fun findControlSiteNearInternal(p: GHPoint): GHPoint? {
        val qr = gh.locationIndex.findClosest(p.lat, p.lon, filter)
        return when {
            !qr.isValid -> null
            qr.snappedPosition == QueryResult.Position.EDGE  -> {
                val pl = qr.closestEdge.fetchWayGeometry(3)
                val rnd = (pl.size * rnd.nextDouble()).toInt()
                pl.toGHPoint(rnd)
            }
            else -> qr.snappedPoint
        }
    }

    private fun getCoords(loc: GHPoint, bearing: Double, dist: Double): GHPoint {
        val radiusOfEarth = 6378.1 * 1000//Radius of the Earth

        val lat1 = Math.toRadians(loc.lat)
        val lon1 = Math.toRadians(loc.lon)

        val lat2 = Math.asin(Math.sin(lat1) * Math.cos(dist / radiusOfEarth) +
                Math.cos(lat1) * Math.sin(dist / radiusOfEarth) * Math.cos(bearing))

        val lon2 = lon1 + Math.atan2(Math.sin(bearing) * Math.sin(dist / radiusOfEarth) * Math.cos(lat1),
                Math.cos(dist / radiusOfEarth) - Math.sin(lat1) * Math.sin(lat2))

        return GHPoint(Math.toDegrees(lat2), Math.toDegrees(lon2))
    }

    private val randomBearing: Double
        get() = 2 * Math.PI * rnd.nextDouble()

    fun routeFitsBox(points: PointList, possibleBoxes: List<MapBox>): Boolean {
        env.setToNull()
        points.forEach { env.expandToInclude(it.lon, it.lat) }
        return possibleBoxes.any {env.width < it.maxWidth && env.height < it.maxHeight}
    }



}
