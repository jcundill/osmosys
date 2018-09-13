package jon.test

import com.graphhopper.GHRequest
import com.graphhopper.util.shapes.GHPoint
import com.vividsolutions.jts.geom.Envelope
import jon.test.annealing.InfeasibleProblemException
import jon.test.annealing.LinearDecayScheduler
import jon.test.annealing.Solver
import jon.test.constraints.CourseLengthConstraint
import jon.test.constraints.IsRouteableConstraint
import jon.test.constraints.PrintableOnMapConstraint
import jon.test.gpx.GpxWriter
import jon.test.mapping.MapFitter
import jon.test.mapping.MapPrinter
import jon.test.scorers.*
import java.io.File
import java.util.*

val rnd: RandomStream = PseudoRandom()//RepeatableRandom(112143432234L)

object Main {
    private val gpxWriter = GpxWriter()
    private val mapPrinter = MapPrinter()
    private val fitter = MapFitter()

    @JvmStatic
    fun main(args: Array<String>) {

        val props = when {
            args.isNotEmpty() -> args[0]
            else -> "./streeto.properties"
        }

        val params = CourseParameters.buildFromProperties(props)

        val constraints = listOf(
                IsRouteableConstraint(),
                CourseLengthConstraint(params.distance),
                PrintableOnMapConstraint(MapFitter())
        )

        val featureScorers = listOf(
                LegLengthScorer(),
                LegRouteChoiceScorer(),
                LegComplexityScorer(),
                BeenThisWayBeforeScorer(),
                ComingBackHereLaterScorer(),
                DidntMoveScorer(),
                LastControlNearTheFinishScorer(),
                DogLegScorer()
        )


        println("init")
        val csf = GHWrapper.initGH("england-latest")
        println("done")
        val scorer = CourseScorer(featureScorers, csf::findRoutes)

        try {
            val problem = CourseFinder(csf, constraints, scorer, params)
            val solver = Solver(problem, LinearDecayScheduler(1000.0, 1000))
            val solution = solver.solve()

            val courseScore = problem.energy(solution)
            val detailedScores = featureScorers.map {
                it::class.java.simpleName
            }.zip(solution.featureScores!!)

            val best = csf.routeRequest(GHRequest(solution.controls)).best
            val envelopeToMap = getEnvelopeForProbableRoutes(solution, csf)

            val timestamp = Date().time
            gpxWriter.writeToFile(solution.controls, best, courseScore, solution.numberedControlScores, detailedScores, "Map-$timestamp.gpx")

            mapPrinter.generatePDF(filename = "Map-$timestamp.pdf",
                    title = "Test+${(best.distance / 1000).toInt()}K+${params.numControls}+Controls",
                    controls = solution.controls,
                    centre = envelopeToMap.centre(),
                    box = fitter.getForEnvelope(envelopeToMap)!!)

            println()
            println("Hit: ${csf.hit}, Miss: ${csf.miss}, Bad: ${problem.bad}")
            println(best.distance)
            println(solution.controls.size)
            println("Energy: $courseScore")
            println("Scores: ${solution.numberedControlScores.joinToString(", ")}")


            detailedScores.forEach { println(it) }

            val controlString = generateAppInput(solution.controls).joinToString(separator = "|")
            println(controlString)
            File("Map-$timestamp.txt").writeText(controlString)

        } catch (e: InfeasibleProblemException) {
            println(e.message ?: "All gone badly wrong")
        }


    }

    private fun getEnvelopeForProbableRoutes(solution: CourseImprover, csf: ControlSiteFinder): Envelope {
        val routes = solution.controls.windowed(2).flatMap {
            val req = GHRequest(it.first(), it.last())
            csf.routeRequest(req, 3).all
        }

        val env = Envelope()
        routes.forEach { it.points.forEach { p -> env.expandToInclude(p.lon, p.lat) } }
        return env
    }

    private fun generateAppInput(controls: List<GHPoint>): List<String> {
        return controls.map { "${it.lat.toFloat()},${it.lon.toFloat()}" }
    }
}
