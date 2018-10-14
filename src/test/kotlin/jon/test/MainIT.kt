package jon.test

import com.graphhopper.PathWrapper
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

        val params = CourseParameters.buildFromProperties(props)
        //val params = CourseParameters.buildFromGPX("/Users/jcundill/stash/wobble/Map-1538908777809.gpx")
        val problem = streetO.makeProblem(params)
        val solution = streetO.findCourse(problem, 1000)

        if (solution != null) {

            val courseScore = problem.energy(solution)
            val detailedScores = streetO.getDetailedScores(solution.featureScores!!)
            val best = streetO.findBestRoute(solution.controls)

            val timestamp = Date().time
            gpxWriter.writeToFile(solution.controls, best, courseScore, solution.numberedControlScores, detailedScores, "Map-$timestamp.gpx")

            val envelopeToMap = streetO.getEnvelopeForProbableRoutes(solution.controls)
            mapPrinter.generatePDF(filename = "Map-$timestamp.pdf",
                    title = "Test+${(best.distance / 1000).toInt()}K+${solution.controls.size - 2}+Controls",
                    controls = solution.controls,
                    centre = envelopeToMap.centre(),
                    box = fitter.getForEnvelope(envelopeToMap)!!)

            printStats(best, solution, courseScore, detailedScores)

            val controlString = generateAppInput(solution.controls).joinToString(separator = "|")
            println(controlString)
            File("Map-$timestamp.txt").writeText(controlString)
        }
    }

    private fun printStats(best: PathWrapper, solution: CourseImprover, courseScore: Double, detailedScores: List<Pair<String, List<Double>>>) {
        println()
        println(best.distance)
        println(solution.controls.size)
        println("Energy: $courseScore")
        println("Scores: ${solution.numberedControlScores.joinToString(", ")}")
        detailedScores.forEach { println(it) }
    }

    private fun generateAppInput(controls: List<GHPoint>): List<String> {
        return controls.map { "${it.lat.toFloat()},${it.lon.toFloat()}" }
    }
}
