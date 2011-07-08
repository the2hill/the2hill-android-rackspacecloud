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

import java.util.List;
import java.util.Map;
import java.net.InetAddress;

/**
 * Rackspace Cloud Servers API.
 *
 * @author Jerome Bernard
 */
public interface CloudServers {

    /**
     * Retrieve the list of servers (only IDs and names) associated with the Rackspace account.
     *
     * @return the list of servers
     * @throws CloudServersException if there is an exception when calling the Cloud Servers API
     */
    List<Server> getServers() throws CloudServersException;

    /**
     * Retrieve the list of servers (with details) associated with the Rackspace account.
     *
     * @return the list of servers
     * @throws CloudServersException if there is an exception when calling the Cloud Servers API
     */
    List<Server> getServersWithDetails() throws CloudServersException;

    /**
     * Retrieve the server details.
     *
     * @param serverID the ID of the server for which details should be retrieved
     * @return the server details
     * @throws CloudServersException if there is an exception when calling the Cloud Servers API
     */
    Server getServerDetails(int serverID) throws CloudServersException;

    /**
     * Retrieve server addresses.
     *
     * @param serverID the ID of the server for which addresses should be retrieved
     * @return the server addresses
     * @throws CloudServersException if there is an exception when calling the Cloud Servers API
     */
    Addresses getServerAddresses(int serverID) throws CloudServersException;

    /**
     * Retrieve public server addresses.
     *
     * @param serverID the ID of the server for which addresses should be retrieved
     * @return the server addresses
     * @throws CloudServersException if there is an exception when calling the Cloud Servers API
     */
    List<InetAddress> getServerPublicAddresses(int serverID) throws CloudServersException;

    /**
     * Retrieve private server addresses.
     *
     * @param serverID the ID of the server for which addresses should be retrieved
     * @return the server addresses
     * @throws CloudServersException if there is an exception when calling the Cloud Servers API
     */
    List<InetAddress> getServerPrivateAddresses(int serverID) throws CloudServersException;

    /**
     * Share an IP address to the specified server.
     * 
     * @param groupID         the ID of the shared IP group
     * @param serverID        the ID of the server for which the IP should be shared
     * @param address         the IP¨address to share with the server
     * @throws CloudServersException if there is an exception when calling the Cloud Servers API
     */
    void shareAddress(int groupID, int serverID, InetAddress address) throws CloudServersException;

    /**
     * Share an IP address to the specified server.
     *
     * @param groupID         the ID of the shared IP group
     * @param serverID        the ID of the server for which the IP should be shared
     * @param address         the IP¨address to share with the server
     * @param configureServer if true the server is configured with the new address, though the new address is not
     *                        enabled; configuring the server does require a reboot
     * @throws CloudServersException if there is an exception when calling the Cloud Servers API
     */
    void shareAddress(int groupID, int serverID, InetAddress address, boolean configureServer) throws CloudServersException;

    /**
     * Remove a shared IP address from the specified server.
     *
     * @param serverID the ID of the server for which the IP should be not be shared anymore
     * @param address  the IP¨address to stop sharing with the server
     * @throws CloudServersException if there is an exception when calling the Cloud Servers API
     */
    void unshareAddress(int serverID, InetAddress address) throws CloudServersException;

    /**
     * Provision a new server.
     *
     * @param name     the name of the server to create
     * @param imageID  the image from which the server should be created
     * @param flavorID the kind of hardware to use
     * @return the created server with precious information such as admin password for that server
     * @throws CloudServersException if there is an exception when calling the Cloud Servers API
     */
    Server createServer(String name, int imageID, int flavorID) throws CloudServersException;

    /**
     * Provision a new server.
     *
     * @param name     the name of the server to create
     * @param imageID  the image from which the server should be created
     * @param flavorID the kind of hardware to use
     * @param metadata the launch metadata
     * @return the created server with precious information such as admin password for that server
     * @throws CloudServersException if there is an exception when calling the Cloud Servers API
     */
    Server createServer(String name, int imageID, int flavorID, Map<String, String> metadata) throws CloudServersException;

    /**
     * Reboot the specified server.
     *
     * @param serverID the ID of the server to reboot
     * @throws CloudServersException if there is an exception when calling the Cloud Servers API
     * @see #rebootServer(int, RebootType)
     */
    void rebootServer(int serverID) throws CloudServersException;

