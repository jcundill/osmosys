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

package org.osmosys

import com.graphhopper.reader.ReaderRelation
import com.graphhopper.reader.ReaderWay
import com.graphhopper.routing.profiles.DecimalEncodedValue
import com.graphhopper.routing.profiles.EncodedValue
import com.graphhopper.routing.profiles.UnsignedDecimalEncodedValue
import com.graphhopper.routing.util.AbstractFlagEncoder
import com.graphhopper.routing.util.EncodedValueOld
import com.graphhopper.routing.util.EncodingManager
import com.graphhopper.routing.util.PriorityCode
import com.graphhopper.routing.weighting.PriorityWeighting
import com.graphhopper.storage.IntsRef
import com.graphhopper.util.PMap
import java.util.*

/*
*  Licensed to GraphHopper GmbH under one or more contributor
*  license agreements. See the NOTICE file distributed with this work for
*  additional information regarding copyright ownership.
*
*  GraphHopper GmbH licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except in
*  compliance with the License. You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing, software
*  distributed under the License is distributed on an "AS IS" BASIS,
*  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*  See the License for the specific language governing permissions and
*  limitations under the License.
*/
/**
 * Defines bit layout for pedestrians (speed, access, surface, ...). Here we put a penalty on unsafe
 * roads only. If you wish to also prefer routes due to beauty like hiking routes use the
 * HikeFlagEncoder instead.
 *
 *
 *
 * @author Peter Karich
 * @author Nop
 * @author Karl Hübner
 */
class OrienteeringFlagEncoder @JvmOverloads constructor(speedBits: Int = 4, speedFactor: Double = 1.0) : AbstractFlagEncoder(speedBits, speedFactor, 0) {
    private val safeHighwayTags: MutableSet<String> = HashSet()
    private val allowedHighwayTags: MutableSet<String> = HashSet()
    private val avoidHighwayTags: MutableSet<String> = HashSet()

    // convert network tag of hiking routes into a way route code
    private val hikingNetworkToCode: MutableMap<String, Int> = HashMap()
    private var sidewalkValues = HashSet<String>(5)
    private var sidewalksNoValues = HashSet<String>(5)
    private var priorityWayEncoder: DecimalEncodedValue? = null
    private var relationCodeEncoder: EncodedValueOld? = null

    constructor(properties: PMap) : this(properties.getLong("speedBits", 4).toInt(),
            properties.getDouble("speedFactor", 1.0)) {
        this.properties = properties
        this.isBlockFords = properties.getBool("block_fords", true)
    }

    constructor(propertiesStr: String?) : this(PMap(propertiesStr)) {}

    override fun getVersion(): Int {
        return 5
    }

    override fun createEncodedValues(registerNewEncodedValue: MutableList<EncodedValue>, prefix: String, index: Int) {
        // first two bits are reserved for route handling in superclass
        super.createEncodedValues(registerNewEncodedValue, prefix, index)
        // larger value required - ferries are faster than pedestrians
        registerNewEncodedValue.add(UnsignedDecimalEncodedValue(EncodingManager.getKey(prefix, "average_speed"), speedBits, speedFactor, false).also { speedEncoder = it })
        registerNewEncodedValue.add(UnsignedDecimalEncodedValue(EncodingManager.getKey(prefix, "priority"), 3, PriorityCode.getFactor(1), false).also { priorityWayEncoder = it })
    }

    override fun defineRelationBits(index: Int, shift: Int): Int {
        relationCodeEncoder = EncodedValueOld("RelationCode", shift, 3, 1.0, 0, 7)
        return shift + relationCodeEncoder!!.bits
    }

    /**
     * Foot flag encoder does not provide any turn cost / restrictions
     */
    override fun defineTurnBits(index: Int, shift: Int): Int {
        return shift
    }

    /**
     * Foot flag encoder does not provide any turn cost / restrictions
     *
     *
     *
     * @return `false`
     */
    override fun isTurnRestricted(flags: Long): Boolean {
        return false
    }

    /**
     * Foot flag encoder does not provide any turn cost / restrictions
     *
     *
     *
     * @return 0
     */
    override fun getTurnCost(flag: Long): Double {
        return 0.0
    }

    override fun getTurnFlags(restricted: Boolean, costs: Double): Long {
        return 0
    }

