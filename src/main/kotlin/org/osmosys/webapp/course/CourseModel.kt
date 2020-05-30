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

package org.osmosys.webapp.course

import com.fasterxml.jackson.annotation.JsonProperty
import com.graphhopper.util.shapes.GHPoint
import org.osmosys.Course

fun GHPoint.toLatLng() = arrayOf(this.lat, this.lon)

data class CoursePrintRequestModel(@JsonProperty val controls: List<Array<Double>>? = emptyList(),
                                   @JsonProperty( required = false) val filename: String = "StreetOMap.pdf")

data class CourseModel(@JsonProperty(required = false) val requestedDistance: Double?,
                       @JsonProperty(required = false) val actualDistance: Double? = requestedDistance,
                       @JsonProperty val numControls: Int,
                       @JsonProperty val controls: List<Array<Double>>,
                       @JsonProperty(required = false) val score: Double = 1000.0,
                       @JsonProperty(required = false) val controlScores: List<Double> = emptyList(),
                       @JsonProperty(required = false) val featureScores: Map<String, List<Double>> = emptyMap(),
                       @JsonProperty(required = false) val route: List<Array<Double>> = emptyList()) {
    companion object {
        fun buildFrom(course: Course): CourseModel {
            return CourseModel(
                requestedDistance = course.distance(),
                actualDistance = course.route.distance,
                numControls = course.controls.size - 2,
                controls = course.controls.map { arrayOf(it.position.lat, it.position.lon) },
                score = course.energy,
                controlScores = course.legScores,
                featureScores = course.featureScores,
                route = course.route.points.map{ it.toLatLng()}
            )
        }

    }
}

data class CourseScoreResponseModel(@JsonProperty(required = true) val numberedControlScores: List<Double>) {
    companion object {
        fun buildFrom(course: Course): CourseScoreResponseModel {
            return CourseScoreResponseModel( numberedControlScores = course.legScores )
        }

    }
}

data class CourseScoreRequestModel(@JsonProperty(required = true) val requestedLength: Double,
                                   @JsonProperty(required = true) val controls: List<List<Double>>)