    /**
     * Reboot the specified server.
     *
     * @param serverID the ID of the server to reboot
     * @param type     the type of reboot to perform
     * @throws CloudServersException if there is an exception when calling the Cloud Servers API
     * @see #rebootServer(int, RebootType)
     */
    void rebootServer(int serverID, RebootType type) throws CloudServersException;

    /**
     * Rebuild the specified server.
     *
     * @param serverID the ID of the server to rebuild
     * @throws CloudServersException if there is an exception when calling the Cloud Servers API
     * @see #rebuildServer(int, int)
     */
    void rebuildServer(int serverID) throws CloudServersException;

    /**
     * Rebuild the specified server from an different image than the one initially used.
     *
     * @param serverID the ID of the server to rebuild
     * @param imageID  the new image to use
     * @throws CloudServersException if there is an exception when calling the Cloud Servers API
     * @see #rebuildServer(int)
     */
    void rebuildServer(int serverID, int imageID) throws CloudServersException;

    /**
     * Resize the specified server.
     *
     * @param serverID the ID of the server to resize
     * @param flavorID the new flavor of hardware which should be used
     * @throws CloudServersException if there is an exception when calling the Cloud Servers API
     */
    void resizeServer(int serverID, int flavorID) throws CloudServersException;

    /**
     * Confirm a pending resize action.
     *
     * @param serverID the ID of the server for which the resize should be confirmed
     * @throws CloudServersException if there is an exception when calling the Cloud Servers API
     */
    void confirmResize(int serverID) throws CloudServersException;

    /**
     * Cancel and revert a pending resize action.
     *
     * @param serverID the ID of the server for which the resize should be cancelled
     * @throws CloudServersException if there is an exception when calling the Cloud Servers API
     */
    void revertResize(int serverID) throws CloudServersException;

    /**
     * Update the specified server's name and/or administrative password. This operation allows you to update the name
     * of the server and change the administrative password. This operation changes the name of the server in the Cloud
     * Servers system and does not change the server host name itself.
     *
     * @param serverID the ID of the server to update
     * @param name     the new name for the server
     * @throws CloudServersException if there is an exception when calling the Cloud Servers API
     */
    void updateServerName(int serverID, String name) throws CloudServersException;

    /**
     * Update the specified server's name and/or administrative password. This operation allows you to update the name
     * of the server and change the administrative password. This operation changes the name of the server in the Cloud
     * Servers system and does not change the server host name itself.
     *
     * @param serverID the ID of the server to update
     * @param password the new password
     * @throws CloudServersException if there is an exception when calling the Cloud Servers API
     */
    void updateServerPassword(int serverID, String password) throws CloudServersException;

    /**
     * Update the specified server's name and/or administrative password. This operation allows you to update the name
     * of the server and change the administrative password. This operation changes the name of the server in the Cloud
     * Servers system and does not change the server host name itself.
     *
     * @param serverID the ID of the server to update
     * @param name     the new name for the server
     * @param password the new password
     * @throws CloudServersException if there is an exception when calling the Cloud Servers API
     */
    void updateServerNameAndPassword(int serverID, String name, String password) throws CloudServersException;

    /**
     * Deletes a cloud server instance from the system
     *
     * @param serverID the ID of the server to delete
     * @throws CloudServersException if there is an exception when calling the Cloud Servers API
     */
    void deleteServer(int serverID) throws CloudServersException;

    /**
     * Return the limits for the Rackspace API account.
     *
     * @return the limits
     * @throws CloudServersException if there is an exception when calling the Cloud Servers API
     */
    Limits getLimits() throws CloudServersException;

    /**
     * Retrieve the list of flavors (only IDs and names) associated with the Rackspace account.
     *
     * @return the flavors
     * @throws CloudServersException if there is an exception when calling the Cloud Servers API
     */
    List<Flavor> getFlavors() throws CloudServersException;

    /**
     * Retrieve the list of flavors (with details) associated with the Rackspace account.
     *
     * @return the flavors
     * @throws CloudServersException if there is an exception when calling the Cloud Servers API
     */
    List<Flavor> getFlavorsWithDetails() throws CloudServersException;

