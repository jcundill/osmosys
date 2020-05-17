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
import com.graphhopper.util.shapes.GHPoint
import com.vividsolutions.jts.geom.Envelope
import org.osmosys.annealing.InfeasibleProblemException
import org.osmosys.improvers.TSP
import org.osmosys.improvers.dist2d
import org.osmosys.mapping.MapFitter

class CourseSeeder(private val csf: ControlSiteFinder) {

    private val fitter = MapFitter()

    fun chooseInitialPoints(initialPoints: List<GHPoint>, requestedNumControls: Int, requestedCourseLength: Double): List<GHPoint> {

        val env = Envelope()

        // check that everything we have been given is mappable
        val startPoint = csf.findNearestControlSiteTo(initialPoints.first())
                ?: throw InfeasibleProblemException("no control point near the start")

        val finishPoint = csf.findNearestControlSiteTo(initialPoints.last())
                ?: throw InfeasibleProblemException("no control point near the finish")

        val chosenControls = initialPoints.drop(1).dropLast(1).map {
            csf.findNearestControlSiteTo(it)
                    ?: throw InfeasibleProblemException("no control point near the waypoint $it")
        }

        with(env) {
            expandToInclude(startPoint.lon, startPoint.lat)
            expandToInclude(finishPoint.lon, finishPoint.lat)
            chosenControls.forEach {
                expandToInclude(it.lon, it.lat)
            }
        }

        if (!canBeMapped(env)) {
            throw InfeasibleProblemException("initial course cannot be mapped")
        }

        // ok, so everything we have been given can be mapped, so add in a number of generated controls and return that
//        val numToGenerate = requestedNumControls - chosenControls.size
//        return listOf(startPoint) + when {
//            numToGenerate < 0 -> removeControls(-numToGenerate, chosenControls)
//            numToGenerate == 0 -> emptyList()
//            else -> generateControls(numToGenerate, requestedCourseLength!!, env)
//        } + chosenControls + finishPoint

        return doGen(initialPoints, requestedNumControls, requestedCourseLength)
    }

    private fun doGen(initialPoints: List<GHPoint>, requestedNumControls: Int, requestedCourseLength: Double): List<GHPoint> {
        val twistFactor = 0.67
        val circLength = 3.5
        val angle = 1.5658238

        val scaleFactor = requestedCourseLength * twistFactor / circLength
        val bearing = csf.randomBearing

        val first = initialPoints.first()
        val second = csf.getCoords(first, Math.PI + bearing, scaleFactor)
        val third = csf.getCoords(first, Math.PI + bearing + angle, scaleFactor)

        val points = listOf(first, second, third, first).map{csf.findControlSiteNear(it, 50.0)}
        val resp = csf.routeRequest(GHRequest( points ))

        val pointList = resp.best.points
        val numPoints = pointList.size

        val controls = (1..requestedNumControls).map{ pointList.get( it * (numPoints/requestedNumControls - 1)) }

        return TSP(csf).run(listOf(first) + controls + first)
    }

    private fun canBeMapped(env: Envelope) =
            fitter.getForEnvelope(env) != null


    private fun generateControls(numControls: Int, distance: Double, env: Envelope): List<GHPoint> {

        val bearing = csf.randomBearing
        val angle = (2 * Math.PI) / numControls

        val envCentre = GHPoint(env.centre().y, env.centre().x)

        // if the env is really small (only a start perhaps)
        // treat it as being on the radius of the circle
        // otherwise buildFrom an initial circle around its centre
        val w = dist2d.calcDist(env.minY, env.minX, env.maxY, env.maxX)

        val fudgeFactor = 5.0 / numControls
        val radius = fudgeFactor * distance / (2 * Math.PI)
        val circleCentre = when {
            w < 1000 -> csf.getCoords(envCentre, Math.PI + bearing, radius)
            else -> envCentre
        }

        val positions = (1..numControls).map { num ->
            csf.getCoords(circleCentre, (num * angle) + bearing, radius)
        }
        return positions.map { csf.findControlSiteNear(it, radius / 5.0) }
    }

}