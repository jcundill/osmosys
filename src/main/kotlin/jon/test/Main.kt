package jon.test

import com.graphhopper.GHRequest
import com.graphhopper.util.shapes.GHPoint
import jon.test.annealing.InfeasibleProblemException
import jon.test.annealing.LinearDecayScheduler
import jon.test.annealing.Solver
import jon.test.constraints.CourseLengthConstraint
import jon.test.constraints.IsRouteableConstraint
import jon.test.constraints.PrintableOnMapConstraint
import jon.test.gpx.GpxWriter
import jon.test.mapping.MapPrinter
import jon.test.scorers.*
import java.io.File
import java.util.*

val rnd: RandomStream = PseudoRandom()//RepeatableRandom(112143432234L)

object Main {
    @JvmStatic
    fun main(args: Array<String>) {

        val props = when {
            args.isNotEmpty() -> args[0]
            else -> "./streeto.properties"
        }

        val params = CourseParameters.buildFromProperties(props)

        val featureScorers = listOf(
                LegLengthScorer(params),
//                LegStraightLineScorer(params),
                LegRouteChoiceScorer(params),
                LegComplexityScorer(params),
                BeenThisWayBeforeScorer(params),
                ComingBackHereLaterScorer(params),
                DidntMoveScorer(params),
                LastControlNearTheFinishScorer(params),
                DogLegScorer(params)
        )

        val constraints = listOf(
                IsRouteableConstraint(params),
                CourseLengthConstraint(params),
                PrintableOnMapConstraint(params)
        )

        println("init")
        val csf = GHWrapper.initGH(params.map)
        println("done")
        val scorer = CourseScorer(csf, featureScorers, params)

        try {
            val problem = CourseFinder(csf, constraints, scorer, params)
            val solver = Solver(problem, LinearDecayScheduler(1000.0, 1000))
            val solution = solver.solve()

            val best = csf.routeRequest(GHRequest(solution.controls)).best


            val courseScore = problem.energy(solution)
            val detailedScores = featureScorers.map {
                it::class.java.simpleName
            }.zip(solution.featureScores!!)

            val timestamp = Date().time
            GpxWriter().writeToFile(solution.controls, best, courseScore, solution.numberedControlScores, detailedScores, "Map-$timestamp.gpx")
            MapPrinter(params).generatePDF(filename = "Map-$timestamp.pdf", title = "Test+${(best.distance / 1000).toInt()}K+${params.numControls}+Controls", controls = solution.controls)
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

    private fun generateAppInput(controls: List<GHPoint>): List<String> {
        return controls.map { "${it.lat.toFloat()},${it.lon.toFloat()}" }
    }
}
