package org.osmosys.genetic

import io.jenetics.AnyGene
import io.jenetics.UniformCrossover
import io.jenetics.util.MSeq
import org.osmosys.*
import org.osmosys.improvers.TSP

class ControlSiteSwapper(val csf: ControlSiteFinder, private val crossoverProbability: Double) :
    UniformCrossover<AnyGene<List<ControlSite>>, Double>(crossoverProbability) {

    private val tsp = TSP(csf)

    override fun crossover(that: MSeq<AnyGene<List<ControlSite>>>, other: MSeq<AnyGene<List<ControlSite>>>): Int {
        val thatCourse = that.first().allele()
        val otherCourse = other.first().allele()

        val swapped = randomCrossover(thatCourse, otherCourse)
        that.first().newInstance(swapped.first)
        other.first().newInstance(swapped.second)
        return 2
    }

    private fun randomCrossover(first: List<ControlSite>, second: List<ControlSite>) : Pair<List<ControlSite>, List<ControlSite>> {
        val newFirsts = mutableListOf<ControlSite>()
        newFirsts.addAll(first)
        val newSeconds = mutableListOf<ControlSite>()
        newSeconds.addAll(second)
        newFirsts.forEachIndexed { index, controlSite ->
            if( controlSite != newFirsts.first() && controlSite != newFirsts.last()){
                if( rnd.nextDouble() < crossoverProbability) {
                    val a = newFirsts[index]
                    val b = newFirsts[index + 1]
                    newFirsts[index] = newSeconds[index]
                    newFirsts[index+1] = newSeconds[index+1]
                    newSeconds[index] = a
                    newSeconds[index+1] = b
                }
            }
        }
        return Pair(tsp.run(newFirsts), tsp.run(newSeconds))
    }
}
