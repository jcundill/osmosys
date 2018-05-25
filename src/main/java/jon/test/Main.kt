package jon.test

import com.graphhopper.util.shapes.GHPoint
import xyz.thepathfinder.simulatedannealing.*


object Main {
    @JvmStatic
    fun main(args: Array<String>) {

        val params = Params(distance = 8000.0, points = 20, start = GHPoint(52.988304, -1.203265))

        val problem = GhProblem(GhWrapper.initGH(), params)
        val solution = Solver(problem, ExponentialDecayScheduler(1000.0, 1000)).solve()

        val best = solution.response!!.best

        GpxWriter().writeToFile(solution.points, best, "jon.gpx")
        println(best.distance)
        println(solution.points.size)
    }
}
