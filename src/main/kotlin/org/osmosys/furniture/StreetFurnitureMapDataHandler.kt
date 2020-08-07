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

import de.westnordost.osmapi.map.data.*
import de.westnordost.osmapi.overpass.MapDataWithGeometryHandler
import org.osmosys.ControlSite

class StreetFurnitureMapDataHandler(private val locations: MutableList<ControlSite>): MapDataWithGeometryHandler {

    private fun parsePointFeatue(node: Node): ControlSite {
        val lat = node.position.latitude
        val lon = node.position.longitude
        val description = when {
            node.tags["tourism"] != null -> node.tags["tourism"]
            node.tags["highway"] != null -> node.tags["highway"]
            node.tags["amenity"] != null -> node.tags["amenity"]
            node.tags["natural"] != null -> node.tags["natural"]
            node.tags["barrier"] != null -> node.tags["barrier"]
            else -> "unknown"
        }!!
        if( description == "")
            println()
        return ControlSite(lat, lon, description)
    }

    private fun parseLinearFeature(way: Way, loc: LatLon): ControlSite {
        val lat = loc.latitude
        val lon = loc.longitude
        val description = when {
            way.tags["highway"] == "steps" -> "steps"
            way.tags["bridge"] == "yes" -> "bridge"
            way.tags["barrier"] == "hedge" -> "hedge"
            else -> "unknown"
        }
        if( description == "")
            println()
        return ControlSite(lat, lon, description)

    }

    override fun handle(bounds: BoundingBox) {}

    override fun handle(node: Node) {
        if( !node.isDeleted) {
            locations.add(parsePointFeatue(node))
        }
    }

    override fun handle(way: Way, bounds: BoundingBox, geometry: MutableList<LatLon>) {
        locations.add(parseLinearFeature(way, geometry.first()))
        locations.add(parseLinearFeature(way, geometry.last()))
    }

    override fun handle(relation: Relation, bounds: BoundingBox, nodeGeometries: MutableMap<Long, LatLon>, wayGeometries: MutableMap<Long, MutableList<LatLon>>) {}
}

