/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package net.elasticgrid.rackspace.cloudservers;

import java.io.Serializable;

/**
 * Server backup schedule.
 *
 * @author Jerome Bernard
 */
public class BackupSchedule implements Serializable {
    private boolean enabled;
    private WeeklyBackup weekly;
    private DailyBackup daily;

    public BackupSchedule(boolean enabled, WeeklyBackup weekly, DailyBackup daily) {
        this.enabled = enabled;
        this.weekly = weekly;
        this.daily = daily;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public WeeklyBackup getWeekly() {
        return weekly;
    }

    public DailyBackup getDaily() {
        return daily;
    }

    public enum WeeklyBackup implements Serializable {
        DISABLED("DISABLED"), SUNDAY("SUNDAY"), MONDAY("MONDAY"), TUESDAY("TUESDAY"),
        WEDNESDAY("WEDNESDAY"), THURSDAY("THURSDAY"), FRIDAY("FRIDAY"),
        SATURDAY("SATURDAY"), SUNDAY1("SUNDAY");
        private final String value;

        private WeeklyBackup(String value) {
            this.value = value;
        }
    }

    public enum DailyBackup implements Serializable {
        DISABLED, H_0000_0200, H_0200_0400, H_0400_0600, H_0600_0800, H_0800_1000,
        H_1000_1200, H_1200_1400, H_1400_1600, H_1600_1800, H_1800_2000,
        H_2000_2200, H_2200_0000
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("BackupSchedule");
        sb.append("{enabled=").append(enabled);
        sb.append(", weekly=").append(weekly);
        sb.append(", daily=").append(daily);
        sb.append('}');
        return sb.toString();
    }
}
