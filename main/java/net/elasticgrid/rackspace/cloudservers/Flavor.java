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
 * Hardware configuration for a server.
 * @author Jerome Bernard 
 */
public class Flavor implements Serializable {
    private final int id;
    private final String name;
    private final Integer ram;
    private final Integer disk;

    public Flavor(int id, String name, Integer ram, Integer disk) {
        this.id = id;
        this.name = name;
        this.ram = ram;
        this.disk = disk;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Integer getRam() {
        return ram;
    }

    public Integer getDisk() {
        return disk;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Flavor");
        sb.append("{id=").append(id);
        sb.append(", name='").append(name).append('\'');
        sb.append(", ram=").append(ram);
        sb.append(", disk=").append(disk);
        sb.append('}');
        return sb.toString();
    }
}
