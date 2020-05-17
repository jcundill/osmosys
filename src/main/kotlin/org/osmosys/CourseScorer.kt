/*
 *
 *     Copyright (c) 2017-2020 Jon Cundill.
 *
 *     Permission is hereby granted, free of charge, to any person obtaining
 *     a copy of this software and associated documentation files (the "Software"),
 *     to deal in the Software without restriction, including without limitation
 *     the rights to use, copy, modify, merge, publish, distribute, sublicense,
 *     and/or sell copies of the Software, and to permit persons to whom the Software
 *     is furnished to do so, subject to the following conditions:
 *
 *     The above copyright notice and this permission notice shall be included in
 *     all copies or substantial portions of the Software.
 *
 *     THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *     EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 *     OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 *     IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 *     CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 *     TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE
 *     OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 *
 */

package org.osmosys

import com.graphhopper.GHResponse
import com.graphhopper.util.shapes.GHPoint
import org.osmosys.scorers.*

typealias LegScoreList = List<Double>
typealias FeatureScoreList = List<Double>

class CourseScorer(private val legScorers: List<LegScorer>, private val findRoutes: (GHPoint, GHPoint) -> GHResponse) {

    fun score(step: Course): Double {
        // score all the legs individually
        // needed currently for alternative routes
        val legs = step.controls.windowed(2)
        val legRoutes = legs.map { ab -> findRoutes(ab.first(), ab.last()) }
        val featureScores: List<LegScoreList> = legScorers.map { raw ->
            raw.score(legRoutes).map {s -> s * raw.weighting}
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
        val legScores: List<FeatureScoreList> = transpose(featureScores)
        step.legScores = legScores.map{ it.sum()/legScorers.size}
         step.featureScores = getDetailedScores(featureScores)
        return step.legScores.average()
    }

    private fun getDetailedScores(featureScores: List<List<Double>>): Map<String, List<Double>> {
        return legScorers.map {
            it::class.java.simpleName
        }.zip(featureScores).toMap()
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