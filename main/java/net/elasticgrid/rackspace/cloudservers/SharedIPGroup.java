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

import net.elasticgrid.rackspace.cloudservers.internal.SharedIpGroup;
import java.io.Serializable;
import java.util.List;

/**
 * Shared IP group information.
 * @author Jerome Bernard
 */
public class SharedIPGroup implements Serializable {
    private final Integer id;
    private final String name;
//    private final Integer serverID;
    private final List<Integer> serverIDs;
//    private int choiceSelect = -1;
//    private final static int SERVER_CHOICE = 0;
//    private final static int SERVERS_CHOICE = 1;

    public SharedIPGroup(Integer id, String name, List<Integer> serverIDs) {
        this.id = id;
        this.name = name;
        this.serverIDs = serverIDs;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<Integer> getServerIDs() {
        return serverIDs;
    }

    /*
    public Integer getServerID() {
        return serverID;
    }

    public int getChoiceSelect() {
        return choiceSelect;
    }

    public static int getServerChoice() {
        return SERVER_CHOICE;
    }

    public static int getServersChoice() {
        return SERVERS_CHOICE;
    }
    */
}
