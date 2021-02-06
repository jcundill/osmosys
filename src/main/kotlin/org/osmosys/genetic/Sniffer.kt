package org.osmosys.genetic

import io.jenetics.AnyGene
import io.jenetics.engine.EvolutionResult
import org.osmosys.Course
import java.util.function.Consumer

class Sniffer<G> : Consumer<EvolutionResult<AnyGene<G>, Double>> {
    override fun accept(t: EvolutionResult<AnyGene<G>, Double>) {
        println("Generation: ${t.generation()}, Best: ${t.bestFitness()}, Altered: ${t.alterCount()}, Invalid: ${t.invalidCount()}")
    }

}
