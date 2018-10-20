package jon.test

import com.graphhopper.util.shapes.GHPoint
import jon.test.gpx.GpxWriter
import jon.test.mapping.MapFitter
import jon.test.mapping.MapPrinter
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.io.File
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MainIT {
    private val gpxWriter = GpxWriter()
    private val mapPrinter = MapPrinter()
    private val fitter = MapFitter()

    private lateinit var streetO: StreetO

    @BeforeAll
    fun beforeTests() {
        streetO = StreetO("england-latest")
    }

    @Test
    fun main() {

        val props =  "./streeto.properties"

        val params = Course.buildFromProperties(props)
        //val params = Course.buildFromGPX("/Users/jcundill/stash/wobble/Map-1538908777809.gpx")
        val problem = streetO.makeProblem(params)
        val solution = streetO.findCourse(problem, 1000)

        if (solution != null) {


            val timestamp = Date().time
            gpxWriter.writeToFile(solution, "Map-$timestamp.gpx")

            printMap(solution, timestamp)

            printStats(solution)

            val controlString = generateAppInput(solution.controls).joinToString(separator = "|")
            println(controlString)
            File("Map-$timestamp.txt").writeText(controlString)
        }
    }

    private fun printMap(solution: Course, timestamp: Long) {
        with(solution) {
            val envelopeToMap = streetO.getEnvelopeForProbableRoutes(controls)
            mapPrinter.generatePDF(filename = "Map-$timestamp.pdf",
                    title = "Test+${(route.distance / 1000).toInt()}K+${controls.size - 2}+Controls",
                    controls = controls,
                    centre = envelopeToMap.centre(),
                    box = fitter.getForEnvelope(envelopeToMap)!!)
        }
    }

    private fun printStats(course: Course) {
        with(course) {
            println()
            println(route.distance)
            println(controls.size)
            println("Energy: $energy")
            println("Scores: ${numberedControlScores.joinToString(", ")}")
            featureScores.forEach { println("${it.key} = ${it.value}") }
        }
    }

    private fun generateAppInput(controls: List<GHPoint>): List<String> {
        return controls.map { "${it.lat.toFloat()},${it.lon.toFloat()}" }
    }
}
