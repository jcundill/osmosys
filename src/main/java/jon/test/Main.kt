package jon.test

import com.graphhopper.GHRequest
import com.graphhopper.util.shapes.GHPoint
import jon.test.scorers.*
import xyz.thepathfinder.simulatedannealing.ExponentialDecayScheduler
import xyz.thepathfinder.simulatedannealing.Solver


object Main {
    @JvmStatic
    fun main(args: Array<String>) {


        val params = Params(distance = 8000.0, points = 10, start = GHPoint(52.988304, -1.203265))
        //val params = Params(distance = 6000.0, points = 12, start = GHPoint(53.234060, -1.436845))

        val featureScorers = listOf(
                CourseLengthScorer(params),
                PreviousLegLengthScorer(params),
                FollowingLegLengthScorer(params),
                PreviousLegRouteChoiceScorer(params),
                FollowingLegRouteChoiceScorer(params),
                LegComplexityScorer(params),
                BeenThisWayBeforeScorer(params),
                DidntMoveScorer(params),
                LastControlNearTheFinishScorer(params)
        )

        val csf = GhWrapper.initGH("NG86BA")
        //val csf = GhWrapper.initGH("S403DF")
        val scorer = CourseScorer(csf, featureScorers,params)

        val problem = CourseFinder(csf, scorer, params)
        val solver = Solver(problem, ExponentialDecayScheduler(1000.0, 1000))
        val solution = solver.solve()

        val best = csf.routeRequest(GHRequest(solution.controls)).best


        GpxWriter().writeToFile(solution.controls, best, "jon.gpx")
        MapPrinter(params).generatePDF(filename = "jon.pdf", points = solution.controls)
        println()
        println("Hit: ${problem.hit}, Miss: ${problem.miss}, Bad: ${problem.bad}")
        println(best.distance)
        println(solution.controls.size)
        println("Energy: ${problem.energy(solution)}")
        println("Scores: ${solution.legScores}")
    }
}
