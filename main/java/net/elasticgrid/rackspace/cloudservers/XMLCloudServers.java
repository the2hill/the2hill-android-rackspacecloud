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

import net.elasticgrid.rackspace.cloudservers.Server.Status;
import net.elasticgrid.rackspace.cloudservers.internal.ConfirmResize;
import net.elasticgrid.rackspace.cloudservers.internal.Flavors;
import net.elasticgrid.rackspace.cloudservers.internal.Images;
import net.elasticgrid.rackspace.cloudservers.internal.Metadata;
import net.elasticgrid.rackspace.cloudservers.internal.MetadataItem;
import net.elasticgrid.rackspace.cloudservers.internal.Private;
import net.elasticgrid.rackspace.cloudservers.internal.Public;
import net.elasticgrid.rackspace.cloudservers.internal.Reboot;
import net.elasticgrid.rackspace.cloudservers.internal.Rebuild;
import net.elasticgrid.rackspace.cloudservers.internal.Resize;
import net.elasticgrid.rackspace.cloudservers.internal.RevertResize;
import net.elasticgrid.rackspace.cloudservers.internal.ServerID;
import net.elasticgrid.rackspace.cloudservers.internal.Servers;
import net.elasticgrid.rackspace.cloudservers.internal.SharedIpGroup;
import net.elasticgrid.rackspace.cloudservers.internal.SharedIpGroups;
import net.elasticgrid.rackspace.cloudservers.internal.ShareIp;
import net.elasticgrid.rackspace.common.RackspaceConnection;
import net.elasticgrid.rackspace.common.RackspaceException;
import org.apache.http.HttpException;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentProducer;
import org.apache.http.entity.EntityTemplate;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IMarshallingContext;
import org.jibx.runtime.JiBXException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation based on the XML documents.
 *
 * @author Jerome Bernard
 */
public class XMLCloudServers extends RackspaceConnection implements CloudServers {
    private static final Logger logger = Logger.getLogger(XMLCloudServers.class.getName());

    /**
     * Initializes the Rackspace Cloud Servers connection with the Rackspace login information.
     *
     * @param username the Rackspace username
     * @param apiKey   the Rackspace API key
     * @throws RackspaceException if the credentials are invalid
     * @throws IOException        if there is a network issue
     * @see #authenticate()
     */
    public XMLCloudServers(String username, String apiKey) throws RackspaceException, IOException {
        super(username, apiKey);
    }

    public List<Server> getServers() throws CloudServersException {
        logger.info("Retrieving servers information...");
        HttpGet request = new HttpGet(getServerManagementURL() + "/servers");
        return buildListOfServers(request);
    }

    public List<Server> getServersWithDetails() throws CloudServersException {
        logger.info("Retrieving detailed servers information...");
        HttpGet request = new HttpGet(getServerManagementURL() + "/servers/detail");
        return buildListOfServers(request);
    }

    private List<Server> buildListOfServers(HttpGet request) throws CloudServersException {
        Servers response = makeRequestInt(request, Servers.class);
        List<Server> servers = new ArrayList<Server>(response.getServers().size());
        for (net.elasticgrid.rackspace.cloudservers.internal.Server server : response.getServers())
            servers.add(buildServer(server));
        return servers;
    }

    public Server getServerDetails(int serverID) throws CloudServersException {
        logger.log(Level.INFO, "Retrieving detailed information for server {0}...", serverID);
        validateServerID(serverID);
        HttpGet request = new HttpGet(getServerManagementURL() + "/servers/" + serverID);
        return buildServer(makeRequestInt(request, net.elasticgrid.rackspace.cloudservers.internal.Server.class));
    }

    private Server buildServer(net.elasticgrid.rackspace.cloudservers.internal.Server response) throws CloudServersException {
        try {
            return new Server(
                    response.getId(), response.getName(), response.getAdminPass(),
                    response.getImageId(), response.getFlavorId(),
                    response.getStatus() == null ? null : Status.valueOf(response.getStatus().name()),
                    metadataAsMap(response.getMetadata()),
                    new Addresses(response.getAddresses()),
                    new Personality(response.getPersonality())
            );
        } catch (UnknownHostException e) {
            throw new CloudServersException("Can't build server", e);
        }
    }

