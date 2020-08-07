package org.osmosys.seeders

import org.osmosys.ControlSite

interface SeedingStrategy {
    fun seed(initialPoints: List<ControlSite>, requestedNumControls: Int, requestedCourseLength: Double): List<ControlSite>
}