    /**
     * Retrieve the flavor details.
     *
     * @param flavorID the ID of the flavor for which details should be retrieved
     * @return the flavor details
     * @throws CloudServersException if there is an exception when calling the Cloud Servers API
     */
    Flavor getFlavorDetails(int flavorID) throws CloudServersException;

    /**
     * Retrieve the list of images (only IDs and names) associated with the Rackspace account.
     *
     * @return the images
     * @throws CloudServersException if there is an exception when calling the Cloud Servers API
     */
    List<Image> getImages() throws CloudServersException;

    /**
     * Retrieve the list of images (with details) associated with the Rackspace account.
     *
     * @return the images
     * @throws CloudServersException if there is an exception when calling the Cloud Servers API
     */
    List<Image> getImagesWithDetails() throws CloudServersException;

    /**
     * Retrieve the image details.
     *
     * @param imageID the ID of the image for which details should be retrieved
     * @return the image details
     * @throws CloudServersException if there is an exception when calling the Cloud Servers API
     */
    Image getImageDetails(int imageID) throws CloudServersException;

    /**
     * Create a new image from a server.
     *
     * @param name     the name of the image to create
     * @param serverID the ID of the server whose content will be used for creating the image
     * @return the created image details
     * @throws CloudServersException if there is an exception when calling the Cloud Servers API
     */
    Image createImage(String name, int serverID) throws CloudServersException;

    /**
     * Retrieve the backup schedule for a server.
     *
     * @param serverID the ID of the server for which the backup schedule should be retrieved
     * @return the backup schedule
     * @throws CloudServersException if there is an exception when calling the Cloud Servers API
     */
    BackupSchedule getBackupSchedule(int serverID) throws CloudServersException;

    /**
     * Create or update backup schedule for a server.
     *
     * @param serverID the ID of the server for which the backup schedule should be created/updated
     * @param schedule the backup schedule
     * @throws CloudServersException if there is an exception when calling the Cloud Servers API
     */
    void scheduleBackup(int serverID, BackupSchedule schedule) throws CloudServersException;

    /**
     * Delete backup schedule for a server.
     *
     * @param serverID the ID of the server for which the backup schedule should be deleted
     * @throws CloudServersException if there is an exception when calling the Cloud Servers API
     */
    void deleteBackupSchedule(int serverID) throws CloudServersException;

    /**
     * Retrieve the list of shared IP groups (only IDs and names) associated with the Rackspace account.
     *
     * @return the list of shared IP groups
     * @throws CloudServersException if there is an exception when calling the Cloud Servers API
     */
    List<SharedIPGroup> getSharedIPGroups() throws CloudServersException;

    /**
     * Retrieve the list of shared IP groups (with details) associated with the Rackspace account.
     *
     * @return the list of shared IP groups
     * @throws CloudServersException if there is an exception when calling the Cloud Servers API
     */
    List<SharedIPGroup> getSharedIPGroupsWithDetails() throws CloudServersException;

    /**
     * Retrieve the shared IP group details.
     *
     * @param groupID the ID of the shared IP group
     * @return the shared IP group
     * @throws CloudServersException if there is an exception when calling the Cloud Servers API
     */
    SharedIPGroup getSharedIPGroup(int groupID) throws CloudServersException;

    /**
     * Create a new shared IP group.
     *
     * @param name the name of the shared IP group to create
     * @return the created shared IP group
     * @throws CloudServersException if there is an exception when calling the Cloud Servers API
     * @see #createSharedIPGroup(String, int)
     */
    SharedIPGroup createSharedIPGroup(String name) throws CloudServersException;

    /**
     * Create a new shared IP group.
     *
     * @param name     the name of the shared IP group to create
     * @param serverID the first server which is going to be part of the group
     * @return the created shared IP group
     * @throws CloudServersException if there is an exception when calling the Cloud Servers API
     * @see #createSharedIPGroup(String)
     */
    SharedIPGroup createSharedIPGroup(String name, int serverID) throws CloudServersException;

    /**
     * Delete shared IP group.
     *
     * @param groupID the ID of the shared IP group
     * @throws CloudServersException if there is an exception when calling the Cloud Servers API
     */
    void deleteSharedIPGroup(int groupID) throws CloudServersException;

}
