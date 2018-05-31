package jon.test

import com.graphhopper.GHRequest
import com.graphhopper.util.shapes.GHPoint
import jon.test.scorers.*
import xyz.thepathfinder.simulatedannealing.ExponentialDecayScheduler
import xyz.thepathfinder.simulatedannealing.Solver
import java.util.*


object Main {
    @JvmStatic
    fun main(args: Array<String>) {


        val params = CourseParameters(distance = 5000.0, points = 8, start = GHPoint(52.988304, -1.203265))
        //val params = CourseParameters(distance = 9000.0, points = 15, start = GHPoint(53.253731, -1.469357))
        //val params = CourseParameters(distance = 9000.0, points = 18, start = GHPoint(53.234060, -1.436845))

        val featureScorers = listOf(
                CourseLengthScorer(params),
                PreviousLegLengthScorer(params),
                FollowingLegLengthScorer(params),
                PreviousLegRouteChoiceScorer(params),
                FollowingLegRouteChoiceScorer(params),
                LegComplexityScorer(params),
                BeenThisWayBeforeScorer(params),
                DidntMoveScorer(params),
                LastControlNearTheFinishScorer(params),
                DogLegScorer(params)
        )

        val csf = GhWrapper.initGH("NG86BA")
        //val csf = GhWrapper.initGH("S403DF")
        val scorer = CourseScorer(csf, featureScorers,params)

        val problem = CourseFinder(csf, scorer, params)
        val solver = Solver(problem, ExponentialDecayScheduler(1000.0, 1000))
        val solution = solver.solve()

        val best = csf.routeRequest(GHRequest(solution.controls)).best


        GpxWriter().writeToFile(solution.controls, best, "jon.gpx")
        MapPrinter(params).generatePDF(filename = "Map-${Date().time}.pdf", title = "Test+${(best.distance/1000).toInt()}K+${params.points -2 }+Controls", points = solution.controls)
        println()
        println("Hit: ${csf.hit}, Miss: ${csf.miss}, Bad: ${problem.bad}")
        println(best.distance)
        println(solution.controls.size)
        println("Energy: ${problem.energy(solution)}")
        println("Scores: ${solution.numberedControlScores.joinToString(", ")}")

        featureScorers.map {
            it::class.java.simpleName
        }.zip(solution.featureScores!!).forEach { println(it) }
    }
}
