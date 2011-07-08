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

import net.elasticgrid.rackspace.cloudservers.internal.Public;
import net.elasticgrid.rackspace.cloudservers.internal.Private;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.ArrayList;

/**
 * Addresses are either public or private.
 * @author Jerome Bernard
 */
public class Addresses implements Serializable {
    private final List<InetAddress> publicAddresses;
    private final List<InetAddress> privateAddresses;

    public Addresses(List<InetAddress> publicAddresses, List<InetAddress> privateAddresses) {
        this.publicAddresses = publicAddresses;
        this.privateAddresses = privateAddresses;
    }

    public Addresses(net.elasticgrid.rackspace.cloudservers.internal.Addresses addresses) throws UnknownHostException {
        // populate public addresses list
        Public publicAddresses = addresses.getPublic();
        this.publicAddresses = new ArrayList<InetAddress>(publicAddresses.getAddressLists().size());
        for (net.elasticgrid.rackspace.cloudservers.internal.Address address : publicAddresses.getAddressLists()) {
            this.publicAddresses.add(InetAddress.getByName(address.getAddr()));
        }
        // populate private addresses list
        Private privateAddresses = addresses.getPrivate();
        this.privateAddresses = new ArrayList<InetAddress>(privateAddresses.getAddressLists().size());
        for (net.elasticgrid.rackspace.cloudservers.internal.Address address : privateAddresses.getAddressLists()) {
            this.privateAddresses.add(InetAddress.getByName(address.getAddr()));
        }
    }

    public List<InetAddress> getPublicAddresses() {
        return publicAddresses;
    }

    public List<InetAddress> getPrivateAddresses() {
        return privateAddresses;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Addresses");
        sb.append("{publicAddresses=").append(publicAddresses);
        sb.append(", privateAddresses=").append(privateAddresses);
        sb.append('}');
        return sb.toString();
    }
}