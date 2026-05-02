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

    public static final int DEFAULT_SCORE = 100;
    public static final int ACCELERATION_PENALTY = 5;
    public static final int BRAKING_PENALTY = 5;
    public static final int CORNERING_PENALTY = 5;

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

    public int getTotalEvents() {
        return harshAccelerationCount + harshBrakingCount + harshCorneringCount;
    }

    public int getScore() {
        int penalty = harshAccelerationCount * ACCELERATION_PENALTY
                + harshBrakingCount * BRAKING_PENALTY
                + harshCorneringCount * CORNERING_PENALTY;
        return Math.max(0, DEFAULT_SCORE - penalty);
    }

}
