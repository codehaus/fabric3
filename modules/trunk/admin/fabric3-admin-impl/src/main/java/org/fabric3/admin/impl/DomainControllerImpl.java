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
import java.net.URI;
import java.net.URL;
import java.util.Set;
import javax.management.JMException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import org.fabric3.admin.api.CommunicationException;
import org.fabric3.admin.api.ContributionException;
import org.fabric3.admin.api.DeploymentException;
import org.fabric3.admin.api.DomainController;
import org.fabric3.admin.api.DuplicateContributionException;
import org.fabric3.admin.api.InvalidContributionException;
import org.fabric3.admin.api.InvalidDeploymentException;

/**
 * Default implementation of the DomainController API.
 *
 * @version $Revision$ $Date$
 */
public class DomainControllerImpl implements DomainController {
    private static final String CONTRIBUTION_SERVICE_MBEAN =
            "f3-management:SubDomain=null,type=service,component=\"fabric3://runtime/ContibutionServiceMBean\",service=ContributionServiceMBean";
    private static final String DOMAIN_MBEAN =
            "f3-management:SubDomain=null,type=service,component=\"fabric3://runtime/DomainMBean\",service=DomainMBean";

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

    public void install(URL contribution, URI uri) throws CommunicationException, ContributionException {
        try {
            if (!isConnected()) {
                throw new IllegalStateException("Not connected");
            }
            // find HTTP port and post contents
            MBeanServerConnection conn = jmxc.getMBeanServerConnection();

            // store the contribution using an HTTP post to the ContributionService
            String address;
            ObjectName oName = new ObjectName(CONTRIBUTION_SERVICE_MBEAN);
            address = (String) conn.getAttribute(oName, "ContributionServiceAddress");

            DefaultHttpClient httpclient = new DefaultHttpClient();
//            String base = null;
//            int port = -1;
//            AuthScope scope = new AuthScope(base, port);
//            UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, password);
//            httpclient.getCredentialsProvider().setCredentials(scope, credentials);

            HttpPost post = new HttpPost(address + "/" + uri);
            InputStreamEntity entity = new InputStreamEntity(contribution.openStream(), -1);
            entity.setContentType("binary/octet-stream");
            entity.setChunked(true);
            post.setEntity(entity);

            HttpResponse response = httpclient.execute(post);
            int code = response.getStatusLine().getStatusCode();
            if (400 == code) {
                throw new ContributionException("Error storing contribution");
            } else if (420 == code) {
                throw new DuplicateContributionException("A contribution already exists for " + uri);
            }

            // install the contribution
            conn.invoke(oName, "install", new URI[]{uri}, new String[]{URI.class.getName()});
        } catch (MBeanException e) {
            if (e.getTargetException() instanceof org.fabric3.management.contribution.InvalidContributionException) {
                org.fabric3.management.contribution.InvalidContributionException ex =
                        (org.fabric3.management.contribution.InvalidContributionException) e.getTargetException();
                throw new InvalidContributionException("Error installing " + uri, ex.getErrors());
            } else {
                throw new ContributionException(e.getMessage(), e.getTargetException());
            }
        } catch (JMException e) {
            throw new CommunicationException(e);
        } catch (IOException e) {
            throw new CommunicationException(e);
        }
    }

    @SuppressWarnings({"unchecked"})
    public Set<URI> list() throws CommunicationException {
        try {
            if (!isConnected()) {
                throw new IllegalStateException("Not connected");
            }
            MBeanServerConnection conn = jmxc.getMBeanServerConnection();
            ObjectName oName = new ObjectName(CONTRIBUTION_SERVICE_MBEAN);
            return (Set<URI>) conn.getAttribute(oName, "Contributions");
        } catch (JMException e) {
            throw new CommunicationException(e);
        } catch (IOException e) {
            throw new CommunicationException(e);
        }
    }

