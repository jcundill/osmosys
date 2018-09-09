package jon.test.improvers

import com.graphhopper.util.DistancePlaneProjection
import com.graphhopper.util.shapes.GHPoint
import jon.test.annealing.LinearDecayScheduler
import jon.test.annealing.Problem
import jon.test.annealing.SearchState
import jon.test.annealing.Solver
import jon.test.rnd

val dist2d = DistancePlaneProjection()
fun dist(a: GHPoint, b: GHPoint): Double = dist2d.calcDist(a.lat, a.lon, b.lat, b.lon)

fun courseDistance(points: List<GHPoint>): Double {
    return points.windowed(2, 1, false).fold(0.0) { acc, curr ->
        acc + dist(curr[0], curr[1])
    }
}

class TSP(val points: List<GHPoint>) {


    fun run():List<GHPoint>? {
        val solver = Solver(TSProblem(points), LinearDecayScheduler(courseDistance(points), 1000))
        val solution = solver.solve()
        return solution.points
    }

    class TSProblem(val points: List<GHPoint>) : Problem<RouteImprover> {


        override fun energy(searchState: RouteImprover): Double {
            return when (searchState) {
                null -> 100000.0
                else -> {
                    val points = searchState.points!!
                    courseDistance(points)
                }
            }
        }


        override fun initialState(): RouteImprover = RouteImprover(points)
    }

    class RouteImprover(val points: List<GHPoint>?) : SearchState<RouteImprover> {
        private fun idxSeq()  = generateSequence { 1 + (rnd.nextDouble() * (points!!.size - 3)).toInt() }.asSequence()

        override fun step(): RouteImprover {
            val pair = randomPair() // don't want start or finish
            return RouteImprover(swap(points!!, pair))
        }

        private fun randomPair(): Pair<Int, Int> {
            val a = idxSeq().take(1).first()
            val b = idxSeq().dropWhile { it == a }.take(1).first()

            return Pair(a, b)
        }

        private fun swap(list: List<GHPoint>, pair: Pair<Int, Int>): List<GHPoint> {
            val first = list[pair.first]
            val second = list[pair.second]
            val array = list.toTypedArray()
            array[pair.first] = second
            array[pair.second] = first
            return array.toList()
        }

    }
}