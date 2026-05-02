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
package org.traccar.reports;

import jakarta.inject.Inject;
import org.traccar.config.Config;
import org.traccar.config.Keys;
import org.traccar.helper.model.DeviceUtil;
import org.traccar.helper.model.PositionUtil;
import org.traccar.model.Device;
import org.traccar.model.Event;
import org.traccar.model.Position;
import org.traccar.reports.common.ReportUtils;
import org.traccar.reports.model.DriverScoreReportItem;
import org.traccar.storage.Storage;
import org.traccar.storage.StorageException;
import org.traccar.storage.query.Columns;
import org.traccar.storage.query.Condition;
import org.traccar.storage.query.Order;
import org.traccar.storage.query.Request;

import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DriverScoreReportProvider {

    private static final String NO_DRIVER = "";

    private final ReportUtils reportUtils;
    private final Storage storage;
    private final Config config;

    @Inject
    public DriverScoreReportProvider(ReportUtils reportUtils, Storage storage, Config config) {
        this.reportUtils = reportUtils;
        this.storage = storage;
        this.config = config;
    }

    private List<Event> getEvents(long deviceId, Date from, Date to) throws StorageException {
        return storage.getObjects(Event.class, new Request(
                new Columns.All(),
                new Condition.And(
                        new Condition.And(
                                new Condition.Equals("deviceId", deviceId),
                                new Condition.Between("eventTime", from, to)),
                        new Condition.Equals("type", Event.TYPE_ALARM)),
                new Order("eventTime")));
    }

    private Position getPosition(long positionId) throws StorageException {
        if (positionId > 0) {
            return storage.getObject(Position.class, new Request(
                    new Columns.All(), new Condition.Equals("id", positionId)));
        }
        return null;
    }

    private void countAlarm(DriverScoreReportItem item, String alarm) {
        if (Position.ALARM_ACCELERATION.equals(alarm)) {
            item.addHarshAccelerationCount();
        } else if (Position.ALARM_BRAKING.equals(alarm)) {
            item.addHarshBrakingCount();
        } else if (Position.ALARM_CORNERING.equals(alarm)) {
            item.addHarshCorneringCount();
        }
    }

    private void addDistance(Map<String, DriverScoreReportItem> items, Device device, Date from, Date to)
            throws StorageException {

        Position previous = null;
        try (var positions = PositionUtil.getPositionsStream(storage, device.getId(), from, to)) {
            var iterator = positions.iterator();
            while (iterator.hasNext()) {
                Position position = iterator.next();
                if (previous != null) {
                    DriverScoreReportItem item = getItem(items, device, previous);
                    item.addDistance(Math.max(0.0, position.getDouble(Position.KEY_TOTAL_DISTANCE)
                            - previous.getDouble(Position.KEY_TOTAL_DISTANCE)));
                }
                previous = position;
            }
        }
    }

    private boolean isScoredAlarm(String alarm) {
        return Position.ALARM_ACCELERATION.equals(alarm)
                || Position.ALARM_BRAKING.equals(alarm)
                || Position.ALARM_CORNERING.equals(alarm);
    }

    private DriverScoreReportItem getItem(
            Map<String, DriverScoreReportItem> items, Device device, Position position) throws StorageException {

        String driverUniqueId = position != null ? position.getString(Position.KEY_DRIVER_UNIQUE_ID) : null;
        String key = device.getId() + ":" + (driverUniqueId != null ? driverUniqueId : NO_DRIVER);
        DriverScoreReportItem item = items.get(key);
        if (item == null) {
            item = new DriverScoreReportItem(
                    config.getDouble(Keys.REPORT_DRIVER_SCORE_ACCELERATION_WEIGHT),
                    config.getDouble(Keys.REPORT_DRIVER_SCORE_BRAKING_WEIGHT),
                    config.getDouble(Keys.REPORT_DRIVER_SCORE_CORNERING_WEIGHT),
                    config.getDouble(Keys.REPORT_DRIVER_SCORE_NORMALIZED_RISK_PENALTY),
                    config.getDouble(Keys.REPORT_DRIVER_SCORE_FALLBACK_RISK_PENALTY),
                    config.getDouble(Keys.REPORT_DRIVER_SCORE_MINIMUM_DISTANCE));
            item.setDeviceId(device.getId());
            item.setDeviceName(device.getName());
            item.setDriverUniqueId(driverUniqueId);
            item.setDriverName(reportUtils.findDriverName(driverUniqueId));
            items.put(key, item);
        }
        return item;
    }

    public Collection<DriverScoreReportItem> getObjects(
            long userId, Collection<Long> deviceIds, Collection<Long> groupIds,
            Date from, Date to) throws StorageException {

        reportUtils.checkPeriodLimit(from, to);
        Map<String, DriverScoreReportItem> result = new LinkedHashMap<>();

        for (Device device: DeviceUtil.getAccessibleDevices(storage, userId, deviceIds, groupIds)) {
            addDistance(result, device, from, to);
            for (Event event : getEvents(device.getId(), from, to)) {
                String alarm = event.getString(Position.KEY_ALARM);
                if (isScoredAlarm(alarm)) {
                    DriverScoreReportItem item = getItem(result, device, getPosition(event.getPositionId()));
                    countAlarm(item, alarm);
                }
            }
        }

        return result.values();
    }

}
