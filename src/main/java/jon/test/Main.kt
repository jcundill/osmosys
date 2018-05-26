package jon.test

import com.graphhopper.util.shapes.GHPoint
import xyz.thepathfinder.simulatedannealing.ExponentialDecayScheduler
import xyz.thepathfinder.simulatedannealing.Solver


object Main {
    @JvmStatic
    fun main(args: Array<String>) {


        //val params = Params(distance = 8000.0, points = 8, start = GHPoint(52.988304, -1.203265))
        val params = Params(distance = 12000.0, points = 12, start = GHPoint(53.223482, -1.461053))

        //val gh = GhWrapper.initGH("NG86BA")
        val gh = GhWrapper.initGH("S403DF")
        val problem = GhProblem(gh, params)
        val solver = Solver(problem, ExponentialDecayScheduler(1000.0, 1000))
        val solution = solver.solve();

        val best = solution.response!!.best

        //GpxWriter().writeToFile(solution.points, best, "jon.gpx")
        //MapPrinter.generatePDF(filename = "jon.pdf", points = solution.points)
        println()
        println("Hit: ${problem.hit}, Miss: ${problem.miss}")
        println(best.distance)
        println(solution.points.size)
        println("Energy: ${problem.energy(solution)}")
    }
}