    private static Map<String, String> metadataAsMap(Metadata metadata) {
        if (metadata == null)
            return Collections.emptyMap();
        Map<String, String> meta = new HashMap<String, String>();
        for (MetadataItem item : metadata.getMetadatas()) {
            meta.put(item.getKey(), item.getString());
        }
        return meta;
    }

    public Addresses getServerAddresses(int serverID) throws CloudServersException {
        logger.log(Level.INFO, "Retrieving all IP addresses of server {0}...", serverID);
        validateServerID(serverID);
        HttpGet request = new HttpGet(getServerManagementURL() + "/servers/" + serverID + "/ips");
        net.elasticgrid.rackspace.cloudservers.internal.Addresses response = makeRequestInt(request, net.elasticgrid.rackspace.cloudservers.internal.Addresses.class);
        try {
            return new Addresses(response);
        } catch (UnknownHostException e) {
            throw new CloudServersException("Can't validate server addresses", e);
        }
    }

    public List<InetAddress> getServerPublicAddresses(int serverID) throws CloudServersException {
        logger.log(Level.INFO, "Retrieving all public IP addresses of server {0}...", serverID);
        validateServerID(serverID);
        HttpGet request = new HttpGet(getServerManagementURL() + "/servers/" + serverID + "/ips/public");
        Public response = makeRequestInt(request, Public.class);
        try {
            List<InetAddress> addresses = new ArrayList<InetAddress>(response.getAddressLists().size());
            for (net.elasticgrid.rackspace.cloudservers.internal.Address address : response.getAddressLists()) {
                addresses.add(InetAddress.getByName(address.getAddr()));
            }
            return addresses;
        } catch (UnknownHostException e) {
            throw new CloudServersException("Can't validate server addresses", e);
        }
    }

    public List<InetAddress> getServerPrivateAddresses(int serverID) throws CloudServersException {
        logger.log(Level.INFO, "Retrieving all private IP addresses of server {0}...", serverID);
        validateServerID(serverID);
        HttpGet request = new HttpGet(getServerManagementURL() + "/servers/" + serverID + "/ips/private");
        Private response = makeRequestInt(request, Private.class);
        try {
            List<InetAddress> addresses = new ArrayList<InetAddress>(response.getAddressLists().size());
            for (net.elasticgrid.rackspace.cloudservers.internal.Address address : response.getAddressLists()) {
                addresses.add(InetAddress.getByName(address.getAddr()));
            }
            return addresses;
        } catch (UnknownHostException e) {
            throw new CloudServersException("Can't validate server addresses", e);
        }
    }

    public void shareAddress(int groupID, int serverID, InetAddress address) throws CloudServersException {
        shareAddress(groupID, serverID, address, false);
    }

    public void shareAddress(int groupID, int serverID, InetAddress address, boolean configureServer) throws CloudServersException {
        logger.log(Level.INFO, "Sharing IP in group {0} for address {1} with server {2}...", new Object[]{groupID, address, serverID});
        validateServerID(serverID);
        if (address == null)
            throw new IllegalArgumentException("Invalid IP address");
        ShareIp shareIp = new ShareIp();
        shareIp.setSharedIpGroupId(groupID);
        shareIp.setConfigureServer(configureServer);
        HttpPut request = new HttpPut(getServerManagementURL() + "/servers/" + serverID
                + "/ips/public/" + address.getHostAddress());
        makeRequestInt(request);
    }

    public void unshareAddress(int serverID, InetAddress address) throws CloudServersException {
        logger.log(Level.INFO, "Unsharing IP address {0} with server {1}...", new Object[]{address, serverID});
        validateServerID(serverID);
        if (address == null)
            throw new IllegalArgumentException("Invalid IP address");
        HttpDelete request = new HttpDelete(getServerManagementURL() + "/servers/" + serverID
                + "/ips/public/" + address.getHostAddress());
        makeRequestInt(request);
    }

    public Server createServer(String name, int imageID, int flavorID) throws CloudServersException {
        return createServer(name, imageID, flavorID, null);
    }

