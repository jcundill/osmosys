package jon.test

import com.graphhopper.GHRequest
import jon.test.scorers.*

class CourseScorer(private val csf: ControlSiteFinder, private val featureScorers: List<FeatureScorer>, private val params: CourseParameters) {

    fun score(step: CourseImprover): Double {
        // route the whole course
        val courseRoute = csf.routeRequest(GHRequest(step.controls))
        return when {
            courseRoute.hasErrors() -> 10000.0
            !csf.routeFitsBox(courseRoute.best.points, params.allowedBoxes) -> 10000.0
            else -> {
                // score all the legs individually
                // needed currently for alternative routes
                val legs = step.controls.windowed(2, 1, false)
                val legRoutes = legs.map{ ab -> csf.findRoutes(ab.first(), ab.last())}
                val numberedControlScores: List<List<Double>> = featureScorers.map { it.score(legRoutes, courseRoute) }

                val avs = numberedControlScores.map {it.average()} // don't include the start when seeing how this feature scored

                step.numberedControlScores = transpose(numberedControlScores).map {it.average()}
                step.featureScores = avs

                return avs.average()
            }
        }
    }

    /**
     * Returns a list of lists, each built from elements of all lists with the same indexes.
     * Output has length of shortest input list.
     */
    fun <T> transpose(lists: List<List<T>>): List<List<T>> {
        return transpose(lists, transform = { it })
    }

    /**
     * Returns a list of values built from elements of all lists with same indexes using provided [transform].
     * Output has length of shortest input list.
     */
    inline fun <T, V> transpose(lists: List<List<T>>, transform: (List<T>) -> V): List<V> {
        val minSize = lists.map(List<T>::size).min() ?: return emptyList()
        val list = ArrayList<V>(minSize)

        val iterators = lists.map { it.iterator() }
        var i = 0
        while (i < minSize) {
            list.add(transform(iterators.map { it.next() }))
            i++
        }

        return list
    }
}