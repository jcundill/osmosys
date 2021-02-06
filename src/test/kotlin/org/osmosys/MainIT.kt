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
import org.jxmapviewer.JXMapViewer
import org.jxmapviewer.OSMTileFactoryInfo
import org.jxmapviewer.painter.CompoundPainter
import org.jxmapviewer.painter.Painter
import org.jxmapviewer.viewer.*
import org.osmosys.csv.ScoreWriter
import org.osmosys.genetic.LastMondayRunner
import org.osmosys.gpx.GpxWriter
import org.osmosys.mapping.MapBox
import org.osmosys.mapping.MapFitter
import org.osmosys.mapping.MapPrinter
import org.osmosys.maprun.KmlWriter
import java.io.File
import java.util.*
import javax.swing.JFrame
import kotlin.math.abs


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MainIT {
    private val gpxWriter = GpxWriter()
    private val kmlWriter = KmlWriter()
    private val scoreWriter = ScoreWriter()
    private val mapPrinter = MapPrinter()
    private val fitter = MapFitter()

    private lateinit var osmosys: Osmosys

    @BeforeAll
    fun beforeTests() {
        osmosys = Osmosys("derbyshire-latest")
    }


    @Test
    fun overpasserTest() {
        osmosys.findFurniture(ControlSite(53.222600, -1.449538))
    }

    @Test
    fun courseFromKml() {
        val initialCourse = Course.buildFromKml("Map-1590429897887.kml")
        val course = osmosys.score(initialCourse)
        val timestamp = Date().time
        printStats(course, timestamp)
        XprintStats(course)
//        val problem = osmosys.makeProblem(course)
//        val newCourse = osmosys.findCourse(problem, 1000)
//        if (newCourse != null ) {
//            printStats(newCourse, timestamp)
//            printMap(newCourse, Date().time)
//        }

    }

    @Test
    fun gpxView() {
        val mapViewer = JXMapViewer()

        // Display the viewer in a JFrame

        // Display the viewer in a JFrame
        val frame = JFrame("JXMapviewer2 Example 2")
        frame.contentPane.add(mapViewer)
        frame.setSize(800, 600)
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame.isVisible = true

        // Create a TileFactoryInfo for OpenStreetMap

        // Create a TileFactoryInfo for OpenStreetMap
        val info: TileFactoryInfo = OSMTileFactoryInfo()
        val tileFactory = DefaultTileFactory(info)
        mapViewer.tileFactory = tileFactory

        val frankfurt = GeoPosition(50, 7, 0, 8, 41, 0)
        val wiesbaden = GeoPosition(50, 5, 0, 8, 14, 0)
        val mainz = GeoPosition(50, 0, 0, 8, 16, 0)
        val darmstadt = GeoPosition(49, 52, 0, 8, 39, 0)
        val offenbach = GeoPosition(50, 6, 0, 8, 46, 0)

        // Create a track from the geo-positions

        // Create a track from the geo-positions
        val track = listOf(frankfurt, wiesbaden, mainz, darmstadt, offenbach)
        val routePainter = RoutePainter(track)

        // Set the focus

        // Set the focus
        mapViewer.zoomToBestFit(HashSet(track), 0.7)

        // Create waypoints from the geo-positions

        // Create waypoints from the geo-positions
        val waypoints: Set<Waypoint> = HashSet<Waypoint>(
            Arrays.asList(
                DefaultWaypoint(frankfurt),
                DefaultWaypoint(wiesbaden),
                DefaultWaypoint(mainz),
                DefaultWaypoint(darmstadt),
                DefaultWaypoint(offenbach)
            )
        )

        // Create a waypoint painter that takes all the waypoints

        // Create a waypoint painter that takes all the waypoints
        val waypointPainter: WaypointPainter<Waypoint> = WaypointPainter<Waypoint>()
        waypointPainter.waypoints = waypoints

        // Create a compound painter that uses both the route-painter and the waypoint-painter

        // Create a compound painter that uses both the route-painter and the waypoint-painter
        val painters: MutableList<Painter<JXMapViewer>> = ArrayList<Painter<JXMapViewer>>()
        painters.add(routePainter)
        painters.add(waypointPainter)

        val painter = CompoundPainter(painters)
        mapViewer.overlayPainter = painter
        print("wwww")
    }

    @Test
    fun kmlFromGpx() {
        val initialCourse = Course.buildFromGPX("aaa.gpx")
        val c =
            initialCourse.copy(controls = initialCourse.controls.map { osmosys.csf.findNearestControlSiteTo(it.position)!! })
        val course = osmosys.score(c)
        val timestamp = Date().time
        printStats(course, timestamp)
//        val kml = kmlWriter.generate(initialCourse.controls, "mapName")
//        File("out.kml").writeText(kml)
        printMap(course, timestamp)
//        printControls(course)
    }

//    @Test
//    fun ga() {
//        val initialCourse = Course.buildFromProperties("./streeto.properties")
//        val ga =
//            GA(osmosys.csf, 50, initialCourse.controls, initialCourse.requestedNumControls, initialCourse.distance())
//        val course = ga.run()
//        if (course != null) {
//            val scoredCourse =
//                osmosys.score(Course(initialCourse.distance(), initialCourse.requestedNumControls, course.controls))
//            gpxWriter.writeToFile(scoredCourse, "aaa.gpx")
//        }
//
//    }
//
//    @Test
//    fun main() {
//
//        val props = "./streeto.properties"
//
//        val initialCourse = Course.buildFromProperties(props)
//        //val params = Course.buildFromGPX("/Users/jcundill/stash/wobble/Map-1538908777809.gpx")
//        val problem = osmosys.makeProblem(initialCourse)
//        val solution = osmosys.findCourse(problem, 2000)
//
//        if (solution != null) {
//            val timestamp = Date().time
//            gpxWriter.writeToFile(solution, "Map-$timestamp.gpx")
//
//            printMap(solution, timestamp)
//            printStats(solution, timestamp)
//            printControls(solution)
//        }
//    }

    private fun printMap(solution: Course, timestamp: Long) {
        with(solution) {
            val envelopeToMap = osmosys.getEnvelopeForProbableRoutes(controls)
            val box = fitter.getForEnvelope(envelopeToMap)!!
            //val bestSplit = makeDoubleSidedIfPossible(controls, box)
            mapPrinter.generateMapFiles(
                filename = "Map-$timestamp",
                title = "${(route.distance / 1000).toInt()}K/${controls.size - 2}Controls",
                controls = controls,
                centre = envelopeToMap.centre(),
                box = box
            )
            File("Map-$timestamp.kml").writeText(kmlWriter.generate(controls, "Map-$timestamp"))
        }
    }

    private fun makeDoubleSidedIfPossible(controls: List<ControlSite>, box: MapBox): List<List<ControlSite>>? {
        // find subset in the middle where
        // subset can be mapped on a larger scale
        // and others including head and tail of subset can also be mapped on the same larger scale
        val splitLocations = (2 until controls.size - 2).flatMap { startOffset ->
            findPartitionsFromOffset(startOffset, controls, box)
        }
        if (splitLocations.isNotEmpty()) {
            val sorted = splitLocations.sortedBy { it.second.scale }
            val mostEqual = sorted.takeWhile { it.second.scale == sorted[0].second.scale }
                .minByOrNull { abs(controls.size / 2.0 * (it.first[1].size - it.first[0].size)) }!!

        }
        return null
    }

    private fun findPartitionsFromOffset(
        startOffset: Int,
        controls: List<ControlSite>,
        box: MapBox
    ): List<Pair<List<List<ControlSite>>, MapBox>> {
        return (startOffset until controls.size - 2).mapNotNull { endOffset ->
            getSplitForOffset(controls, startOffset, endOffset, box)
        }
    }

    private fun getSplitForOffset(
        controls: List<ControlSite>,
        startOffset: Int,
        endOffset: Int,
        box: MapBox
    ): Pair<List<List<ControlSite>>, MapBox>? {
        val partitionsFromStart = listOf(
            controls.take(endOffset).drop(startOffset),
            controls.take(startOffset + 1) + controls.drop(endOffset - 1)
        )
        val env1 = osmosys.getEnvelopeForProbableRoutes(partitionsFromStart[0])
        val env2 = osmosys.getEnvelopeForProbableRoutes(partitionsFromStart[1])
        val box1 = fitter.getForEnvelope(env1)
        val box2 = fitter.getForEnvelope(env2)
        return if (box1 != null && box2 != null) {
            val canPossiblySplit = box1.scale < box.scale && box2.scale < box.scale
            when {
                !canPossiblySplit -> null
                box1.scale == box2.scale && box1.landscape == box2.landscape -> Pair(partitionsFromStart, box1)
                box1.scale > box2.scale && fitter.canFitOnMap(env2, box1) -> Pair(partitionsFromStart, box1)
                box2.scale > box1.scale && fitter.canFitOnMap(env1, box2) -> Pair(partitionsFromStart, box2)
                else -> null
            }
        } else
            null
    }

    private fun printControls(course: Course) {
        course.controls.forEachIndexed { idx, it ->
            println("$idx = ${it.description}")
        }
    }

    private fun printStats(course: Course, timestamp: Long) {
        scoreWriter.writeScores(course, "Map-$timestamp.csv")
    }

    private fun XprintStats(course: Course) {
        with(course) {
            println()
            println(route.distance)
            println(controls.size)
            println("Energy: $energy")
        }
    }


    private fun generateAppInput(controls: List<GHPoint>): List<String> {
        return controls.map { "${it.lat.toFloat()},${it.lon.toFloat()}" }
    }


    @Test
    fun jenetic() {
        val initialCourse = Course.buildFromProperties("./streeto.properties")
        val lastMondayRunner = LastMondayRunner(osmosys.csf)
        val course = lastMondayRunner.run(initialCourse)
        //val course = courses.first()
        val scoredCourse =
            osmosys.score(Course(initialCourse.distance(), initialCourse.requestedNumControls, course.controls))
        gpxWriter.writeToFile(scoredCourse, "aaa.gpx")
        println("best score: ${1.0 - scoredCourse.energy}")
        println("distance: ${scoredCourse.route.distance}")
    }
}