    /**
     * Some ways are okay but not separate for pedestrians.
     */
    override fun getAccess(way: ReaderWay): EncodingManager.Access {
        val highwayValue = way.getTag("highway")
        if (highwayValue == null) {
            var acceptPotentially = EncodingManager.Access.CAN_SKIP
            if (way.hasTag("route", ferries)) {
                val footTag = way.getTag("foot")
                if (footTag == null || intendedValues.contains(footTag)) acceptPotentially = EncodingManager.Access.FERRY
            }

            // special case not for all acceptedRailways, only platform
            if (way.hasTag("railway", "platform")) acceptPotentially = EncodingManager.Access.WAY
            if (way.hasTag("man_made", "pier")) acceptPotentially = EncodingManager.Access.WAY
            return if (!acceptPotentially.canSkip()) {
                if (way.hasTag(restrictions, restrictedValues) && !conditionalTagInspector.isRestrictedWayConditionallyPermitted(way)) EncodingManager.Access.CAN_SKIP else acceptPotentially
            } else EncodingManager.Access.CAN_SKIP
        }
        val sacScale = way.getTag("sac_scale")
        if (sacScale != null) {
            if ("hiking" != sacScale && "mountain_hiking" != sacScale
                    && "demanding_mountain_hiking" != sacScale && "alpine_hiking" != sacScale) // other scales are too dangerous, see http://wiki.openstreetmap.org/wiki/Key:sac_scale
                return EncodingManager.Access.CAN_SKIP
        }

        // no need to evaluate ferries or fords - already included here
        if (way.hasTag("foot", intendedValues)) return EncodingManager.Access.WAY

        // check access restrictions
        if (way.hasTag(restrictions, restrictedValues) && !conditionalTagInspector.isRestrictedWayConditionallyPermitted(way)) return EncodingManager.Access.CAN_SKIP
        if (way.hasTag("sidewalk", sidewalkValues)) return EncodingManager.Access.WAY
        if (!allowedHighwayTags.contains(highwayValue)) return EncodingManager.Access.CAN_SKIP
        if (way.hasTag("motorroad", "yes")) return EncodingManager.Access.CAN_SKIP

        // do not get our feet wet, "yes" is already included above
        if (isBlockFords && (way.hasTag("highway", "ford") || way.hasTag("ford"))) return EncodingManager.Access.CAN_SKIP
        return if (conditionalTagInspector.isPermittedWayConditionallyRestricted(way)) EncodingManager.Access.CAN_SKIP else EncodingManager.Access.WAY
    }

    override fun handleRelationTags(oldRelationFlags: Long, relation: ReaderRelation): Long {
        var code = 0
        if (relation.hasTag("route", "hiking") || relation.hasTag("route", "foot")) {
            val `val` = hikingNetworkToCode[relation.getTag("network")]
            code = `val` ?: hikingNetworkToCode["lwn"]!!
        } else if (relation.hasTag("route", "ferry")) {
            code = PriorityCode.AVOID_IF_POSSIBLE.value
        }
        val oldCode = relationCodeEncoder!!.getValue(oldRelationFlags).toInt()
        return if (oldCode < code) relationCodeEncoder!!.setValue(0, code.toLong()) else oldRelationFlags
    }

    override fun handleWayTags(edgeFlags: IntsRef, way: ReaderWay, access: EncodingManager.Access, relationFlags: Long): IntsRef {
        if (access.canSkip()) return edgeFlags
        if (!access.isFerry) {
            val sacScale = way.getTag("sac_scale")
            if (sacScale != null) {
                if ("hiking" == sacScale) speedEncoder.setDecimal(false, edgeFlags, MEAN_SPEED.toDouble()) else speedEncoder.setDecimal(false, edgeFlags, SLOW_SPEED.toDouble())
            } else {
                speedEncoder.setDecimal(false, edgeFlags, MEAN_SPEED.toDouble())
            }
            accessEnc.setBool(false, edgeFlags, true)
            accessEnc.setBool(true, edgeFlags, true)
        } else {
            val ferrySpeed = getFerrySpeed(way)
            setSpeed(false, edgeFlags, ferrySpeed)
            accessEnc.setBool(false, edgeFlags, true)
            accessEnc.setBool(true, edgeFlags, true)
        }
        var priorityFromRelation = 0
        if (relationFlags != 0L) priorityFromRelation = relationCodeEncoder!!.getValue(relationFlags).toInt()
        priorityWayEncoder!!.setDecimal(false, edgeFlags, PriorityCode.getFactor(handlePriority(way, priorityFromRelation)))
        return edgeFlags
    }

    private fun handlePriority(way: ReaderWay, priorityFromRelation: Int): Int {
        val weightToPrioMap = TreeMap<Double, Int>()
        if (priorityFromRelation == 0) weightToPrioMap[0.0] = PriorityCode.UNCHANGED.value else weightToPrioMap[110.0] = priorityFromRelation
        collect(way, weightToPrioMap)

        // pick priority with biggest order value
        return weightToPrioMap.lastEntry().value
    }

