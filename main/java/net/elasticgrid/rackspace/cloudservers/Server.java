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

import net.elasticgrid.rackspace.cloudservers.internal.Metadata;
import net.elasticgrid.rackspace.cloudservers.internal.MetadataItem;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.net.UnknownHostException;

/**
 * Rackspace Server.
 *
 * @author Jerome Bernard
 */
public class Server implements Serializable {
    private final Integer id;
    private final String name;
    private final String adminPass;
    private final Integer imageID;
    private final Integer flavorID;
    private final Status status;
    private final Map<String, String> metadata;
    private final Personality personality;
    private final Addresses addresses;

    public Server(Integer id, String name, String adminPass, Integer imageID, Integer flavorID, Server.Status status,
                  Map<String, String> metadata, Addresses addresses, Personality personality) {
        this.id = id;
        this.name = name;
        this.adminPass = adminPass;
        this.imageID = imageID;
        this.flavorID = flavorID;
        this.status = status;
        this.metadata = metadata;
        this.addresses = addresses;
        this.personality = personality;
    }

    private static Map<String, String> metadataAsMap(Metadata metadata) {
        Map<String, String> meta = new HashMap<String, String>();
        for (MetadataItem item : metadata.getMetadatas()) {
            meta.put(item.getKey(), item.getString());
        }
        return meta;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Integer getImageID() {
        return imageID;
    }

    public Integer getFlavorID() {
        return flavorID;
    }

    public Status getStatus() {
        return status;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public List<java.net.InetAddress> getPublicAddresses() {
        return addresses.getPublicAddresses();
    }

    public List<java.net.InetAddress> getPrivateAddresses() {
        return addresses.getPrivateAddresses();
    }

    public enum Status {
        ACTIVE, SUSPENDED, DELETED, QUEUE_RESIZE, PREP_RESIZE, RESIZE, VERIFY_RESIZE,
        QUEUE_MOVE, PREP_MOVE, MOVE, VERIFY_MOVE, RESCUE, ERROR, BUILD, RESTORING,
        PASSWORD, REBUILD, DELETE_IP, SHARE_IP_NO_CONFIG, SHARE_IP, REBOOT, HARD_REBOOT, UNKNOWN
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Server");
        sb.append("{id=").append(id);
        sb.append(", name='").append(name).append('\'');
        sb.append(", adminPass='").append(adminPass).append('\'');
        sb.append(", imageID=").append(imageID);
        sb.append(", flavorID=").append(flavorID);
        sb.append(", status=").append(status);
        sb.append(", metadata=").append(metadata);
        sb.append(", personality=").append(personality);
        sb.append(", addresses=").append(addresses);
        sb.append('}');
        return sb.toString();
    }
}