    public Server createServer(String name, int imageID, int flavorID, Map<String, String> metadata) throws CloudServersException {
        logger.log(Level.INFO, "Creating server {0} from image {1} running on flavor {2}...",
                new Object[]{name, imageID, flavorID});
        if (name == null)
            throw new IllegalArgumentException("Server name has to be specified!");
        if (imageID == 0)
            throw new IllegalArgumentException("Image ID has to be specified!");
        if (flavorID == 0)
            throw new IllegalArgumentException("Flavor ID has to be specified!");
        HttpPost request = new HttpPost(getServerManagementURL() + "/servers");
        net.elasticgrid.rackspace.cloudservers.internal.Server server = new net.elasticgrid.rackspace.cloudservers.internal.Server();
        server.setName(name);
        server.setImageId(imageID);
        server.setFlavorId(flavorID);
        if (metadata != null && !metadata.isEmpty()) {
            Metadata rawMetadata = new Metadata();
            List<MetadataItem> metadataItems = rawMetadata.getMetadatas();
            for (Map.Entry<String, String> entry : metadata.entrySet()) {
                MetadataItem item = new MetadataItem();
                item.setKey(entry.getKey());
                item.setString(entry.getValue());
                metadataItems.add(item);
            }
            server.setMetadata(rawMetadata);
        }
        return buildServer(makeEntityRequestInt(request, server, net.elasticgrid.rackspace.cloudservers.internal.Server.class));
    }

    public void rebootServer(int serverID) throws CloudServersException {
        rebootServer(serverID, RebootType.SOFT);
    }

    public void rebootServer(int serverID, RebootType type) throws CloudServersException {
        logger.log(Level.INFO, "Rebooting server {0} via {1} reboot...", new Object[]{serverID, type.name()});
        validateServerID(serverID);
        HttpPost request = new HttpPost(getServerManagementURL() + "/servers/" + serverID + "/action");
        Reboot reboot = new Reboot();
        reboot.setType(net.elasticgrid.rackspace.cloudservers.internal.RebootType.valueOf(type.name()));
        makeEntityRequestInt(request, reboot);
    }

    public void rebuildServer(int serverID) throws CloudServersException {
        logger.log(Level.INFO, "Rebuilding server {0}...", serverID);
        validateServerID(serverID);
        HttpPost request = new HttpPost(getServerManagementURL() + "/servers/" + serverID + "/action");
        makeEntityRequestInt(request, new Rebuild());
    }

    public void rebuildServer(int serverID, int imageID) throws CloudServersException {
        logger.log(Level.INFO, "Rebuilding server {0} from image {1}...", new Object[]{serverID, imageID});
        validateServerID(serverID);
        HttpPost request = new HttpPost(getServerManagementURL() + "/servers/" + serverID + "/action");
        Rebuild rebuild = new Rebuild();
        rebuild.setImageId(imageID);
        makeEntityRequestInt(request, rebuild);
    }

    public void resizeServer(int serverID, int flavorID) throws CloudServersException {
        logger.log(Level.INFO, "Resizing server {0} to run on flavor {1}...", new Object[]{serverID, flavorID});
        validateServerID(serverID);
        HttpPost request = new HttpPost(getServerManagementURL() + "/servers/" + serverID + "/action");
        Resize resize = new Resize();
        resize.setFlavorId(flavorID);
        makeEntityRequestInt(request, resize);
    }

    public void confirmResize(int serverID) throws CloudServersException {
        logger.log(Level.INFO, "Confirming resize of server {0}...", serverID);
        validateServerID(serverID);
        HttpPost request = new HttpPost(getServerManagementURL() + "/servers/" + serverID + "/action");
        makeEntityRequestInt(request, new ConfirmResize());
    }

    public void revertResize(int serverID) throws CloudServersException {
        logger.log(Level.INFO, "Cancelling resize of server {0}...", serverID);
        validateServerID(serverID);
        HttpPost request = new HttpPost(getServerManagementURL() + "/servers/" + serverID + "/action");
        makeEntityRequestInt(request, new RevertResize());
    }

    public void updateServerName(int serverID, String name) throws CloudServersException {
        updateServerNameAndPassword(serverID, name, null);
    }

    public void updateServerPassword(int serverID, String password) throws CloudServersException {
        updateServerNameAndPassword(serverID, null, password);
    }

    public void updateServerNameAndPassword(final int serverID, final String name, final String password) throws CloudServersException {
        validateServerID(serverID);
        HttpPut request = new HttpPut(getServerManagementURL() + "/servers/" + serverID);
        net.elasticgrid.rackspace.cloudservers.internal.Server server = new net.elasticgrid.rackspace.cloudservers.internal.Server();
        server.setId(serverID);
        if (name != null)
            server.setName(name);
        if (password != null)
            server.setAdminPass(password);
        makeEntityRequestInt(request, server);
    }

