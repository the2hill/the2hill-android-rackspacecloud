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
package net.elasticgrid.rackspace.common;

import net.elasticgrid.rackspace.cloudservers.internal.CloudServersAPIFault;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.HttpEntityWrapper;
import org.apache.http.entity.EntityTemplate;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HttpContext;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

/**
 * This class provides common code to the REST connection classes.
 * Logging:
 * <table>
 *   <thead>
 *     <th>Level</th>
 *     <th align="left">Information</th>
 *   </thead>
 *   <tbody>
 *     <tr>
 *       <td>WARNING</td>
 *       <td>Retries, Expired Authentication before the request is automatically retried</td>
 *     </tr>
 *     <tr>
 *       <td>INFO</td>
 *       <td>Request URI &amp; Method</td>
 *     </tr>
 *     <tr>
 *       <td>FINEST</td>
 *       <td>Request Body, Response Body</td>
 *     </tr>
 *   </tbody>
 * </table>
 *
 * @author Jerome Bernard
 */
public class RackspaceConnection {
    // this is the number of automatic retries
    private int maxRetries = 5;
    private String userAgent = "Elastic-Grid/";
    private HttpClient hc = null;
    private int maxConnections = 100;
    private String proxyHost = null;
    private int proxyPort;
    private int connectionManagerTimeout = 0;
    private int soTimeout = 0;
    private int connectionTimeout = 0;

    private final String username;
    private final String apiKey;
    private String serverManagementURL;
    private String storageURL;
    private String cdnManagementURL;
    private String authToken;

    private boolean authenticated = false;

    private static final String API_AUTH_URL = "https://auth.api.rackspacecloud.com/v1.0";

    private static final Logger logger = Logger.getLogger(RackspaceConnection.class.getName());

    /**
     * Initializes the Rackspace connection with the Rackspace login information.
     *
     * @param username the Rackspace username
     * @param apiKey   the Rackspace API key
     * @throws RackspaceException if the credentials are invalid
     * @throws IOException        if there is a network issue
     * @see #authenticate()
     */
    public RackspaceConnection(String username, String apiKey) throws RackspaceException, IOException {
        this.username = username;
        this.apiKey = apiKey;
        String version;
        try {
            Properties props = new Properties();
            props.load(this.getClass().getClassLoader().getResourceAsStream("version.properties"));
            version = props.getProperty("version");
        } catch (Exception ex) {
            version = "?";
        }
        userAgent = userAgent + version + " (" + System.getProperty("os.arch") + "; " + System.getProperty("os.name") + ")";
        authenticate();
    }

    /**
     * Authenticate on Rackspace API. Tokens are only valid for 24 hours, so client code should expect token to expire
     * and renew them if needed.
     *
     * @return the auth token, valid for 24 hours
     * @throws RackspaceException if the credentials are invalid
     * @throws IOException        if there is a network issue
     */
    public String authenticate() throws RackspaceException, IOException {
        logger.info("Authenticating to Rackspace API...");
        HttpGet request = new HttpGet(API_AUTH_URL);
        request.addHeader("X-Auth-User", username);
        request.addHeader("X-Auth-Key", apiKey);
        HttpResponse response = getHttpClient().execute(request);
        int statusCode = response.getStatusLine().getStatusCode();
        switch (statusCode) {
            case 204:
                if (response.getFirstHeader("X-Server-Management-Url") != null)
                    serverManagementURL = response.getFirstHeader("X-Server-Management-Url").getValue();
                if (response.getFirstHeader("X-Storage-Url") != null)
                    storageURL = response.getFirstHeader("X-Storage-Url").getValue();
                if (response.getFirstHeader("X-CDN-Management-Url") != null)
                    cdnManagementURL = response.getFirstHeader("X-CDN-Management-Url").getValue();
                authToken = response.getFirstHeader("X-Auth-Token").getValue();
                authenticated = true;
                return authToken;
            case 401:
                throw new RackspaceException("Invalid credentials: " + response.getStatusLine().getReasonPhrase());
            default:
                throw new RackspaceException("Unexpected HTTP response");
        }
    }

