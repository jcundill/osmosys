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

import com.graphhopper.PathWrapper
import com.graphhopper.util.shapes.GHPoint
import org.osmosys.gpx.GpxWriter
import org.osmosys.maprun.KmlWriter
import java.io.FileInputStream
import java.util.*

fun String.toGHPoint(): GHPoint? {
    val arr = this.split(',')
    return when (arr.size) {
        2 -> GHPoint(arr[0].toDouble(), arr[1].toDouble())
        else -> null
    }
}


data class Course(
        val requestedDistance: Double? = null,
        val requestedNumControls: Int = 6,
        val controls: List<GHPoint> = emptyList()) {

    lateinit var legScores: List<Double>
    lateinit var featureScores: Map<String, List<Double>>
    var energy: Double = 1000.0
    lateinit var route: PathWrapper

    fun distance() = when(requestedDistance) {
        null -> route.distance * 0.8 // no desired distance given, make it about as long as it is now
        else -> requestedDistance
    }

    companion object {

        fun buildFromProperties(filename: String): Course {
            val props = Properties()
            props.load(FileInputStream(filename))
            val waypoints = props.getProperty("controls")
            val distance = props.getProperty("distance", "6000").toDouble()
            val numControls = props.getProperty("numControls", "10").toInt()
            val initials = when (waypoints) {
                null -> emptyList()
                else -> waypoints.split("|").map {
                    val latlon = it.split(",").map {s -> s.trim().toDouble()}
                    GHPoint(latlon.first(), latlon.last())
                }
            }

            return Course(controls = initials, requestedDistance = distance, requestedNumControls = numControls)
        }

        fun buildFromGPX(filename: String): Course {
            val gpx = GpxWriter()
            val points = gpx.readFromFile(filename)
            val numControls = points.size - 2

            return Course(controls = points, requestedNumControls = numControls)
        }

        fun buildFromKml(filename: String): Course {
            val points = KmlWriter().readFile(filename)
            val numControls = points.size - 2

            return Course(controls = points, requestedNumControls = numControls)

        }
    }
}