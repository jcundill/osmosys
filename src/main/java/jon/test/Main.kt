package jon.test

import com.graphhopper.GHRequest
import com.graphhopper.util.shapes.GHPoint
import jon.test.constraints.CourseLengthConstraint
import jon.test.constraints.IsRouteableConstraint
import jon.test.constraints.PrintableOnMapConstraint
import jon.test.scorers.*
import xyz.thepathfinder.simulatedannealing.ExponentialDecayScheduler
import xyz.thepathfinder.simulatedannealing.LinearDecayScheduler
import xyz.thepathfinder.simulatedannealing.Solver
import java.util.*


object Main {
    @JvmStatic
    fun main(args: Array<String>) {


        //val params = CourseParameters(distance = 8000.0, points = 10, start = GHPoint(52.988304, -1.203265))
        val params = CourseParameters(distance = 9000.0, points = 17, start = GHPoint(53.223489, -1.461063))
        //val params = CourseParameters(distance = 9000.0, points = 9, start = GHPoint(53.234060, -1.436845))

        val featureScorers = listOf(
                LegLengthScorer(params),
                LegRouteChoiceScorer(params),
                LegComplexityScorer(params),
                BeenThisWayBeforeScorer(params),
                DidntMoveScorer(params),
                LastControlNearTheFinishScorer(params),
                DogLegScorer(params)
        )

        val constraints = listOf(
                CourseLengthConstraint(params),
                PrintableOnMapConstraint(params),
                IsRouteableConstraint(params)
        )

        //val csf = GhWrapper.initGH("NG86BA")
        val csf = GhWrapper.initGH("S403DF")
        val scorer = CourseScorer(csf, featureScorers,params)

        val problem = CourseFinder(csf, constraints, scorer, params)
        val solver = Solver(problem, LinearDecayScheduler(1000.0, 2000))
        val solution = solver.solve()

        val best = csf.routeRequest(GHRequest(solution.controls)).best


        val courseScore = problem.energy(solution)
        val detailedScores = featureScorers.map {
            it::class.java.simpleName
        }.zip(solution.featureScores!!)

        GpxWriter().writeToFile(solution.controls, best, courseScore, solution.numberedControlScores, detailedScores, "jon.gpx")
        MapPrinter(params).generatePDF(filename = "Map-${Date().time}.pdf", title = "Test+${(best.distance/1000).toInt()}K+${params.points -2 }+Controls", points = solution.controls)
        println()
        println("Hit: ${csf.hit}, Miss: ${csf.miss}, Bad: ${problem.bad}")
        println(best.distance)
        println(solution.controls.size)
        println("Energy: $courseScore")
        println("Scores: ${solution.numberedControlScores.joinToString(", ")}")


        detailedScores.forEach { println(it) }
    }
}
