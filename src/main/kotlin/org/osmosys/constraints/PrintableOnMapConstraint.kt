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

package org.osmosys.constraints

import com.graphhopper.GHResponse
import com.graphhopper.PathWrapper
import com.vividsolutions.jts.geom.Envelope
import org.osmosys.mapping.MapFitter

class PrintableOnMapConstraint(val fitter: MapFitter) : CourseConstraint {
    val env = Envelope()

    override fun valid(routedCourse: GHResponse): Boolean {
        return routeFitsBox(routedCourse.all)
    }

    private fun routeFitsBox(routes: List<PathWrapper>): Boolean {
        env.setToNull()
        routes.forEach { pw -> pw.points.forEach { env.expandToInclude(it.lon, it.lat) } }
        return fitter.getForEnvelope(env) != null
    }


}