    /**
     * Make a http request and process the response. This method also performs automatic retries.
     *
     * @param request  the HTTP method to use (GET, POST, DELETE, etc)
     * @param respType the class that represents the desired/expected return type
     * @return the unmarshalled entity
     * @throws RackspaceException
     * @throws IOException        if there is an I/O exception
     * @throws HttpException      if there is an HTTP exception
     * @throws JiBXException      if the result can't be unmarshalled
     */
    @SuppressWarnings("unchecked")
    protected <T> T makeRequest(HttpRequestBase request, Class<T> respType)
            throws HttpException, IOException, JiBXException, RackspaceException {

        if (!authenticated)
            authenticate();

        // add auth params, and protocol specific headers
        request.addHeader("X-Auth-Token", getAuthToken());

        // set accept and content-type headers
        request.setHeader("Accept", "application/xml; charset=UTF-8");
        request.setHeader("Accept-Encoding", "gzip");
        request.setHeader("Content-Type", "application/xml; charset=UTF-8");

        // send the request
        T result = null;
        boolean done = false;
        int retries = 0;
        boolean doRetry = false;
        RackspaceException error = null;
        do {
            HttpResponse response = null;
            if (retries > 0)
                logger.log(Level.INFO, "Retry #{0}: querying via {1} {2}",
                        new Object[]{retries, request.getMethod(), request.getURI()});
            else
                logger.log(Level.INFO, "Querying via {0} {1}", new Object[]{request.getMethod(), request.getURI()});

            if (logger.isLoggable(Level.FINEST) && request instanceof HttpEntityEnclosingRequestBase) {
                HttpEntity entity = ((HttpEntityEnclosingRequestBase) request).getEntity();
                if (entity instanceof EntityTemplate) {
                    EntityTemplate template = (EntityTemplate) entity;
                    ByteArrayOutputStream baos = null;
                    try {
                        baos = new ByteArrayOutputStream();
                        template.writeTo(baos);
                        logger.log(Level.FINEST, "Request body:\n{0}", baos.toString());
                    } finally {
                        IOUtils.closeQuietly(baos);
                    }
                }
            }

            InputStream entityStream = null;
            HttpEntity entity = null;

            if (logger.isLoggable(Level.FINEST)) {
                response = getHttpClient().execute(request);
                entity = response.getEntity();
                try {
                    entityStream = entity.getContent();
                    logger.log(Level.FINEST, "Response body on " + request.getURI()
                            + " via " + request.getMethod() + ":\n" + IOUtils.toString(entityStream));
                } finally {
                    IOUtils.closeQuietly(entityStream);
                }
            }

            response = getHttpClient().execute(request);
            int statusCode = response.getStatusLine().getStatusCode();
            entity = response.getEntity();

            switch (statusCode) {
                case 200:
                case 202:
                case 203:
                    try {
                        entityStream = entity.getContent();
                        IBindingFactory bindingFactory = BindingDirectory.getFactory(respType);
                        IUnmarshallingContext unmarshallingCxt = bindingFactory.createUnmarshallingContext();
                        result = (T) unmarshallingCxt.unmarshalDocument(entityStream, "UTF-8");
                    } finally {
                        entity.consumeContent();
                        IOUtils.closeQuietly(entityStream);
                    }
                    done = true;
                    break;
                case 503:   // service unavailable
                    logger.log(Level.WARNING, "Service unavailable on {0} via {1}. Will retry in {2} seconds.",
                            new Object[]{request.getURI(), request.getMethod(), Math.pow(2.0, retries + 1)});
                    doRetry = true;
                    break;
                case 401:   // unauthorized
                    logger.warning("Not authenticated or authentication token expired. Authenticating...");
                    authenticate();
                    doRetry = true;
                    break;
                case 417:
                    throw new RackspaceException(new IllegalArgumentException("Some parameters are invalid!")); // TODO: temp hack 'til Rackspace API is fixed!
                case 400:
                case 500:
                default:
                    try {
                        entityStream = entity.getContent();
                        IBindingFactory bindingFactory = BindingDirectory.getFactory(CloudServersAPIFault.class);
                        IUnmarshallingContext unmarshallingCxt = bindingFactory.createUnmarshallingContext();
                        CloudServersAPIFault fault = (CloudServersAPIFault) unmarshallingCxt.unmarshalDocument(entityStream, "UTF-8");
                        done = true;
                        throw new RackspaceException(fault.getCode(), fault.getMessage(), fault.getDetails());
                    } catch (JiBXException e) {
                        response = getHttpClient().execute(request);
                        entity = response.getEntity();
                        entityStream = entity.getContent();
                        logger.log(Level.SEVERE, "Can't unmarshal response from " + request.getURI()
                                + " via " + request.getMethod() + ":" + IOUtils.toString(entityStream));
                        e.printStackTrace();
                        throw e;
                    } finally {
                        entity.consumeContent();
                        IOUtils.closeQuietly(entityStream);
                    }
            }

            if (doRetry) {
                retries++;
                if (retries > maxRetries) {
                    throw new HttpException("Number of retries exceeded for " + request.getURI(), error);
                }
                doRetry = false;
                try {
                    Thread.sleep((int) Math.pow(2.0, retries) * 1000);
                } catch (InterruptedException ex) {
                    // do nothing
                }
            }
        } while (!done);

        return result;
    }

