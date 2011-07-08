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

import java.io.*;
import java.util.*;

/**
 * Rackspace API limits.
 * @author Jerome Bernard
 */
public class Limits implements Serializable {
    private final List<RateLimit> rateLimits;
    private final List<AbsoluteLimit> absoluteLimits;

    public Limits(List<RateLimit> rateLimits, List<AbsoluteLimit> absoluteLimits) {
        this.rateLimits = Collections.unmodifiableList(rateLimits);
        this.absoluteLimits = Collections.unmodifiableList(absoluteLimits);
    }

    public List<RateLimit> getRateLimits() {
        return rateLimits;
    }

    public List<AbsoluteLimit> getAbsoluteLimits() {
        return absoluteLimits;
    }

}

class RateLimit implements Serializable {
    private final HTTPVerb verb;
    private final String URI;
    private final String regex;
    private final int value;
    private final int remaining;
    private final RateLimit.Unit unit;
    private final long resetTime;

    RateLimit(HTTPVerb verb, String URI, String regex, int value, int remaining, RateLimit.Unit unit, long resetTime) {
        this.verb = verb;
        this.URI = URI;
        this.regex = regex;
        this.value = value;
        this.remaining = remaining;
        this.unit = unit;
        this.resetTime = resetTime;
    }

    public HTTPVerb getVerb() {
        return verb;
    }

    public String getURI() {
        return URI;
    }

    public String getRegex() {
        return regex;
    }

    public int getValue() {
        return value;
    }

    public int getRemaining() {
        return remaining;
    }

    public RateLimit.Unit getUnit() {
        return unit;
    }

    public long getResetTime() {
        return resetTime;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("RateLimit");
        sb.append("{verb=").append(verb);
        sb.append(", URI='").append(URI).append('\'');
        sb.append(", regex='").append(regex).append('\'');
        sb.append(", value=").append(value);
        sb.append(", remaining=").append(remaining);
        sb.append(", unit=").append(unit);
        sb.append(", resetTime=").append(resetTime);
        sb.append('}');
        return sb.toString();
    }

    enum Unit implements Serializable {
        MINUTE, HOUR, DAY
    }
}

class AbsoluteLimit implements Serializable {
    private final String name;
    private final int value;

    public AbsoluteLimit(String name, int value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("AbsoluteLimit");
        sb.append("{name='").append(name).append('\'');
        sb.append(", value=").append(value);
        sb.append('}');
        return sb.toString();
    }
}
