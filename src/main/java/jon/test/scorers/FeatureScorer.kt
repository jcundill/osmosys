package jon.test.scorers

import com.graphhopper.GHResponse

interface FeatureScorer {
    /**
     * returns the points awarded to each numbered control
     * i.e. not the start/finish
     * so the returned list is 2 less than the number of controls
     * and score[0] refers to control numbered 1
     */
    fun score(legs: List<GHResponse>, course: GHResponse): List<Double>
}