    public void deleteServer(int serverID) throws CloudServersException {
        logger.log(Level.INFO, "Deleting server {0}...", serverID);
        validateServerID(serverID);
        HttpDelete request = new HttpDelete(getServerManagementURL() + "/servers/" + serverID);
        makeRequestInt(request);
    }

    public Limits getLimits() throws CloudServersException {
        HttpGet request = new HttpGet(getServerManagementURL() + "/limits");
        net.elasticgrid.rackspace.cloudservers.internal.Limits response = makeRequestInt(request, net.elasticgrid.rackspace.cloudservers.internal.Limits.class);
        List<RateLimit> rateLimits = new ArrayList<RateLimit>(response.getRate().getRateLimits().size());
        for (net.elasticgrid.rackspace.cloudservers.internal.RateLimit limit : response.getRate().getRateLimits())
            rateLimits.add(new RateLimit(
                    HTTPVerb.valueOf(limit.getVerb().name()),
                    limit.getURI(),
                    limit.getRegex(),
                    limit.getValue(),
                    limit.getRemaining(),
                    RateLimit.Unit.valueOf(limit.getUnit().name()),
                    limit.getResetTime()
            ));
        List<AbsoluteLimit> absoluteLimits = new ArrayList<AbsoluteLimit>(response.getAbsolute().getAbsoluteLimits().size());
        for (net.elasticgrid.rackspace.cloudservers.internal.AbsoluteLimit limit : response.getAbsolute().getAbsoluteLimits())
            absoluteLimits.add(new AbsoluteLimit(limit.getName(), limit.getValue()));
        return new Limits(rateLimits, absoluteLimits);
    }

    public List<Flavor> getFlavors() throws CloudServersException {
        logger.info("Retrieving flavors information...");
        HttpGet request = new HttpGet(getServerManagementURL() + "/flavors");
        return buildListOfFlavors(request);
    }

    public List<Flavor> getFlavorsWithDetails() throws CloudServersException {
        logger.info("Retrieving detailed flavors information...");
        HttpGet request = new HttpGet(getServerManagementURL() + "/flavors/detail");
        return buildListOfFlavors(request);
    }

    public Flavor getFlavorDetails(int flavorID) throws CloudServersException {
        logger.log(Level.INFO, "Retrieving detailed information for flavor {0}...", flavorID);
        validateFlavorID(flavorID);
        HttpGet request = new HttpGet(getServerManagementURL() + "/flavors/" + flavorID);
        return buildFlavor(makeRequestInt(request, net.elasticgrid.rackspace.cloudservers.internal.Flavor.class));
    }

    private List<Flavor> buildListOfFlavors(HttpGet request) throws CloudServersException {
        Flavors response = makeRequestInt(request, Flavors.class);
        List<Flavor> flavors = new ArrayList<Flavor>(response.getFlavors().size());
        for (net.elasticgrid.rackspace.cloudservers.internal.Flavor flavor : response.getFlavors())
            flavors.add(buildFlavor(flavor));
        return flavors;
    }

    private Flavor buildFlavor(net.elasticgrid.rackspace.cloudservers.internal.Flavor response) {
        return new Flavor(response.getId(), response.getName(), response.getRam(), response.getDisk());
    }

    public List<Image> getImages() throws CloudServersException {
        logger.info("Retrieving images information...");
        HttpGet request = new HttpGet(getServerManagementURL() + "/images");
        return buildListOfImages(makeRequestInt(request, Images.class));
    }

    public List<Image> getImagesWithDetails() throws CloudServersException {
        logger.info("Retrieving detailed images information...");
        HttpGet request = new HttpGet(getServerManagementURL() + "/images/detail");
        return buildListOfImages(makeRequestInt(request, Images.class));
    }

    public Image getImageDetails(int imageID) throws CloudServersException {
        logger.log(Level.INFO, "Retrieving detailed information for image {0}...", imageID);
        validateImageID(imageID);
        HttpGet request = new HttpGet(getServerManagementURL() + "/images/" + imageID);
        return buildImage(makeRequestInt(request, net.elasticgrid.rackspace.cloudservers.internal.Image.class));
    }

