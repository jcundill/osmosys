package jon.test.scorers

import com.graphhopper.GHResponse

interface FeatureScorer {
    fun score(legs: List<GHResponse>, course: GHResponse): List<Double>
}