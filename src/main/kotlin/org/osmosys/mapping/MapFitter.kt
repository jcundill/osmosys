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

package org.osmosys.mapping

import com.vividsolutions.jts.geom.Envelope

data class MapBox(val maxWidth: Double, val maxHeight: Double, val scale: Int, val landscape: Boolean)

/**
 * finds, from the available possibilities, the best map scale and orientation that would allow
 * the current course to be printed on a sheet of paper
 */
class MapFitter {

    private val landscape10000 = MapBox(
            maxWidth = 0.04187945854565189 * 0.9,
            maxHeight = 0.016405336634527146 * 0.9,
            scale = 10000,
            landscape = true
    )

    private val portrait10000 = MapBox(
            maxWidth = 0.02955457284753238 * 0.9,
            maxHeight = 0.024208663288803223 * 0.9,
            scale = 10000,
            landscape = false
    )

    private val landscape5000 = MapBox(
            maxWidth = landscape10000.maxWidth * 0.5,
            maxHeight = landscape10000.maxHeight * 0.5,
            scale = 5000,
            landscape = true
    )

    private val portrait5000 = MapBox(
            maxWidth = portrait10000.maxWidth * 0.5,
            maxHeight = portrait10000.maxHeight * 0.5,
            scale = 5000,
            landscape = false
    )
    private val landscape7500 = MapBox(
            maxWidth = landscape10000.maxWidth * 0.75,
            maxHeight = landscape10000.maxHeight * 0.75,
            scale = 7500,
            landscape = true
    )

    private val portrait7500 = MapBox(
            maxWidth = portrait10000.maxWidth * 0.75,
            maxHeight = portrait10000.maxHeight * 0.75,
            scale = 7500,
            landscape = false
    )
    private val landscape12500 = MapBox(
            maxWidth = landscape10000.maxWidth * 1.25,
            maxHeight = landscape10000.maxHeight * 1.25,
            scale = 12500,
            landscape = true
    )

    private val portrait12500 = MapBox(
            maxWidth = portrait10000.maxWidth * 1.25,
            maxHeight = portrait10000.maxHeight * 1.25,
            scale = 12500,
            landscape = false
    )

    private val landscape15000 = MapBox(
            maxWidth = landscape10000.maxWidth * 1.5,
            maxHeight = landscape10000.maxHeight * 1.5,
            scale = 15000,
            landscape = true
    )

    private val portrait15000 = MapBox(
            maxWidth = portrait10000.maxWidth * 1.5,
            maxHeight = portrait10000.maxHeight * 1.5,
            scale = 15000,
            landscape = false
    )

    private val possibleBoxes = listOf(
            landscape5000, portrait5000,
            landscape7500, portrait7500,
            landscape10000, portrait10000,
            portrait12500, landscape12500,
            portrait15000, landscape15000
    )

    /**
     * return the MapBox that would best enclose the points in the passed in envelope
     * or null if we don't have one
     */
    fun getForEnvelope(env: Envelope): MapBox? =
            possibleBoxes.find { env.width < it.maxWidth && env.height < it.maxHeight }

}