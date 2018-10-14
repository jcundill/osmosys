package jon.test.streeto

import com.graphhopper.util.shapes.GHPoint
import jon.test.*
import jon.test.constraints.CourseLengthConstraint
import jon.test.constraints.IsRouteableConstraint
import jon.test.constraints.PrintableOnMapConstraint
import jon.test.scorers.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class StreetOConfiguration {

    @Bean
    fun streetO(): StreetO {
        return StreetO("england-latest")
    }
}