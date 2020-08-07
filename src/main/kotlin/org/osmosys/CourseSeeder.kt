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
import org.osmosys.seeders.*

class CourseSeeder(private val csf: ControlSiteFinder) {

    private val fitter = MapFitter()
    private val rect = RectangleSeeder(csf)
    private val triangle = TriangleSeeder(csf)
    private val hourglass = HourglassSeeder(csf)
    private val fatHourglass = FatHourglassSeeder(csf)
    private val centredFatHourglass = CentredFatHourglassSeeder(csf)
    private val centredHourglass = CentredHourglassSeeder(csf)

    private fun chooseSeeder(): SeedingStrategy {
        val roll = rnd.nextDouble() * 6.0
        return when {
            roll < 1.0 -> centredFatHourglass
            roll < 2.0 -> centredHourglass
            roll < 3.0 -> rect
            roll < 4.0 -> triangle
            roll < 5.0 -> fatHourglass
            else -> hourglass
        }
    }

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
        val controls =  chooseSeeder().seed(initialControls, requestedNumControls, requestedCourseLength)
        val ctrls = if(rnd.nextDouble() < 0.5) controls else controls.reversed()
        return TSP(csf).run(listOf(startPoint) + ctrls + finishPoint)
    }


    private fun canBeMapped(env: Envelope) =
            fitter.getForEnvelope(env) != null

}