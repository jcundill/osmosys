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

package org.osmosys.furniture

import com.graphhopper.util.shapes.BBox
import de.westnordost.osmapi.OsmConnection
import de.westnordost.osmapi.overpass.OverpassMapDataDao
import org.osmosys.ControlSite

class StreetFurnitureFinder {


    fun findForBoundingBox(box: BBox): List<ControlSite> {
        val locations = mutableListOf<ControlSite>()

        val connection = OsmConnection("https://overpass-api.de/api/", "osmosys")
        val overpass = OverpassMapDataDao(connection)
        val handler = StreetFurnitureMapDataHandler(locations)
        val bbox = "${box.minLat},${box.minLon},${box.maxLat},${box.maxLon}"
        val q = """
            (
  node["highway"="bus_stop"]($bbox);
  node["highway"="crossing"]($bbox);
  node["highway"="give_way"]($bbox);
  node["highway"="traffic_signals"]($bbox);
  node["tourism"="information"]($bbox);
  node["tourism"="artwork"]($bbox);
  node["natural"="tree"]($bbox);
  node["amenity"="post_box"]($bbox);
  node["amenity"="bbq"]($bbox);
  node["amenity"="drinking_water"]($bbox);
  node["amenity"="charging_station"]($bbox);
  node["amenity"="grit_bin"]($bbox);
  node["amenity"="bench"]($bbox);
  node["amenity"="telephone"]($bbox);
  node["amenity"="vending_machine"]($bbox);
  node["amenity"="waste_basket"]($bbox);
  node["amenity"="waste_disposal"]($bbox);
  node["amenity"="water_point"]($bbox);
  node["barrier"="gate"]($bbox);
  node["barrier"="bollard"]($bbox);
  node["barrier"="cycle_barrier"]($bbox);
  node["barrier"="kissing_gate"]($bbox);
  node["barrier"="horse_stile"]($bbox);
  node["barrier"="stile"]($bbox);
  node["historic"="memorial"]($bbox);
  node["historic"="milestone"]($bbox);
  node["historic"="boundary_stone"]($bbox);
  node["historic"="cannon"]($bbox);
  way["highway"="steps"]($bbox);
  way["bridge"="yes"]($bbox);
  way["barrier"="hedge"]($bbox);
                <;); out body geom;
""".trimIndent()

        overpass.queryElementsWithGeometry(q, handler)
        return locations
    }
}