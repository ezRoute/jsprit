/*
 * Licensed to GraphHopper GmbH under one or more contributor
 * license agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * GraphHopper GmbH licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.graphhopper.jsprit.core.algorithm.ruin.distance;

import java.util.List;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.util.EuclideanDistanceCalculator;


/**
 * Calculator that calculates average distance between two jobs based on the
 * input-transport costs.
 * <p>
 * <p>
 * If the distance between two jobs cannot be calculated with input-transport
 * costs, it tries the euclidean distance between these jobs.
 *
 * @author stefan schroeder
 */
public class AvgServiceAndShipmentDistance implements JobDistance {

    private VehicleRoutingTransportCosts costs;

    public AvgServiceAndShipmentDistance(VehicleRoutingTransportCosts costs) {
        super();
        this.costs = costs;

    }

    /**
     * Calculates and returns the average distance between two jobs based on the
     * input-transport costs.
     * <p>
     * <p>
     * If the distance between two jobs cannot be calculated with
     * input-transport costs, it tries the euclidean distance between these
     * jobs.
     */
    @Override
    public double getDistance(Job i, Job j) {
        if (i.equals(j)) {
            return 0.0;
        }

        // TODO: Do we really need these checks after the refactor?
//      if (!(i instanceof Service || i instanceof Shipment || i instanceof ShipmentWithBackhaul)) {
//          throw new IllegalStateException("this supports only shipments or services");
//      }
//      if (!(j instanceof Service || j instanceof Shipment || j instanceof ShipmentWithBackhaul)) {
//          throw new IllegalStateException("this supports only shipments or services");
//      }

        return calcDist(i.getAllLocations(), j.getAllLocations());
    }

    /**
     * Calculates the average distance of the two set of positions.
     *
     * @param leftLocations
     *            The position list of one side.
     * @param rightLocations
     *            The position list of the other side.
     * @return The Average distance. (Returns 0 when any of the sides contains
     *         no distances.)
     */
    private double calcDist(List<Location> leftLocations, List<Location> rightLocations) {
        if (leftLocations.isEmpty() || rightLocations.isEmpty()) {
            return 0d;
        }
        double totalDistance = 0d;
        for (Location left : leftLocations) {
            for (Location right : rightLocations) {
                totalDistance += calcDist(left, right);
            }
        }
        return totalDistance / (leftLocations.size() * rightLocations.size());
    }

    private double calcDist(Location location_i, Location location_j) {
        try {
            return costs.getTransportCost(location_i, location_j, 0.0, null, null);
        } catch (IllegalStateException e) {
            // now try the euclidean distance between these two services
        }
        return EuclideanDistanceCalculator.calculateDistance(location_i.getCoordinate(), location_j.getCoordinate());
    }
}
