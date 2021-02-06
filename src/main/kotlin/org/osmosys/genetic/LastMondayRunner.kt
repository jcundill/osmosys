package org.osmosys.genetic

import io.jenetics.*
import io.jenetics.engine.Engine
import io.jenetics.engine.EvolutionStatistics
import io.jenetics.engine.Limits
import org.osmosys.ControlSiteFinder
import org.osmosys.Course
import java.util.stream.Collectors

class LastMondayRunner(val csf: ControlSiteFinder) {

    private val myAlterer = Alterer.of(
        ControlSiteSwapper(csf, Alterer.DEFAULT_ALTER_PROBABILITY),
        CourseMutator(csf, Alterer.DEFAULT_ALTER_PROBABILITY)
    )


    fun run(initialCourse: Course): Course {
        val engine = Engine
            .builder(LastMondayProblem(csf, initialCourse))
//            .survivorsSelector(EliteSelector())
//            .survivorsFraction(0.25)
//            .offspringSelector(ExponentialRankSelector())
//            .offspringFraction(0.25)
//            .populationSize(50)
            .alterers(myAlterer)
            .build()
        val statistics = EvolutionStatistics.ofNumber<Double>()
        val population = engine.stream()
            .limit(Limits.byFitnessThreshold(0.99))
            //.limit(Limits.byExecutionTime(Duration.ofSeconds(90)))
            .limit(Limits.byFixedGeneration(100))
            .peek(Sniffer())
            .peek(statistics)
            .distinct()
            .collect(Collectors.toList())

        val bests = population.sortedByDescending { it.bestFitness() }.map { it.bestPhenotype() }.distinctBy { it.genotype().gene().allele() }
        val best = bests.first()

        println(statistics)
        println("num generations: ${population.last().generation()}")
        println("best generation: ${best.generation()}")

        return when {
            best != null -> Course(initialCourse.requestedDistance, initialCourse.requestedNumControls, best.genotype().gene().allele())
            else -> initialCourse
        }

    }

}