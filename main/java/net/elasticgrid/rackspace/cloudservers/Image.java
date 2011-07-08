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
import java.util.Date;

/**
 * Image: collection of files used to create or rebuild a server.
 *
 * @author Jerome Bernard
 */
public class Image implements Serializable {
    private final Integer id;
    private final String name;
    private final Integer serverId;
    private final Date updated;
    private final Date created;
    private final Integer progress;
    private final Status status;

    public Image(Integer id, String name, Integer serverId, Date updated, Date created, Integer progress, Status status) {
        this.id = id;
        this.name = name;
        this.serverId = serverId;
        this.updated = updated;
        this.created = created;
        this.progress = progress;
        this.status = status;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Integer getServerId() {
        return serverId;
    }

    public Date getUpdated() {
        return updated;
    }

    public Date getCreated() {
        return created;
    }

    public Integer getProgress() {
        return progress;
    }

    public Status getStatus() {
        return status;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Image");
        sb.append("{id=").append(id);
        sb.append(", name='").append(name).append('\'');
        sb.append(", serverId=").append(serverId);
        sb.append(", updated=").append(updated);
        sb.append(", created=").append(created);
        sb.append(", progress=").append(progress);
        sb.append(", status=").append(status);
        sb.append('}');
        return sb.toString();
    }

    enum Status implements Serializable {
        UNKNOWN, ACTIVE, SAVING, PREPARING, QUEUED, FAILED
    }
}
