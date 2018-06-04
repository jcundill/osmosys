package jon.test

import com.graphhopper.GHResponse
import jon.test.scorers.FeatureScorer

typealias ControlScoreList = List<Double>
typealias FeatureScoreList = List<Double>

class CourseScorer(private val csf: ControlSiteFinder, private val featureScorers: List<FeatureScorer>, private val params: CourseParameters) {

    fun score(step: CourseImprover, courseRoute: GHResponse): Double {
        // score all the legs individually
        // needed currently for alternative routes
        val legs = step.controls.windowed(2, 1, false)
        val legRoutes = legs.map { ab -> csf.findRoutes(ab.first(), ab.last()) }
        val featureScores: List<ControlScoreList> = featureScorers.map {
            it.score(legRoutes, courseRoute)
        }

        /*
                featureScores =
                        1       2       3       4       5       6
                FS1     0.1     0.2     0.1     0.1     0.5     0.1
                FS2     0.2     0.1     0.1     0.4     0.3     0.0
                FS3     0.3     0.1     0.2     0.0     0.0     0.4

                step.numberedControlScores = 0.2, 0.167, 0.167, 0.167, 0.267, 0.167
                featureScores =
         */
        val numberedControlScores: List<FeatureScoreList> = transpose(featureScores)

        step.numberedControlScores = numberedControlScores.map { it.sum() / featureScorers.size }
        step.featureScores = featureScores
        return step.numberedControlScores.average()
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