    public Image createImage(String name, int serverID) throws CloudServersException {
        logger.log(Level.INFO, "Creating image named ''{0}'' from server {1}...", new Object[]{name, serverID});
        validateServerID(serverID);
        HttpPost request = new HttpPost(getServerManagementURL() + "/images");
        net.elasticgrid.rackspace.cloudservers.internal.Image image = new net.elasticgrid.rackspace.cloudservers.internal.Image();
        image.setName(name);
        image.setServerId(serverID);
        return buildImage(makeEntityRequestInt(request, image, net.elasticgrid.rackspace.cloudservers.internal.Image.class));
    }

    private List<Image> buildListOfImages(Images response) {
        List<Image> images = new ArrayList<Image>(response.getImages().size());
        for (net.elasticgrid.rackspace.cloudservers.internal.Image image : response.getImages())
            images.add(buildImage(image));
        return images;
    }

    private Image buildImage(net.elasticgrid.rackspace.cloudservers.internal.Image created) {
        return new Image(
                created.getId(), created.getName(), created.getServerId(),
                created.getUpdated(), created.getCreated(), created.getProgress(),
                created.getStatus() == null ? null : Image.Status.valueOf(created.getStatus().name())
        );
    }

    public BackupSchedule getBackupSchedule(int serverID) throws CloudServersException {
        logger.log(Level.INFO, "Retrieving backup schedule for server {0}...", serverID);
        validateServerID(serverID);
        HttpGet request = new HttpGet(getServerManagementURL() + "/servers/" + serverID + "/backup_schedule");
        net.elasticgrid.rackspace.cloudservers.internal.BackupSchedule response = makeRequestInt(request, net.elasticgrid.rackspace.cloudservers.internal.BackupSchedule.class);
        return new BackupSchedule(
                response.getEnabled(),
                BackupSchedule.WeeklyBackup.valueOf(response.getWeekly().name()),
                BackupSchedule.DailyBackup.valueOf(response.getDaily().name())
        );
    }

    public void scheduleBackup(int serverID, BackupSchedule schedule) throws CloudServersException {
        logger.log(Level.INFO, "Updating backup schedule for server {0} to {1}...",
                new Object[]{serverID, schedule});
        validateServerID(serverID);
        HttpPost request = new HttpPost(getServerManagementURL() + "/servers/" + serverID + "/backup_schedule");
        net.elasticgrid.rackspace.cloudservers.internal.BackupSchedule s = new net.elasticgrid.rackspace.cloudservers.internal.BackupSchedule();
        s.setEnabled(schedule.isEnabled());
        s.setWeekly(net.elasticgrid.rackspace.cloudservers.internal.WeeklyBackup.valueOf(schedule.getWeekly().name()));
        s.setDaily(net.elasticgrid.rackspace.cloudservers.internal.DailyBackup.valueOf(schedule.getDaily().name()));
        makeEntityRequestInt(request, s);
    }

    public void deleteBackupSchedule(int serverID) throws CloudServersException {
        logger.log(Level.INFO, "Deleting backup schedule for server {0}...", serverID);
        validateServerID(serverID);
        HttpDelete request = new HttpDelete(getServerManagementURL() + "/servers/" + serverID + "/backup_schedule");
        makeRequestInt(request);
    }

    public List<SharedIPGroup> getSharedIPGroups() throws CloudServersException {
        logger.info("Retrieving shared IP groups information...");
        HttpGet request = new HttpGet(getServerManagementURL() + "/shared_ip_groups");
        return buildListOfSharedIPGroups(request);
    }

    public List<SharedIPGroup> getSharedIPGroupsWithDetails() throws CloudServersException {
        logger.info("Retrieving detailed shared IP groups information...");
        HttpGet request = new HttpGet(getServerManagementURL() + "/shared_ip_groups/detail");
        return buildListOfSharedIPGroups(request);
    }

    public SharedIPGroup getSharedIPGroup(int groupID) throws CloudServersException {
        logger.log(Level.INFO, "Retrieving detailed shared IP group information for {0}...", groupID);
        HttpGet request = new HttpGet(getServerManagementURL() + "/shared_ip_groups/" + groupID);
        return buildSharedIPGroup(makeRequestInt(request, SharedIpGroup.class));
    }

    public SharedIPGroup createSharedIPGroup(String name) throws CloudServersException {
        return createSharedIPGroup(name, 0);
    }

