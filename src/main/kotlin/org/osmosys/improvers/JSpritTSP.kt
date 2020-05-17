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

import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm
import com.graphhopper.jsprit.core.algorithm.box.Jsprit
import com.graphhopper.jsprit.core.algorithm.state.StateManager
import com.graphhopper.jsprit.core.problem.Location.Builder
import com.graphhopper.jsprit.core.problem.Location.newInstance
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem
import com.graphhopper.jsprit.core.problem.constraint.ConstraintManager
import com.graphhopper.jsprit.core.problem.job.Pickup
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl
import com.graphhopper.jsprit.core.problem.vehicle.VehicleType
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl
import com.graphhopper.jsprit.core.util.Coordinate
import com.graphhopper.jsprit.core.util.Solutions
import com.graphhopper.jsprit.core.util.VehicleRoutingTransportCostsMatrix
import com.graphhopper.util.shapes.GHPoint
import org.osmosys.ControlSiteFinder

data class DistResult(val from: String, val to: String, val dist: Double)

class JSpritTSP(private val csf: ControlSiteFinder) {

    private fun buildAlgorithm(vrp: VehicleRoutingProblem?, costs: VehicleRoutingTransportCostsMatrix?): VehicleRoutingAlgorithm {
        val vraBuilder = Jsprit.Builder.newInstance(vrp)

        val stateManager = StateManager(vrp) //head of development - upcoming release (v1.4)
        val constraintManager = ConstraintManager(vrp, stateManager);

        vraBuilder.addCoreStateAndConstraintStuff(true)
        vraBuilder.setStateAndConstraintManager(stateManager, constraintManager);

        //algorithm.maxIterations = 5000
        return vraBuilder.buildAlgorithm()
    }

    private fun calcDistances(index: Int = 0, boxes: List<GHPoint>): List<DistResult> {
        if (boxes.isEmpty()) return emptyList()
        return calcBoxDistances(index, boxes.first(), boxes.drop(1)) + calcDistances(index+1, boxes.drop(1))
    }

    private fun calcBoxDistances(index : Int, box: GHPoint, rest: List<GHPoint>): List<DistResult> {
        return rest.mapIndexed { bIdx, b ->
            val aName = index.toString()
            val bName = (index+bIdx+1).toString()
            DistResult(aName, bName, dist(box, b)) }
    }

    fun run(controls: List<GHPoint>): List<GHPoint> {

        val points = controls.dropLast(1)
        val jobs = points.mapIndexed { index, ghPoint ->
            val pub = Pickup.Builder.newInstance(index.toString())
            val lb = Builder.newInstance()
            lb.setId(index.toString())
            lb.setName(index.toString());
            lb.setIndex(index)
            lb.setCoordinate(Coordinate(ghPoint.lat, ghPoint.lon))
            lb.setUserData(Coordinate(ghPoint.lat, ghPoint.lon))
            pub.setLocation(lb.build())
            pub.setUserData(ghPoint)
            pub.build()
        }

        val dists = calcDistances(0, points)
        val costsBuilder = VehicleRoutingTransportCostsMatrix.Builder.newInstance(true)
        dists.forEach {
            costsBuilder.addTransportDistance(it.from, it.to, it.dist)
        }
        val costs = costsBuilder.build()

        val vehicleTypeBuilder = VehicleTypeImpl.Builder.newInstance("vehicleType").addCapacityDimension(0, 10000)
        val vehicleType: VehicleType = vehicleTypeBuilder.build()

        val runner = VehicleImpl.Builder.newInstance("runner")
                .setStartLocation(newInstance("0"))
                .setType(vehicleType)
                .setReturnToDepot(true).build()

        val vrp = VehicleRoutingProblem.Builder.newInstance()
                .addVehicle(runner)
                .setRoutingCost(costs)
                .setFleetSize(VehicleRoutingProblem.FleetSize.FINITE)
                .addAllJobs(jobs).build()

        val bestSolution = Solutions.bestOf(buildAlgorithm(vrp, costs).searchSolutions())
        print(".")
        return bestSolution.routes.first().activities.map { it ->
            val latlon = it.location.coordinate
            GHPoint(latlon.x, latlon.y)
        }
    }
}
