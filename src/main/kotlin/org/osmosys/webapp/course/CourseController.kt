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
import org.osmosys.Course
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

typealias LatLon = Array<Double>

@RestController
class CourseController {

    @Autowired
    lateinit var courseService: CourseService

    @GetMapping("/create")
    fun create(@RequestParam(required = false) distance: Int?,
                @RequestParam(required = true) numControls: Int,
                @RequestParam(required = true) latlons: String
               ): CourseModel {

        val initialControls = latlons.split("|")
                .map { it.split(",")}
                .map{ s -> GHPoint(s.first().toDouble(), s.last().toDouble())}

        val initialCourse = Course(
                requestedDistance = distance?.toDouble(),
                requestedNumControls = numControls,
                controls = initialControls
        )
        val solution = courseService.generate(initialCourse)

        return CourseModel.buildFrom(solution!!)
    }

//    @RequestMapping("/scoreCourse")
//    fun score(@RequestBody(required = true) model: CourseScoreRequestModel): CourseScoreResponseModel {
//        val scoredCourse =  courseService.score(model.controls.map { it.toGHPoint()})
//        return CourseScoreResponseModel.buildFrom(scoredCourse)
//    }

//    @RequestMapping("/evaluateLeg")
//    fun routes( @RequestBody(required = true) model: LegScoreRequestModel): LegScoreModel {
//        val choices = courseService.evaluateLeg(model.from.toGHPoint(), model.to.toGHPoint())
//        return LegScoreModel(model.from, model.to, choices.map { LegRouteModel(it.second.map { it.toLatLng()}, it.first) } )
//    }

//    @RequestMapping("/print")
//    fun print(@RequestBody(required = true) model: CoursePrintRequestModel): ResponseEntity<ByteArray?> {
//        val ctrls = model.controls!!.map{ it.toGHPoint()}
//        val contents = courseService.printMap(ctrls)
//        val headers = HttpHeaders()
//        headers.contentType = MediaType.parseMediaType("application/pdf")
//        // Here you have to set the actual filename of your pdf
//        headers.setContentDispositionFormData(model.filename, model.filename)
//        headers.cacheControl = "must-revalidate, post-check=0, pre-check=0"
//        return ResponseEntity(contents, headers, HttpStatus.OK)
//    }

}