    public SharedIPGroup createSharedIPGroup(String name, int serverID) throws CloudServersException {
        logger.log(Level.INFO, "Creating shared IP group named {0} for server {1}...", new Object[]{name, serverID});
        HttpPost request = new HttpPost(getServerManagementURL() + "/shared_ip_groups");
        SharedIpGroup group = new SharedIpGroup();
        group.setName(name);
        if (serverID != 0) {
            ServerID server = new ServerID();
            server.setId(serverID);
            group.setServer(server);
        }
        return buildSharedIPGroup(makeEntityRequestInt(request, group, SharedIpGroup.class));
    }

    public void deleteSharedIPGroup(int groupID) throws CloudServersException {
        logger.log(Level.INFO, "Deleting shared IP group {0}...", groupID);
        validateSharedIPGroupID(groupID);
        HttpDelete request = new HttpDelete(getServerManagementURL() + "/shared_ip_groups/" + groupID);
        makeRequestInt(request);
    }

    private List<SharedIPGroup> buildListOfSharedIPGroups(HttpGet request) throws CloudServersException {
        SharedIpGroups response = makeRequestInt(request, SharedIpGroups.class);
        List<SharedIPGroup> groups = new ArrayList<SharedIPGroup>(response.getSharedIpGroups().size());
        for (SharedIpGroup group : response.getSharedIpGroups())
            groups.add(buildSharedIPGroup(group));
        return groups;
    }

    private SharedIPGroup buildSharedIPGroup(SharedIpGroup group) {
        List<Integer> serverIDs = new ArrayList<Integer>(group.getServers().getServerIDLists().size());
        for (ServerID id : group.getServers().getServerIDLists())
            serverIDs.add(id.getId());
        return new SharedIPGroup(group.getId(), group.getName(), serverIDs);
    }

    protected void makeEntityRequestInt(HttpEntityEnclosingRequestBase request, final Object entity) throws CloudServersException {
        makeEntityRequestInt(request, entity, Void.class);
    }

    protected <T> T makeEntityRequestInt(HttpEntityEnclosingRequestBase request, final Object entity, Class<T> respType) throws CloudServersException {
        request.setEntity(new EntityTemplate(new ContentProducer() {
            public void writeTo(OutputStream output) throws IOException {
                try {
                    IBindingFactory bindingFactory = BindingDirectory.getFactory(entity.getClass());
                    final IMarshallingContext marshallingCxt = bindingFactory.createMarshallingContext();
                    marshallingCxt.marshalDocument(entity, "UTF-8", true, output);
                } catch (JiBXException e) {
                    IOException ioe = new IOException("Can't marshal server details");
                    ioe.initCause(e);
                    e.printStackTrace();
                    throw ioe;
                }
            }
        }));
        return makeRequestInt(request, respType);
    }

    protected void makeRequestInt(HttpRequestBase request) throws CloudServersException {
        makeRequestInt(request, Void.class);
    }

    protected <T> T makeRequestInt(HttpRequestBase request, Class<T> respType) throws CloudServersException {
        try {
            return makeRequest(request, respType);
        } catch (RackspaceException e) {
            throw new CloudServersException(e);
        } catch (JiBXException e) {
            throw new CloudServersException("Problem parsing returned message.", e);
        } catch (MalformedURLException e) {
            throw new CloudServersException(e.getMessage(), e);
        } catch (IOException e) {
            throw new CloudServersException(e.getMessage(), e);
        } catch (HttpException e) {
            throw new CloudServersException(e.getMessage(), e);
        }
    }

    private void validateServerID(int serverID) throws IllegalArgumentException {
        if (serverID == 0)
            throw new IllegalArgumentException("Invalid serverID " + serverID);
    }

    private void validateFlavorID(int flavorID) throws IllegalArgumentException {
        if (flavorID == 0)
            throw new IllegalArgumentException("Invalid flavorID " + flavorID);
    }

    private void validateImageID(int imageID) throws IllegalArgumentException {
        if (imageID == 0)
            throw new IllegalArgumentException("Invalid imageID " + imageID);
    }

    private void validateSharedIPGroupID(int groupID) throws IllegalArgumentException {
        if (groupID == 0)
            throw new IllegalArgumentException("Invalid shared IP group ID " + groupID);
    }
}
