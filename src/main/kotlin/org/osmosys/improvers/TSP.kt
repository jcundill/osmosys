/*
 *
 *     Copyright (c) 2017-2020 Jon Cundill.
 *
 *     Permission is hereby granted, free of charge, to any person obtaining
 *     a copy of this software and associated documentation files (the "Software"),
 *     to deal in the Software without restriction, including without limitation
 *     the rights to use, copy, modify, merge, publish, distribute, sublicense,
 *     and/or sell copies of the Software, and to permit persons to whom the Software
 *     is furnished to do so, subject to the following conditions:
 *
 *     The above copyright notice and this permission notice shall be included in
 *     all copies or substantial portions of the Software.
 *
 *     THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *     EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 *     OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 *     IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 *     CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 *     TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE
 *     OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 *
 */

package org.osmosys.improvers

import com.graphhopper.util.DistancePlaneProjection
import com.graphhopper.util.shapes.GHPoint
import org.osmosys.ControlSite
import org.osmosys.ControlSiteFinder
import org.osmosys.annealing.LinearDecayScheduler
import org.osmosys.annealing.Problem
import org.osmosys.annealing.SearchState
import org.osmosys.annealing.Solver
import org.osmosys.rnd
import kotlin.math.roundToInt

val dist2d = DistancePlaneProjection()
fun dist(a: GHPoint, b: GHPoint): Double = dist2d.calcDist(a.lat, a.lon, b.lat, b.lon)

//fun courseDistance(points: List<GHPoint>): Double {
//    return points.windowed(2, 1, false).fold(0.0) { acc, curr ->
//        acc + dist(curr[0], curr[1])
//    }



class TSP(private val csf: ControlSiteFinder) {

    private fun sensitivity(numControls: Int) : Int = (10000 * numControls / 10.0).roundToInt()
    private fun courseDistance(points: List<ControlSite>): Double {
        return points.windowed(2, 1, false).fold(0.0) { acc, curr ->
            acc + dist(curr[0].position, curr[1].position)
        }
    }

    fun run(points: List<ControlSite>): List<ControlSite> {
        val steps =  sensitivity(points.size)
        val solver = Solver(TSProblem(this::courseDistance, points), LinearDecayScheduler(courseDistance(points), steps))
        val solution = solver.solve()
        return solution.points
    }

    class TSProblem(val courseDistance :(List<ControlSite>)->Double, val points: List<ControlSite>) : Problem<RouteImprover> {


        override fun energy(searchState: RouteImprover): Double = courseDistance(searchState.points)


        override fun initialState(): RouteImprover = RouteImprover(points)
    }

    class RouteImprover(val points: List<ControlSite>) : SearchState<RouteImprover> {
        private fun idxSeq() = generateSequence { 1 + (rnd.nextDouble() * (points.size - 3)).toInt() }.asSequence()

        override fun step(): RouteImprover {
            val pair = randomPair() // don't want start or finish
            return RouteImprover(swap(points, pair))
        }

        private fun randomPair(): Pair<Int, Int> {
            val a = idxSeq().take(1).first()
            val b = idxSeq().dropWhile { it == a }.take(1).first()

            return Pair(a, b)
        }

        private fun swap(list: List<ControlSite>, pair: Pair<Int, Int>): List<ControlSite> {
            val first = list[pair.first]
            val second = list[pair.second]
            val array = list.toTypedArray()
            array[pair.first] = second
            array[pair.second] = first
            return array.toList()
        }

    }
}