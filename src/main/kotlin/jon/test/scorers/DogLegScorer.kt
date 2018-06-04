package jon.test.scorers

import com.graphhopper.GHResponse
import jon.test.CourseParameters

class DogLegScorer(val params: CourseParameters) : FeatureScorer {

    /**
     * scores each numbered control based on the repetition of the route to it and the route from the previous control.
     * i.e. control 3 is in a bad place as the route from 1 to 2 is pretty much the same as the route from 2 to 3
     */
    override fun score(routedLegs: List<GHResponse>, routedCourse: GHResponse): List<Double> {
        return dogLegs(routedLegs.map { it.best.points })
    }

    fun <T>dogLegs(routes: List<Iterable<T>>): List<Double> =
            when {
                routes.size < 2 -> emptyList()
                else -> listOf(0.0) + routes.windowed(2).map { dogLegScore(it.first().toList(), it.last().toList()) }.dropLast(1)
            }


    fun <T>dogLegScore(a2b: List<T>, b2c: List<T>): Double {
        if( a2b.size < 2 || b2c.size < 2) return 1.0 //controls are in the same place
        val numInAandB = a2b.dropLast(1).filter { b2c.drop(1).contains(it) }.size
        //val ratioAtoB =  numInAandB.toDouble() / (a2b.size.toDouble() - 1.0) // we dropped one
        val ratioBtoC = numInAandB.toDouble() / (b2c.size.toDouble() - 1.0)
        return ratioBtoC
    }
}