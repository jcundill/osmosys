package jon.test.streeto.course

import jon.test.CourseParameters
import jon.test.StreetO
import jon.test.toGHPoint
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController


@RestController
class CourseController {

    @Autowired
    lateinit var streetO: StreetO

    @RequestMapping("/create")
    fun create(@RequestParam(name = "length", required = false, defaultValue = "8000.0") length: Double,
               @RequestParam(name = "num_controls", required = false, defaultValue = "8") numControls: Int,
               @RequestParam(name = "start", required = true) start: String): String {

        val params = CourseParameters(distance = length, numControls = numControls, start = start.toGHPoint()!!)
        val problem = streetO.makeProblem(params)

        val solution = streetO.findCourse(problem)!!
        //val best = streetO.findBestRoute(solution.controls)

        return solution.controls.joinToString("|")
    }
}