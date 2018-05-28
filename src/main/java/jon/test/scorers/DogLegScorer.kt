package jon.test.scorers

import com.graphhopper.GHResponse
import jon.test.CourseParameters

class DogLegScorer(val params: CourseParameters) : FeatureScorer {

    /**
     * scores each numbered control based on the repetition of the route to it and the route to the next control.
     * i.e. control 2 is in a bad place as the route from 1 to 2 is pretty much the same as the route from 2 to 3
     */
    override fun score(legs: List<GHResponse>, course: GHResponse): List<Double> {
        return dogLegs(legs.map { it.best.points })
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