    /**
     * @param weightToPrioMap associate a weight with every priority. This sorted map allows
     * subclasses to 'insert' more important priorities as well as overwrite determined priorities.
     */
    private fun collect(way: ReaderWay, weightToPrioMap: TreeMap<Double, Int>) {
        val highway = way.getTag("highway")
        if (way.hasTag("foot", "designated")) weightToPrioMap[43.0] = PriorityCode.PREFER.value
        val maxSpeed = getMaxSpeed(way)
        if (safeHighwayTags.contains(highway) || maxSpeed > 0 && maxSpeed <= 20) {
            weightToPrioMap[40.0] = PriorityCode.PREFER.value
            if (way.hasTag("tunnel", intendedValues)) {
                if (way.hasTag("sidewalk", sidewalksNoValues)) weightToPrioMap[40.0] = PriorityCode.AVOID_IF_POSSIBLE.value else weightToPrioMap[40.0] = PriorityCode.UNCHANGED.value
            }
        } else if (maxSpeed > 50 || avoidHighwayTags.contains(highway)) {
            if (!way.hasTag("sidewalk", sidewalkValues)) weightToPrioMap[45.0] = PriorityCode.AVOID_AT_ALL_COSTS.value
        }
        if (way.hasTag("bicycle", "official") || way.hasTag("bicycle", "designated")) weightToPrioMap[44.0] = PriorityCode.PREFER.value
        if (way.hasTag("service")) weightToPrioMap[100.0] = PriorityCode.AVOID_AT_ALL_COSTS.value
    }

    override fun supports(feature: Class<*>): Boolean {
        return if (super.supports(feature)) true else PriorityWeighting::class.java.isAssignableFrom(feature)
    }

    override fun toString(): String {
        return "orienteering"
    }


    companion object {
        const val SLOW_SPEED = 2
        const val MEAN_SPEED = 5
        const val FERRY_SPEED = 15
    }

    /**
     * Should be only instantiated via EncodingManager
     */
    init {
        restrictions.addAll(listOf("foot", "access"))
        restrictedValues.add("private")
        restrictedValues.add("no")
        restrictedValues.add("restricted")
        restrictedValues.add("military")
        restrictedValues.add("emergency")

        intendedValues.add("yes")
        intendedValues.add("designated")
        intendedValues.add("official")
        intendedValues.add("permissive")

        sidewalksNoValues.add("no")
        sidewalksNoValues.add("none")
        // see #712
        sidewalksNoValues.add("separate")
        sidewalkValues.add("yes")
        sidewalkValues.add("both")
        sidewalkValues.add("left")
        sidewalkValues.add("right")

        setBlockByDefault(false)
        absoluteBarriers.add("fence")
//        potentialBarriers.add("gate")
//        potentialBarriers.add("cattle_grid")

        safeHighwayTags.add("footway")
        safeHighwayTags.add("path")
        safeHighwayTags.add("steps")
        safeHighwayTags.add("pedestrian")
        safeHighwayTags.add("living_street")
        safeHighwayTags.add("track")
        safeHighwayTags.add("residential")
        safeHighwayTags.add("platform")

        avoidHighwayTags.add("motorway")
        avoidHighwayTags.add("motorway_link")
        avoidHighwayTags.add("trunk")
        avoidHighwayTags.add("trunk_link")
        avoidHighwayTags.add("primary")
        avoidHighwayTags.add("primary_link")
        avoidHighwayTags.add("secondary")
        avoidHighwayTags.add("secondary_link")
        avoidHighwayTags.add("tertiary")
        avoidHighwayTags.add("tertiary_link")
        avoidHighwayTags.add("service")

        // for now no explicit avoiding #257
        //avoidHighwayTags.add("cycleway");
        allowedHighwayTags.addAll(safeHighwayTags)
        //allowedHighwayTags.addAll(avoidHighwayTags)
        allowedHighwayTags.add("cycleway")
        allowedHighwayTags.add("unclassified")
        allowedHighwayTags.add("road")
        // disallowed in some countries
        allowedHighwayTags.add("bridleway")
        hikingNetworkToCode["iwn"] = PriorityCode.UNCHANGED.value
        hikingNetworkToCode["nwn"] = PriorityCode.UNCHANGED.value
        hikingNetworkToCode["rwn"] = PriorityCode.UNCHANGED.value
        hikingNetworkToCode["lwn"] = PriorityCode.UNCHANGED.value
        maxPossibleSpeed = FERRY_SPEED
        speedDefault = MEAN_SPEED.toDouble()
        init()
    }
}