package org.osmosys.genetic

import org.osmosys.*
import org.osmosys.constraints.CourseLengthConstraint
import org.osmosys.constraints.IsRouteableConstraint
import org.osmosys.constraints.MustVisitWaypointsConstraint
import org.osmosys.constraints.PrintableOnMapConstraint
import org.osmosys.improvers.TSP
import org.osmosys.mapping.MapFitter
import org.osmosys.scorers.*

class GA(val csf: ControlSiteFinder, val popSize: Int,  val initial: List<ControlSite>, val numControls: Int, val distance: Double) {
    val population = mutableListOf<Course>()

    var currIter = 0

    val fitter = MapFitter()
    val seeder = CourseSeeder(csf)

    val constraints = listOf(
            IsRouteableConstraint(),
            CourseLengthConstraint(distance),
            PrintableOnMapConstraint(fitter),
            MustVisitWaypointsConstraint(initial.drop(1).dropLast(1))
    )

    private val featureScorers = listOf(
            LegLengthScorer(),
            LegRouteChoiceScorer(),
            LegComplexityScorer(),
            BeenThisWayBeforeScorer(),
            ComingBackHereLaterScorer(),
            OnlyGoToTheFinishAtTheEndScorer(),
            DidntMoveScorer(),
            LastControlNearTheFinishScorer(),
            DogLegScorer()
    )
    private val courseScorer = CourseScorer(featureScorers, csf::findRoutes)
    private val tsp = TSP(csf)


    fun run(): Course {
        var offspringPopulation: List<Course>
        var matingPopulation: List<Course>

        createInitialPopulation()
        evaluatePopulation(population)
        initProgress()
        while (!isStoppingConditionReached()) {
            matingPopulation = selection(population)
            offspringPopulation = reproduction(matingPopulation)
            evaluatePopulation(offspringPopulation)
            replacement(population, offspringPopulation)
            updateProgress()
        }
        return population.minBy { it.energy }!!
    }

    private fun replacement(population: MutableList<Course>, offspringPopulation: List<Course>) {
        val sortedPopulation = population.sortedBy { it.energy }
        val sortedOffspring = offspringPopulation.sortedBy { it.energy }
        val worsts = sortedPopulation.takeLast(offspringPopulation.size)
        worsts.indices.forEach { i ->
            if (sortedOffspring[i].energy < worsts[i].energy) {
                population.remove(worsts[i])
                population.add(sortedOffspring[i])
            }
        }

    }

    var lastEnergy = 1000.0
    private fun updateProgress() {
        currIter += 1
        val currEnergy = population.minBy { it.energy }!!.energy
        if( currEnergy < lastEnergy) {
            lastEnergy = currEnergy
            println("iteration: " + currIter + " enery: " + currEnergy)
        }

    }

    private fun reproduction(matingPopulation: List<Course>): List<Course> {

        val parents = listOf(Pair(matingPopulation[0], matingPopulation[1]),
                Pair(matingPopulation[0], matingPopulation[2]),
                Pair(matingPopulation[0], matingPopulation[3]),
                Pair(matingPopulation[1], matingPopulation[2]),
                Pair(matingPopulation[1], matingPopulation[3]),
                Pair(matingPopulation[2], matingPopulation[3])
        )

        return parents.map { p ->
            mutate(crossover(p.first, p.second))
        }
    }

    private fun mutate(course: Course): Course {
       val mutated = course.controls.drop(1).dropLast(1).map { ctrl ->
            if( rnd.nextDouble() < 0.2) {
                csf.findAlternativeControlSiteFor(ctrl)
            } else {
                ctrl
            }
        }
        val newCtrls = listOf(course.controls.first()) + mutated + course.controls.last()
        return Course(course.distance(), course.requestedNumControls, newCtrls)
    }

    private fun crossover(first: Course, second: Course): Course {
        val newCtrls = mutableListOf<ControlSite>()
        newCtrls.addAll(first.controls)
        var reroute = false
        // find the worst legs in replace with the best legs in b
        first.legScores.zip(second.legScores).forEachIndexed { index, pair ->
            if( pair.first > pair.second ) {
                newCtrls[index] = second.controls[index]
                newCtrls[index+1] = second.controls[index+1]
                reroute =true
            }
        }
        val ctrls = if(reroute) {
            tsp.run(newCtrls)
        } else {
            newCtrls
        }
        return Course(first.distance(), first.requestedNumControls, ctrls)
    }

    private fun selection(population: List<Course>): List<Course> {
        return population.sortedBy { it.energy }.take(4)
    }

    private fun isStoppingConditionReached(): Boolean {
        return currIter > 1000
    }

    private fun initProgress() {
        println("Initial population established")
    }

    private fun evaluatePopulation(population: List<Course>) {
        population.forEach { course ->
            val courseRoute = csf.routeRequest(course.controls)
            course.route = courseRoute.best
            val score =
                    if (constraints.any { !it.valid(courseRoute) }) 10000.0
                    else courseScorer.score(course) * 1000

            course.energy = score
            course.route = courseRoute.best

        }
    }

    private fun createInitialPopulation() {
        while (population.size < popSize) {
            val ctrls = seeder.chooseInitialPoints(initial, numControls, distance)
            val course = Course(distance, numControls, ctrls)
            val route = csf.routeRequest(ctrls)
            if( constraints.map { it.valid(route) }.all { it }) {
                course.route = route.best
                population.add(course)
                println("pop: " + population.size)
            }
        }
    }
}