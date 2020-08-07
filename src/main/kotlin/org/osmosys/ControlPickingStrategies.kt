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


/**
 * here we are dealing with the numbered controls so should answer in the range 1 to the last control.
 * i.e. if there are 10 point on the course - start and finish and 8 controls
 * the we are given a list of 8
 *
 * if we don't like the first one in the list and we only have to choose 1 then we should return 1 rather than 0
 */
object ControlPickingStrategies {

    fun pickRandomly(numberedControlScores: List<Double>, num: Int): List<Int> =
            pick(numberedControlScores, num) { _ -> rnd.nextDouble() > 0.5 }

    fun pickWeightedRandom(numberedControlScores: List<Double>, num: Int): List<Int> {
        val prob = rnd.nextDouble() / numberedControlScores.size
        return pick(numberedControlScores, num) { x -> x.second > prob }
    }

    fun pickAboveAverage(numberedControlScores: List<Double>, num: Int): List<Int> {
        val mean =  numberedControlScores.average()
        return pick(numberedControlScores, num) { x -> x.second > mean }
    }

    fun pickWorstAndNextIfAboveAverage(numberedControlScores: List<Double>, num: Int): List<Int> {
        val mean = numberedControlScores.average()
        val worsts = pick(numberedControlScores, num) { x -> x.second > mean }
        val options = worsts.takeWhile { x -> numberedControlScores[x - 1] == numberedControlScores[worsts.first() - 1] }
        return if (options.first() < numberedControlScores.size - 1 &&
                numberedControlScores[options.first() + 1] > mean)
            listOf(options.first(), options.first() + 1)
        else {
            options.take(1)
        }
    }

    fun pick(numberedControlScores: List<Double>, num: Int, selector: (Pair<Int, Double>) -> Boolean): List<Int> {
        val indexedControlScores = (1 .. (numberedControlScores.size)).zip(numberedControlScores)
        val choices = indexedControlScores.filter { selector(it) }.sortedByDescending { it.second }
        val selectedIndexes = choices.map { it.first }

        return when {
            selectedIndexes.size <= num -> selectedIndexes
            else -> selectedIndexes.take(num)
        }
    }
}