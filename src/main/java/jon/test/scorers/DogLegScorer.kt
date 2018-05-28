package jon.test.scorers

import com.graphhopper.GHResponse
import com.graphhopper.util.shapes.GHPoint3D
import jon.test.CourseParameters

class DogLegScorer(val params: CourseParameters) : FeatureScorer {
    override fun score(legs: List<GHResponse>, course: GHResponse): List<Double> {
        return listOf(0.0) + dogLegs<GHPoint3D?>(legs.map { it.best.points })
    }

    fun <T>dogLegs(routes: List<Iterable<T>>): List<Double> =
            routes.windowed(2, 1, false).map { dogLegScore(it.first().toList(), it.last().toList()) }

    fun <T>dogLegScore(a2b: List<T>, b2c: List<T>): Double {
        if( a2b.size < 2 || b2c.size < 2) return 1.0 //controls are in the same place
        val numInAandB = a2b.dropLast(1).filter { b2c.drop(1).contains(it) }.size
        val ratioAtoB =  numInAandB.toDouble() / (a2b.size.toDouble() - 1.0) // we dropped one
        val ratioBtoC = numInAandB.toDouble() / (b2c.size.toDouble() - 1.0)
        return Math.max(ratioAtoB, ratioBtoC)
    }
}