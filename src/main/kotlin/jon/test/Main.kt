package jon.test

import com.graphhopper.GHRequest
import com.graphhopper.util.shapes.GHPoint
import jon.test.constraints.CourseLengthConstraint
import jon.test.constraints.IsRouteableConstraint
import jon.test.constraints.PrintableOnMapConstraint
import jon.test.mapping.MapPrinter
import jon.test.scorers.*
import xyz.thepathfinder.simulatedannealing.ExponentialDecayScheduler
import xyz.thepathfinder.simulatedannealing.InfeasibleProblemException
import xyz.thepathfinder.simulatedannealing.Solver
import java.util.*


object Main {
    @JvmStatic
    fun main(args: Array<String>) {


        val params = CourseParameters(distance = 8000.0, numControls = 8, start = GHPoint(52.988304, -1.203265))
        //val params = CourseParameters(distance = 5000.0, points = 6, start = GHPoint(51.469109, -0.094237)) //venetian road
        //val params = CourseParameters(distance = 6000.0, points = 8, start = GHPoint(53.223482, -1.461064), finish = GHPoint(53.233456, -1.433246))
        //val params = CourseParameters(distance = 6000.0, points = 8, start = GHPoint(53.223482, -1.461064), finish = GHPoint(51.511287, -0.113695))
        //val params = CourseParameters(distance = 9000.0, points = 9, start = GHPoint(53.234060, -1.436845))  //york
        //val params = CourseParameters(distance = 10000.0, points = 15, start = GHPoint(54.490507, -0.616562)) //whitby
        //val params = CourseParameters(distance = 10000.0, points = 15, start = GHPoint(51.511287, -0.113695)) //london
        //val params = CourseParameters(distance = 10000.0, points = 15, start = GHPoint(54.599451, -3.136601)) //keswick
        //val params = CourseParameters(distance = 10000.0, points = 15, start = GHPoint(54.422079, -2.9659541)) //ambleside
        //val params = CourseParameters(distance = 10000.0, points = 15, start = GHPoint(52.036697, -0.762663)) //milton keynes
        //val params = CourseParameters(distance = 5000.0, points = 7, start = GHPoint(52.906028, -1.380663)) //borrowash

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
        //val csf = GhWrapper.initGH("S403DF")
        println("init")
        val csf = GhWrapper.initGH("england-latest")
        println("done")
        val scorer = CourseScorer(csf, featureScorers,params)

        try {
            val problem = CourseFinder(csf, constraints, scorer, params)
            val solver = Solver(problem, ExponentialDecayScheduler(1000.0, 1000))
            val solution = solver.solve()

            val best = csf.routeRequest(GHRequest(solution.controls)).best


            val courseScore = problem.energy(solution)
            val detailedScores = featureScorers.map {
                it::class.java.simpleName
            }.zip(solution.featureScores!!)

            GpxWriter().writeToFile(solution.controls, best, courseScore, solution.numberedControlScores, detailedScores, "jon.gpx")
            MapPrinter(params).generatePDF(filename = "Map-${Date().time}.pdf", title = "Test+${(best.distance/1000).toInt()}K+${params.numControls}+Controls", controls = solution.controls)
            println()
            println("Hit: ${csf.hit}, Miss: ${csf.miss}, Bad: ${problem.bad}")
            println(best.distance)
            println(solution.controls.size)
            println("Energy: $courseScore")
            println("Scores: ${solution.numberedControlScores.joinToString(", ")}")


            detailedScores.forEach { println(it) }

        } catch (e: InfeasibleProblemException) {
            println(e.message ?: "All gone badly wrong")
        }


    }
}
