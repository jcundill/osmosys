/*
 *
 *     Copyright (c) 2017-2020 Jon Cundill.
 *
 *     Permission is hereby granted, free of charge, to any person obtaining
 *     a copy of this software and associated documentation files (the "Software"),
 *     to deal in the Software without restriction, including without limitation
 *     the rights to use, copy, modify, merge, publish, distribute, sublicense,
 *     and/or sell copies of the Software, and to permit persons to whom the Software
 *     is furnished to do so, subject to the following conditions:
 *
 *     The above copyright notice and this permission notice shall be included in
 *     all copies or substantial portions of the Software.
 *
 *     THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *     EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 *     OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 *     IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 *     CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 *     TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE
 *     OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 *
 */

package org.osmosys

import com.graphhopper.GHRequest
import com.graphhopper.GHResponse
import com.graphhopper.GraphHopper
import com.graphhopper.routing.util.DefaultEdgeFilter
import com.graphhopper.storage.index.QueryResult
import com.graphhopper.util.Parameters
import com.graphhopper.util.PointList
import com.graphhopper.util.shapes.BBox
import com.graphhopper.util.shapes.GHPoint
import com.vividsolutions.jts.geom.Envelope
import org.osmosys.furniture.StreetFurnitureRepository
import org.osmosys.improvers.dist
import org.osmosys.mapping.MapBox
import java.lang.Math.toDegrees
import java.lang.Math.toRadians
import kotlin.math.*


/**
 * Created by jcundill on 18/01/2017.
 */
class ControlSiteFinder(private val gh: GraphHopper, private val furnitureRepository: StreetFurnitureRepository) {

    private val filter =  DefaultEdgeFilter.allEdges(gh.encodingManager.getEncoder("orienteering"))

    private val env = Envelope()

    private val routedLegCache = HashMap<Pair<GHPoint, GHPoint>, GHResponse>()
    var hit = 0
    var miss = 0

    fun loadLocalFurniture(start: ControlSite) {
        val scaleFactor = 5000.0
        val max = getCoords(start.position, Math.PI * 0.25, scaleFactor)
        val min = getCoords(start.position, Math.PI * 1.25, scaleFactor)
        val bbox = BBox(min.lon, max.lon, min.lat, max.lat)
        furnitureRepository.initialiseForBoundingBox(bbox)
    }

    fun findControlSiteNear(point: GHPoint, distance: Double = 500.0): ControlSite {
        var node = findNearestControlSiteTo(getCoords(point, randomBearing, distance))
        while (node == null) {
            node = findNearestControlSiteTo(getCoords(point, randomBearing, distance + ((rnd.nextDouble() - 0.5) * distance)))
        }
        return node
    }

    fun findAlternativeControlSiteFor(point: ControlSite, distance: Double = 500.0): ControlSite {
        var node = findNearestControlSiteTo(getCoords(point.position, randomBearing, rnd.nextDouble() * distance))
        while (node == null || node == point) {
            node = findNearestControlSiteTo(getCoords(point.position, randomBearing, rnd.nextDouble() * distance))

        }
        return node
    }

    fun routeRequest(controls: List<ControlSite>, numAlternatives: Int = 0): GHResponse {
        val req = GHRequest(controls.map { it.position })
        return routeRequest(req, numAlternatives)
    }

    fun routeRequest(req: GHRequest, numAlternatives: Int = 0): GHResponse {
        req.weighting = "fastest"
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
                req.weighting = "fastest"
                req.algorithm = Parameters.Algorithms.ALT_ROUTE
                req.hints.put(Parameters.Algorithms.AltRoute.MAX_SHARE, 0.8)
                val resp = gh.route(req)
                routedLegCache[p] = resp
                resp
            }
        }
    }


    fun findNearestControlSiteTo(p: GHPoint): ControlSite? {
        return when (val loc = findClosestStreetLocation(p)) {
            null -> null
            else -> {
                // have we got nearby furniture - if so always use that
                val f = findLocalStreetFurniture(loc, 50.0)
                if (f != null) f
                else {
                    val isTower = gh.locationIndex.findClosest(loc.lat, loc.lon, filter).snappedPosition == QueryResult.Position.TOWER
                    val desc = if (isTower) "junction" else "bend"
                    ControlSite(loc.lat, loc.lon, desc)
                }
            }
        }
    }

    private fun findLocalStreetFurniture(p: GHPoint, distance: Double = 20.0): ControlSite? {
        val possible = furnitureRepository.findNear(p, distance)
        if( possible != null) {
            // is this near to a way?
            val qr = gh.locationIndex.findClosest(possible.position.lat, possible.position.lon, filter)
            if( qr.queryDistance > 7.5 ) // more than a couple of metres back from 1 side of nearest way
                return null
        }
        return possible
    }

    private fun findClosestStreetLocation(p: GHPoint): GHPoint? {
        val qr = gh.locationIndex.findClosest(p.lat, p.lon, filter)
        return when {
            !qr.isValid -> null
            qr.snappedPosition == QueryResult.Position.EDGE -> {
                val pl = qr.closestEdge.fetchWayGeometry(3)
                pl.find { pt ->
                    val loc = gh.locationIndex.findClosest(pt.lat, pt.lon, filter).snappedPosition
                    loc == QueryResult.Position.TOWER || loc == QueryResult.Position.PILLAR
                }
            }
            else -> qr.snappedPoint
        }
    }

    internal fun getCoords(loc: GHPoint, bearing: Double, dist: Double): GHPoint {
        val radiusOfEarth = 6378.1 * 1000//Radius of the Earth

        val lat1 = toRadians(loc.lat)
        val lon1 = toRadians(loc.lon)

        val lat2 = asin(sin(lat1) * cos(dist / radiusOfEarth) +
                cos(lat1) * sin(dist / radiusOfEarth) * Math.cos(bearing))

        val lon2 = lon1 + atan2(Math.sin(bearing) * sin(dist / radiusOfEarth) * cos(lat1),
                cos(dist / radiusOfEarth) - sin(lat1) * sin(lat2))

        return GHPoint(toDegrees(lat2), toDegrees(lon2))
    }

    internal val randomBearing: Double
        get() = 2 * PI * rnd.nextDouble()

    fun routeFitsBox(points: PointList, possibleBoxes: List<MapBox>): Boolean {
        env.setToNull()
        points.forEach { env.expandToInclude(it.lon, it.lat) }
        return possibleBoxes.any { env.width < it.maxWidth && env.height < it.maxHeight }
    }
}