    public void deploy(URI uri) throws CommunicationException, DeploymentException {
        try {
            if (!isConnected()) {
                throw new IllegalStateException("Not connected");
            }
            MBeanServerConnection conn = jmxc.getMBeanServerConnection();
            ObjectName oName = new ObjectName(DOMAIN_MBEAN);
            conn.invoke(oName, "deploy", new URI[]{uri}, new String[]{URI.class.getName()});
        } catch (MBeanException e) {
            if (e.getTargetException() instanceof org.fabric3.management.contribution.InvalidContributionException) {
                org.fabric3.management.contribution.InvalidContributionException ex =
                        (org.fabric3.management.contribution.InvalidContributionException) e.getTargetException();
                throw new InvalidDeploymentException("Error deploying " + uri, ex.getErrors());
            } else {
                throw new DeploymentException(e.getTargetException().getMessage(), e.getTargetException());
            }
        } catch (JMException e) {
            throw new CommunicationException(e);
        } catch (IOException e) {
            throw new CommunicationException(e);
        }

    }

    public void deploy(URI uri, String plan) throws CommunicationException, DeploymentException {
        try {
            if (!isConnected()) {
                throw new IllegalStateException("Not connected");
            }
            MBeanServerConnection conn = jmxc.getMBeanServerConnection();
            ObjectName oName = new ObjectName(DOMAIN_MBEAN);
            conn.invoke(oName, "deploy", new Object[]{uri, plan}, new String[]{URI.class.getName(), "java.lang.String"});
        } catch (MBeanException e) {
            if (e.getTargetException() instanceof org.fabric3.management.contribution.InvalidContributionException) {
                org.fabric3.management.contribution.InvalidContributionException ex =
                        (org.fabric3.management.contribution.InvalidContributionException) e.getTargetException();
                throw new InvalidDeploymentException("Error deploying " + uri, ex.getErrors());
            } else {
                throw new DeploymentException(e.getTargetException().getMessage(), e.getTargetException());
            }
        } catch (JMException e) {
            throw new CommunicationException(e);
        } catch (IOException e) {
            throw new CommunicationException(e);
        }
    }

    public void undeploy(URI uri) throws CommunicationException, DeploymentException {
        try {
            if (!isConnected()) {
                throw new IllegalStateException("Not connected");
            }
            MBeanServerConnection conn = jmxc.getMBeanServerConnection();
            ObjectName oName = new ObjectName(DOMAIN_MBEAN);
            conn.invoke(oName, "undeploy", new Object[]{uri}, new String[]{URI.class.getName()});
        } catch (MBeanException e) {
            throw new DeploymentException(e.getTargetException().getMessage(), e.getTargetException());
        } catch (JMException e) {
            throw new CommunicationException(e);
        } catch (IOException e) {
            throw new CommunicationException(e);
        }
    }

    public void uninstall(URI name) throws CommunicationException, ContributionException {
        try {
            if (!isConnected()) {
                throw new IllegalStateException("Not connected");
            }
            MBeanServerConnection conn = jmxc.getMBeanServerConnection();
            ObjectName oName = new ObjectName(CONTRIBUTION_SERVICE_MBEAN);
            conn.invoke(oName, "uninstall", new Object[]{name}, new String[]{URI.class.getName()});
        } catch (JMException e) {
            throw new CommunicationException(e);
        } catch (IOException e) {
            throw new CommunicationException(e);
        }
    }

    public void remove(URI name) throws CommunicationException, ContributionException {
        try {
            if (!isConnected()) {
                throw new IllegalStateException("Not connected");
            }
            MBeanServerConnection conn = jmxc.getMBeanServerConnection();
            ObjectName oName = new ObjectName(CONTRIBUTION_SERVICE_MBEAN);
            conn.invoke(oName, "remove", new Object[]{name}, new String[]{URI.class.getName()});
        } catch (JMException e) {
            throw new CommunicationException(e);
        } catch (IOException e) {
            throw new CommunicationException(e);
        }
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
