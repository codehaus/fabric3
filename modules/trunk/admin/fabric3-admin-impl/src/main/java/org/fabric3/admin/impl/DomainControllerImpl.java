/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the “License”), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an “as is” basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 */
package org.fabric3.admin.impl;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.management.JMException;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import org.fabric3.admin.api.AdministrationException;
import org.fabric3.admin.api.ContributionAlreadyInstalledException;
import org.fabric3.admin.api.DomainController;
import org.fabric3.admin.api.InvalidContributionException;

/**
 * Default implementation of the DomainController API.
 *
 * @version $Revision$ $Date$
 */
public class DomainControllerImpl implements DomainController {
    private static final String CONTRIBUTION_SERVICE_MBEAN =
            "f3-management:SubDomain=null,type=service,component=\"fabric3://runtime/ContibutionHandler\",service=ContributionServiceMBean";
    private String username;
    private String password;
    private String controllerAddress = "service:jmx:rmi:///jndi/rmi://localhost:1099/server";
    private JMXConnector jmxc;

    public void setControllerAddress(String address) {
        controllerAddress = address;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void install(URL contribution, String name) throws AdministrationException {
        try {
            if (!isConnected()) {
                throw new IllegalStateException("Not connected");
            }
            // find HTTP port and post contents
            MBeanServerConnection conn = jmxc.getMBeanServerConnection();
            String address;
            ObjectName oName = new ObjectName(CONTRIBUTION_SERVICE_MBEAN);
            address = (String) conn.getAttribute(oName, "ContributionServiceAddress");
            DefaultHttpClient httpclient = new DefaultHttpClient();
//            String base = null;
//            int port = -1;
//            AuthScope scope = new AuthScope(base, port);
//            UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, password);
//            httpclient.getCredentialsProvider().setCredentials(scope, credentials);

            HttpPost post = new HttpPost(address + "/" + name);
            InputStreamEntity entity = new InputStreamEntity(contribution.openStream(), -1);
            entity.setContentType("binary/octet-stream");
            entity.setChunked(true);
            post.setEntity(entity);

            HttpResponse response = httpclient.execute(post);
            handleResponse(response, name);
        } catch (JMException e) {
            throw new AdministrationException(e);
        } catch (IOException e) {
            throw new AdministrationException(e);
        }
    }

    private void handleResponse(HttpResponse response, String name) throws AdministrationException {
        int code = response.getStatusLine().getStatusCode();
        if (400 == code) {
            throw new AdministrationException();
        } else if (420 == code) {
            throw new ContributionAlreadyInstalledException(name);
        } else if (422 == code) {
            List<String> errors = new ArrayList<String>();
            // TODO fill in
            throw new InvalidContributionException(errors);
            // TODO iterator
        }
//        HttpEntity resEntity = response.getEntity();
//
//        if (resEntity != null) {
//            resEntity.consumeContent();
//        }
    }

    public void deploy(String name) throws AdministrationException {

    }

    public boolean isConnected() {
        return jmxc != null;
    }

    public void connect() throws IOException {
        if (jmxc != null) {
            throw new IllegalStateException("Already connected");
        }
        JMXServiceURL url = new JMXServiceURL(controllerAddress);
        jmxc = JMXConnectorFactory.connect(url, null);
    }

    public void disconnect() throws IOException {
        if (jmxc == null) {
            throw new IllegalStateException("Not connected");
        }
        try {
            jmxc.close();
        } finally {
            jmxc = null;
        }

    }
}
