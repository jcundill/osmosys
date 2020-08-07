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

import org.osmosys.annealing.SearchState
import org.osmosys.improvers.TSP

class CourseImprover(private val csf: ControlSiteFinder, val course: Course) : SearchState<CourseImprover> {

    private val noChoicePicker = ControlPickingStrategies::pickRandomly
    private val hasChoicePicker = ControlPickingStrategies::pickAboveAverage
    private val tsp = TSP(csf)

    override fun step(): CourseImprover {
        val numberedControlsToChange = findIndexesOfWorst(course.legScores, course.controls.size / 3)
        val newCourse = replaceSelectedNumberedControls(numberedControlsToChange, course.controls)
        return CourseImprover(csf, course.copy(controls = newCourse))
    }

    fun replaceSelectedNumberedControls(selected: List<Int>, existing: List<ControlSite>): List<ControlSite> =
            selected.fold(existing) { current, ctrl ->
                current.subList(0, ctrl) +
                        listOf(csf.findAlternativeControlSiteFor(current[ctrl])) +
                        current.subList(ctrl + 1, current.size)
            }

    /**
     * find some of the numbered controls that we would like to reposition
     * @param num - how many to choose to reposition
     * @param legScores - the scores allocated to each leg
     *
     * @return a list in the range 1 .. last numbered control of size num, or less if there aren't num to select
     */
    fun findIndexesOfWorst(legScores: List<Double>, num: Int): List<Int> {
        val controlScores = scoreControls(legScores)
        return when {
            allTheSameScore(controlScores) -> noChoicePicker(controlScores, num)
            else -> hasChoicePicker(controlScores, num)

        }
    }

    private fun scoreControls(legScores: List<Double>): List<Double> {
        val apartFromRunIn = legScores.dropLast(1)
        return if( legScores.last() > apartFromRunIn.last()) apartFromRunIn.dropLast(1) + legScores.last()
                else apartFromRunIn
    }

    /**
     * do all the numbered controls have the same score?
     */
    fun allTheSameScore(scores: List<Double>): Boolean {
        return scores.all { it == scores[0] }
    }

    override fun equals(other: Any?): Boolean {
        return when (other) {
            is CourseImprover -> other.course.controls == this.course.controls
            else -> false
        }
    }

    override fun hashCode(): Int {
        return 31 * course.controls.hashCode()
    }
}


