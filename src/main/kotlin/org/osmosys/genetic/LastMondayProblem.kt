package org.osmosys.genetic

import io.jenetics.AnyChromosome
import io.jenetics.AnyGene
import io.jenetics.Genotype
import io.jenetics.Phenotype
import io.jenetics.engine.Codec
import io.jenetics.engine.Constraint
import io.jenetics.engine.Problem
import io.jenetics.engine.RetryConstraint
import org.osmosys.*
import org.osmosys.constraints.*
import org.osmosys.mapping.MapFitter
import org.osmosys.scorers.*
import java.util.*
import java.util.function.Function

class LastMondayProblem(
    val csf: ControlSiteFinder,
    private val initialCourse: Course)
    : Problem<List<ControlSite>, AnyGene<List<ControlSite>>, Double> {

    val constraints = listOf(
        IsRouteableConstraint(),
        CourseLengthConstraint(initialCourse.distance()),
        PrintableOnMapConstraint(MapFitter()),
        MustVisitWaypointsConstraint(initialCourse.controls.drop(1).dropLast(1)),
        LastControlNearTheFinishConstraint(),
        DidntMoveConstraint(),
        OnlyGoToTheFinishAtTheEndConstraint()
    )

    private val featureScorers = listOf(
        LegLengthScorer(),
        LegComplexityScorer(),
        LegRouteChoiceScorer(),
        BeenThisWayBeforeScorer(),
        ComingBackHereLaterScorer(),
        DogLegScorer()
    )

    private val courseScorer = CourseScorer(featureScorers, csf::findRoutes)

    private val seeder = CourseSeeder(csf)


    override fun codec(): Codec<List<ControlSite>, AnyGene<List<ControlSite>>> {
        return Codec.of(
            Genotype.of(AnyChromosome.of(::nextRandomCourse)),
            { gt -> gt.gene().allele() }
        )
    }

    override fun fitness(): Function<List<ControlSite>, Double> {
        return Function { controls -> courseFitness(controls) }
    }

    override fun constraint(): Optional<Constraint<AnyGene<List<ControlSite>>, Double>> {
        return Optional.of(RetryConstraint.of(::courseValidator))
    }

    private val validatedSet = mutableMapOf<Int, Boolean>()

    private fun courseValidator(pt: Phenotype<AnyGene<List<ControlSite>>, Double>): Boolean {
        val controls = pt.genotype().gene().allele()
        val hashCode = controls.hashCode()
        if( !validatedSet.containsKey(hashCode)) {
            val route = csf.routeRequest(controls)
            val ok = constraints.all { it.valid(route) }
            validatedSet[hashCode] = ok
        }
        return validatedSet[hashCode]!!
     }

    private fun nextRandomCourse(): List<ControlSite> {
        return seeder.chooseInitialPoints(initialCourse.controls, initialCourse.requestedNumControls, initialCourse.requestedDistance!!)
     }

    private fun courseFitness(controls: List<ControlSite>): Double {
        val legScores = courseScorer.score(controls).second
        val average = legScores.map { scores -> scores.average() }.average()
        return 1.0 - average
    }


}

