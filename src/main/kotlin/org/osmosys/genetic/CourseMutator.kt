package org.osmosys.genetic

import io.jenetics.AnyGene
import io.jenetics.Mutator
import org.osmosys.*
import java.util.*

class CourseMutator(
    val csf: ControlSiteFinder,
    private val probability: Double
): Mutator<AnyGene<List<ControlSite>>, Double>(probability) {
    override fun mutate(gene: AnyGene<List<ControlSite>>?, random: Random?): AnyGene<List<ControlSite>> {
        return if (gene != null) {
            val course = gene.allele()
            val newCourse = mutateCourse(course, random!!, this.probability)
            gene.newInstance(newCourse)
        } else {
            super.mutate(gene, random)
        }
    }

    private fun randomMutate(controls: List<ControlSite>, random: Random, probability: Double):List<ControlSite> {
        return controls.map {ctrl ->
            if( random.nextDouble() < probability) csf.findAlternativeControlSiteFor(ctrl)
            else ctrl
        }
    }

    private fun mutateCourse(controls: List<ControlSite>, random: Random, probability: Double): List<ControlSite> {
        return listOf(controls.first()) +
                randomMutate(controls.drop(1).dropLast(1), random, probability) +
                controls.last()
    }

}

