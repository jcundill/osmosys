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

package org.osmosys.gpx

import com.graphhopper.util.shapes.GHPoint
import org.alternativevision.gpx.GPXParser
import org.alternativevision.gpx.beans.GPX
import org.alternativevision.gpx.beans.Route
import org.alternativevision.gpx.beans.Track
import org.alternativevision.gpx.beans.Waypoint
import org.osmosys.ControlSite
import org.osmosys.Course
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.DecimalFormat


/**
 * Created by jcundill on 23/02/2017.
 */
class GpxWriter {

    fun readFromFile(filename: String): List<ControlSite> {

        val fis = FileInputStream(filename)
        val parser = GPXParser()
        val gpx = parser.parseGPX(fis)

        fis.close()
        val route = gpx.routes.toList()[0]
        return route.routePoints.map { wpt -> ControlSite(wpt.latitude, wpt.longitude) }
    }

    fun writeToFile(course: Course, filename: String, labels: List<String> = emptyList()) {
        val df = DecimalFormat("#")
        val gpx = GPX()
        val rte = Route()
        rte.name = "Course"
        gpx.addRoute(rte)

        fun describeFeatures(idx: Int): String {
            return if(labels.isNullOrEmpty()) {
//                val i = idx
//                val descs = course.featureScores.map { "${it.key} ${df.format(100.0 - it.value[i] * 100.0)}%" }
//                return "Score: ${df.format(100.0 - course.numberedControlScores[i] * 100.0)}%" +
//                        descs.joinToString("\n")
                return ""
            } else {
                labels[idx]
            }
        }

        course.controls.forEachIndexed { idx, pt ->
            val wp = Waypoint().apply {
                latitude = pt.position.lat
                longitude = pt.position.lon
                name = when (idx) {
                    0 -> "Start"
                    course.controls.size - 1 -> "Start"
                    else -> "$idx".padStart(2, '0')

                }
                description = describeFeatures(idx)

            }
            gpx.addWaypoint(wp)
            rte.addRoutePoint(wp)
        }


        gpx.addTrack((Track()).apply {
            name = "Calculated Route"
            val s = df.format((1000.0 - course.energy) / 10.0)
            description =
                    """Length: ${df.format(course.route.distance)}
                  |Ascend: ${course.route.ascend} Descend: ${course.route.descend}
                  |Goodness: $s%""".trimMargin()

            //  now you can fetch the closest edge via:
            val wpts = course.route.points.map { pt ->
                Waypoint().apply {
                    latitude = pt.lat
                    longitude = pt.lon
                }
            }
            trackPoints = ArrayList(wpts)
        })

        val parser = GPXParser()

        val fred = File(filename)
        val fos = FileOutputStream(fred)
        parser.writeGPX(gpx, fos)
        fos.close()

    }


}
