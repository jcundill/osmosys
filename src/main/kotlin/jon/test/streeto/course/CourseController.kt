package jon.test.streeto.course

import jon.test.Course
import jon.test.StreetO
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
class CourseController {

    @Autowired
    lateinit var streetO: StreetO

    @RequestMapping("/create")
    fun create(@RequestBody(required = true) course: CourseModel): CourseModel {

        val initialCourse = Course(
                requestedDistance = course.requestedDistance,
                requestedNumControls = course.numControls,
                controls = course.controls.map { it.toGHPoint() }
        )
        val problem = streetO.makeProblem(initialCourse)
        val solution = streetO.findCourse(problem)!!

        return CourseModel.buildFrom(solution)
    }
}