    private void configureHttpClient() {
        HttpParams params = new BasicHttpParams();

        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(params, "UTF-8");
        HttpProtocolParams.setUserAgent(params, userAgent);
        HttpProtocolParams.setUseExpectContinue(params, true);

//        params.setBooleanParameter("http.tcp.nodelay", true);
//        params.setBooleanParameter("http.coonection.stalecheck", false);
        ConnManagerParams.setTimeout(params, getConnectionManagerTimeout());
        ConnManagerParams.setMaxTotalConnections(params, getMaxConnections());
        ConnManagerParams.setMaxConnectionsPerRoute(params, new ConnPerRouteBean(getMaxConnections()));
        params.setIntParameter("http.socket.timeout", getSoTimeout());
        params.setIntParameter("http.connection.timeout", getConnectionTimeout());

        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
        ClientConnectionManager connMgr = new ThreadSafeClientConnManager(params, schemeRegistry);
        hc = new DefaultHttpClient(connMgr, params);

        ((DefaultHttpClient) hc).addRequestInterceptor(new HttpRequestInterceptor() {
            public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
                if (!request.containsHeader("Accept-Encoding")) {
                    request.addHeader("Accept-Encoding", "gzip");
                }
            }
        });
        ((DefaultHttpClient) hc).addResponseInterceptor(new HttpResponseInterceptor() {
            public void process(HttpResponse response, HttpContext context) throws HttpException, IOException {
                HttpEntity entity = response.getEntity();
                if (entity == null)
                    return;
                Header ceHeader = entity.getContentEncoding();
                if (ceHeader != null) {
                    for (HeaderElement codec : ceHeader.getElements()) {
                        if (codec.getName().equalsIgnoreCase("gzip")) {
                            response.setEntity(new GzipDecompressingEntity(response.getEntity()));
                            return;
                        }
                    }
                }
            }
        });

        if (proxyHost != null) {
            HttpHost proxy = new HttpHost(proxyHost, proxyPort);
            hc.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
            logger.info("Proxy Host set to " + proxyHost + ":" + proxyPort);
        }
    }

    public String getAuthToken() {
        return authToken;
    }

    public String getServerManagementURL() {
        return serverManagementURL;
    }

    public String getStorageURL() {
        return storageURL;
    }

    public String getCdnManagementURL() {
        return cdnManagementURL;
    }

    protected HttpClient getHttpClient() {
        if (hc == null) {
            configureHttpClient();
        }
        return hc;
    }

    public void setHttpClient(HttpClient hc) {
        this.hc = hc;
    }

    public int getConnectionManagerTimeout() {
        return connectionManagerTimeout;
    }

    public void setConnectionManagerTimeout(int timeout) {
        connectionManagerTimeout = timeout;
        hc = null;
    }

    public int getSoTimeout() {
        return soTimeout;
    }

    public void setSoTimeout(int timeout) {
        soTimeout = timeout;
        hc = null;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(int timeout) {
        connectionTimeout = timeout;
        hc = null;
    }

    public int getMaxConnections() {
        return maxConnections;
    }

    public void setMaxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
        hc = null;
    }

    public String getProxyHost() {
        return proxyHost;
    }

    public void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
        hc = null;
    }

    public int getProxyPort() {
        return proxyPort;
    }

    public void setProxyPort(int proxyPort) {
        this.proxyPort = proxyPort;
        hc = null;
    }

    static class GzipDecompressingEntity extends HttpEntityWrapper {

        public GzipDecompressingEntity(final HttpEntity entity) {
            super(entity);
        }

        @Override
        public InputStream getContent() throws IOException, IllegalStateException {
            // the wrapped entity's getContent() decides about repeatability
            InputStream wrappedin = wrappedEntity.getContent();
            return new GZIPInputStream(wrappedin);
        }

        @Override
        public long getContentLength() {
            // length of ungzipped content is not known
            return -1;
        }

    }
}
