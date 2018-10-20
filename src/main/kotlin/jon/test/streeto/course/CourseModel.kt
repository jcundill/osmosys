package jon.test.streeto.course

import com.fasterxml.jackson.annotation.JsonProperty
import com.graphhopper.util.shapes.GHPoint
import jon.test.Course

fun GHPoint.toLatLng(): LatLng = LatLng(this.lat, this.lon)

data class LatLng(@JsonProperty val lat: Double, @JsonProperty val lng: Double) {
    fun toGHPoint() = GHPoint(lat, lng)
}

data class CourseModel(@JsonProperty val requestedDistance: Double,
                       @JsonProperty(required = false) val actualDistance: Double = requestedDistance,
                       @JsonProperty val numControls: Int,
                       @JsonProperty val controls: List<LatLng>,
                       @JsonProperty(required = false) val score: Double = 1000.0,
                       @JsonProperty(required = false) val controlScores: List<Double> = emptyList(),
                       @JsonProperty(required = false) val featureScores: Map<String, List<Double>> = emptyMap(),
                       @JsonProperty(required = false) val route: List<LatLng> = emptyList()) {
    companion object {
        fun buildFrom(course: Course): CourseModel {
            return CourseModel(
                requestedDistance = course.requestedDistance,
                actualDistance = course.route.distance,
                numControls = course.controls.size - 2,
                controls = course.controls.map { it.toLatLng() },
                score = course.energy,
                controlScores = course.numberedControlScores,
                featureScores = course.featureScores,
                route = course.route.points.map{ it.toLatLng()}
            )
        }

    }
}