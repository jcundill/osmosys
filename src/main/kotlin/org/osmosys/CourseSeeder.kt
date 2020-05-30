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

import com.vividsolutions.jts.geom.Envelope
import org.osmosys.annealing.InfeasibleProblemException
import org.osmosys.improvers.TSP
import org.osmosys.mapping.MapFitter

class CourseSeeder(private val csf: ControlSiteFinder) {

    private val fitter = MapFitter()

    fun chooseInitialPoints(initialPoints: List<ControlSite>, requestedNumControls: Int, requestedCourseLength: Double): List<ControlSite> {

        val env = Envelope()

        // check that everything we have been given is mappable
        val startPoint = csf.findNearestControlSiteTo(initialPoints.first().position)
                ?: throw InfeasibleProblemException("no control point near the start")

        val finishPoint = csf.findNearestControlSiteTo(initialPoints.last().position)
                ?: throw InfeasibleProblemException("no control point near the finish")

        val chosenControls = initialPoints.drop(1).dropLast(1).map {
            csf.findNearestControlSiteTo(it.position)
                    ?: throw InfeasibleProblemException("no control point near the waypoint $it")
        }

        with(env) {
            expandToInclude(startPoint.position.lon, startPoint.position.lat)
            expandToInclude(finishPoint.position.lon, finishPoint.position.lat)
            chosenControls.forEach {
                expandToInclude(it.position.lon, it.position.lat)
            }
        }

        if (!canBeMapped(env)) {
            throw InfeasibleProblemException("initial course cannot be mapped")
        }

        val initialControls = listOf(startPoint) + chosenControls + finishPoint
        return doGen(initialControls, requestedNumControls, requestedCourseLength)
    }

    private fun doGen(initialPoints: List<ControlSite>, requestedNumControls: Int, requestedCourseLength: Double): List<ControlSite> {
        val twistFactor = 0.67
        val circLength = 3.5
        val angle = 1.5658238

        val scaleFactor = requestedCourseLength * twistFactor / circLength
        val bearing = csf.randomBearing

        val first = initialPoints.first()
        val last = initialPoints.last()
        val second = csf.getCoords(first.position, Math.PI + bearing, scaleFactor)
        val third = csf.getCoords(first.position, Math.PI + bearing + angle, scaleFactor)

        val points = listOf(first.position, second, third, first.position).map { csf.findControlSiteNear(it, 50.0) }
        val resp = csf.routeRequest(points)

        val pointList = resp.best.points
        val numPoints = pointList.size

        val controls = (1..requestedNumControls).mapNotNull {
            val position = pointList.get(it * (numPoints / requestedNumControls - 1))
            csf.findNearestControlSiteTo(position)
        }

        return TSP(csf).run(listOf(first) + controls + last)
    }

    private fun canBeMapped(env: Envelope) =
            fitter.getForEnvelope(env) != null

}