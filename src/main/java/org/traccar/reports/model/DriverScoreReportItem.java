/*
 * Copyright 2026 GhulamTaseen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.traccar.reports.model;

public class DriverScoreReportItem {

    private static final double DEFAULT_SCORE = 100.0;
    private static final double MINIMUM_NORMALIZED_DISTANCE = 10000.0;
    private static final double ACCELERATION_WEIGHT = 1.0;
    private static final double BRAKING_WEIGHT = 1.5;
    private static final double CORNERING_WEIGHT = 1.3;
    private static final double NORMALIZED_RISK_PENALTY = 2.0;
    private static final double FALLBACK_RISK_PENALTY = 5.0;

    private long deviceId;

    public long getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(long deviceId) {
        this.deviceId = deviceId;
    }

    private String deviceName;

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    private String driverUniqueId;

    public String getDriverUniqueId() {
        return driverUniqueId;
    }

    public void setDriverUniqueId(String driverUniqueId) {
        this.driverUniqueId = driverUniqueId;
    }

    private String driverName;

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    private int harshAccelerationCount;

    public int getHarshAccelerationCount() {
        return harshAccelerationCount;
    }

    public void setHarshAccelerationCount(int harshAccelerationCount) {
        this.harshAccelerationCount = harshAccelerationCount;
    }

    public void addHarshAccelerationCount() {
        harshAccelerationCount++;
    }

    private int harshBrakingCount;

    public int getHarshBrakingCount() {
        return harshBrakingCount;
    }

    public void setHarshBrakingCount(int harshBrakingCount) {
        this.harshBrakingCount = harshBrakingCount;
    }

    public void addHarshBrakingCount() {
        harshBrakingCount++;
    }

    private int harshCorneringCount;

    public int getHarshCorneringCount() {
        return harshCorneringCount;
    }

    public void setHarshCorneringCount(int harshCorneringCount) {
        this.harshCorneringCount = harshCorneringCount;
    }

    public void addHarshCorneringCount() {
        harshCorneringCount++;
    }

    private double distance;

    public double getDistance() {
        return distance;
    }

    public void addDistance(double distance) {
        this.distance += distance;
    }

    public int getTotalEvents() {
        return harshAccelerationCount + harshBrakingCount + harshCorneringCount;
    }

    public double getRiskScore() {
        return harshAccelerationCount * ACCELERATION_WEIGHT
                + harshBrakingCount * BRAKING_WEIGHT
                + harshCorneringCount * CORNERING_WEIGHT;
    }

    public double getEventsPer100Km() {
        if (distance > 0.0) {
            return getTotalEvents() * 100000.0 / Math.max(distance, MINIMUM_NORMALIZED_DISTANCE);
        }
        return 0.0;
    }

    public double getRiskPer100Km() {
        if (distance > 0.0) {
            return getRiskScore() * 100000.0 / Math.max(distance, MINIMUM_NORMALIZED_DISTANCE);
        }
        return 0.0;
    }

    public int getScore() {
        double penalty;
        if (distance > 0.0) {
            penalty = getRiskPer100Km() * NORMALIZED_RISK_PENALTY;
        } else {
            penalty = getRiskScore() * FALLBACK_RISK_PENALTY;
        }
        return (int) Math.round(Math.max(0.0, DEFAULT_SCORE - penalty));
    }

}
