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

package org.osmosys.csv

import org.osmosys.Course
import java.io.File
import java.math.BigDecimal
import java.math.RoundingMode

class ScoreWriter {
    fun writeScores(course: Course, filename: String) {
        with(File(filename)) {
            with(course) {
                val featureList = featureScores.keys.toList()
                val titles = getTitles(featureList) + "\n"
                val legs = (1 until controls.size).map{
                    getLegDetails(it, legScores[it-1], featureList, featureScores)
                }
                writeText( titles + legs.joinToString("\n"))
            }
        }
    }
    private fun getLegDetails(leg: Int, score: Double, featureList: List<String>, featureScores: Map<String, List<Double>>): String {
        return (listOf("$leg", "${scoreFormatter(score)}") + featureList.map { f -> scoreFormatter((featureScores[f] ?: error(""))[leg-1])}).joinToString()
    }

    private fun getTitles(features: List<String>): String {
        return (listOf("Leg","Score") + features).joinToString()
    }

    private fun scoreFormatter(it: Double) = BigDecimal(1.0 - it).setScale(2, RoundingMode.HALF_EVEN)

}