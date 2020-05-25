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

import com.graphhopper.util.shapes.GHPoint
import org.osmosys.ControlSite
import org.osmosys.Course
import org.osmosys.Osmosys
import org.osmosys.mapping.MapFitter
import org.osmosys.mapping.MapPrinter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.ApplicationEventPublisherAware
import org.springframework.stereotype.Service
import java.io.File

@Service
class CourseService : ApplicationEventPublisherAware {

    private lateinit var publisher: ApplicationEventPublisher

    @Autowired
    lateinit var osmosys: Osmosys

    @Autowired
    lateinit var mapPrinter: MapPrinter

    @Autowired
    lateinit var fitter: MapFitter

    override fun setApplicationEventPublisher(applicationEventPublisher: ApplicationEventPublisher) {
        this.publisher = applicationEventPublisher
    }

    fun generate(initialCourse: Course): Course? {
        val problem = osmosys.makeProblem(initialCourse)
        return osmosys.findCourse(problem = problem)!!
    }

    fun printMap(ctrls: List<ControlSite>): ByteArray? {
        val best = osmosys.findBestRoute(ctrls)

        val envelopeToMap = osmosys.getEnvelopeForProbableRoutes(ctrls)
        mapPrinter.generateMapFiles(filename = "Map",
                title = "Test+${(best.distance / 1000).toInt()}K+${ctrls.size - 2}+Controls",
                controls = ctrls,
                centre = envelopeToMap.centre(),
                box = fitter.getForEnvelope(envelopeToMap)!!)

        return File("Map.pdf").readBytes()
    }

    fun score(ctrls: List<ControlSite>): Course {
        val course = Course(controls = ctrls)
        osmosys.score(course)
        return course
    }

    fun evaluateLeg(from: GHPoint, to: GHPoint): List<Pair<Double, List<GHPoint>>> {
        return osmosys.evaluateLeg(from, to)
    }
}