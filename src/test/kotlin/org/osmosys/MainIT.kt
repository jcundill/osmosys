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

import com.graphhopper.util.shapes.GHPoint
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.osmosys.gpx.GpxWriter
import org.osmosys.mapping.MapFitter
import org.osmosys.mapping.MapPrinter
import org.osmosys.maprun.KmlWriter
import java.io.File
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MainIT {
    private val gpxWriter = GpxWriter()
    private val kmlWriter = KmlWriter()
    private val mapPrinter = MapPrinter()
    private val fitter = MapFitter()

    private lateinit var osmosys: Osmosys

    @BeforeAll
    fun beforeTests() {
        osmosys = Osmosys("derbyshire-latest")
    }



    @Test
    fun overpasserTest() {
        osmosys.findFurniture(GHPoint(53.222600, -1.449538))
    }

    @Test
    fun courseFromKml() {
        val initialCourse = Course.buildFromKml("Map-1588865743205.kml")
        val course = osmosys.score(initialCourse)
        printStats(course)
        val problem = osmosys.makeProblem(course)
        val newCourse = osmosys.findCourse(problem, 1000)
        if (newCourse != null )
            printStats(newCourse)

    }

    @Test
    fun kmlFromGpx() {
        val initialCourse = Course.buildFromGPX("Map-1588589961836.gpx")
        val course = osmosys.score(initialCourse)
        printStats(course)
        val kml = kmlWriter.generate(initialCourse.controls, "mapName")
        File("out.kml").writeText(kml)
    }

    @Test
    fun main() {

        val props =  "./streeto.properties"

        val initialCourse = Course.buildFromProperties(props)
        //val params = Course.buildFromGPX("/Users/jcundill/stash/wobble/Map-1538908777809.gpx")
        osmosys.findFurniture(initialCourse.controls[0])
        val problem = osmosys.makeProblem(initialCourse)
        val solution = osmosys.findCourse(problem, 1000)

        if (solution != null) {
           val timestamp = Date().time
            gpxWriter.writeToFile(solution, "Map-$timestamp.gpx")

            printMap(solution, timestamp)
            printStats(solution)
            printControls(solution)
        }
    }

    private fun printMap(solution: Course, timestamp: Long) {
        with(solution) {
            val envelopeToMap = osmosys.getEnvelopeForProbableRoutes(controls)
            mapPrinter.generateMapFiles(filename = "Map-$timestamp",
                    title = "${(route.distance / 1000).toInt()}K/${controls.size - 2}Controls",
                    controls = controls,
                    centre = envelopeToMap.centre(),
                    box = fitter.getForEnvelope(envelopeToMap)!!)
            File("Map-$timestamp.kml").writeText(kmlWriter.generate(controls, "Map-$timestamp"))
        }
    }

    private fun printControls(course: Course) {
      course.controls.forEachIndexed {idx, it ->
            val fd = osmosys.describe(it)
          if(fd == null) {
              val ctrl = course.controls[idx]
            val instruction = course.route.instructions.find(ctrl.lat, ctrl.lon, 10.0)
              if( instruction != null) {
                  println("$idx = ${instruction.name}")
              } else println("$idx = ???????")
          } else println("$idx = $fd")
        }
    }

    private fun printStats(course: Course) {
        with(course) {
            println()
            println(route.distance)
            println(controls.size)
            println("Energy: $energy")
            println("${"Leg = ".padStart(43)}${(1 until controls.size).map{ it.toString().padStart(4)}}")
            println("${"Scores = ".padStart(43)}${legScores.map { scoreFormatter(it) }}")
            featureScores.forEach { println("${it.key.padStart(40)} = ${it.value.map { scoreFormatter(it)}}") }
        }
    }

    private fun scoreFormatter(it: Double) = BigDecimal(it).setScale(2, RoundingMode.HALF_EVEN)

    private fun generateAppInput(controls: List<GHPoint>): List<String> {
        return controls.map { "${it.lat.toFloat()},${it.lon.toFloat()}" }
    }
}
