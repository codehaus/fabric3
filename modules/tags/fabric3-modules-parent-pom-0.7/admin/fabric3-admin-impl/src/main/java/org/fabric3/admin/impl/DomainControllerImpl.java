/*
 * Fabric3
 * Copyright � 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the �License�), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an �as is� basis,
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
import org.fabric3.admin.api.DomainController;
import org.fabric3.management.contribution.ContributionInUseManagementException;
import org.fabric3.management.contribution.ContributionInfo;
import org.fabric3.management.contribution.ContributionInstallException;
import org.fabric3.management.contribution.ContributionLockedManagementException;
import org.fabric3.management.contribution.ContributionManagementException;
import org.fabric3.management.contribution.ContributionUninstallException;
import org.fabric3.management.contribution.DuplicateContributionManagementException;
import org.fabric3.management.contribution.InvalidContributionException;
import org.fabric3.management.domain.DeploymentManagementException;
import org.fabric3.management.domain.InvalidDeploymentException;

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
    private String domainAddress = "service:jmx:rmi:///jndi/rmi://localhost:1099/server";
    private JMXConnector jmxc;

    public void setDomainAddress(String address) {
        domainAddress = address;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void store(URL contribution, URI uri) throws CommunicationException, ContributionManagementException {
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
                throw new ContributionManagementException("Error storing contribution");
            } else if (420 == code) {
                throw new DuplicateContributionManagementException("A contribution already exists for " + uri);
            }

        } catch (MBeanException e) {
            throw new ContributionManagementException(e.getMessage(), e.getTargetException());
        } catch (JMException e) {
            throw new CommunicationException(e);
        } catch (IOException e) {
            throw new CommunicationException(e);
        }
    }

    public void install(URI uri) throws CommunicationException, ContributionInstallException {
        try {
            if (!isConnected()) {
                throw new IllegalStateException("Not connected");
            }
            // find HTTP port and post contents
            MBeanServerConnection conn = jmxc.getMBeanServerConnection();

            ObjectName oName = new ObjectName(CONTRIBUTION_SERVICE_MBEAN);
            // install the contribution
            conn.invoke(oName, "install", new URI[]{uri}, new String[]{URI.class.getName()});
        } catch (MBeanException e) {
            if (e.getTargetException() instanceof InvalidContributionException) {
                throw (InvalidContributionException) e.getTargetException();
            } else {
                throw new ContributionInstallException(e.getMessage(), e.getTargetException());
            }
        } catch (JMException e) {
            throw new CommunicationException(e);
        } catch (IOException e) {
            throw new CommunicationException(e);
        }
    }

    @SuppressWarnings({"unchecked"})
    public Set<ContributionInfo> stat() throws CommunicationException {
        try {
            if (!isConnected()) {
                throw new IllegalStateException("Not connected");
            }
            MBeanServerConnection conn = jmxc.getMBeanServerConnection();
            ObjectName oName = new ObjectName(CONTRIBUTION_SERVICE_MBEAN);
            return (Set<ContributionInfo>) conn.getAttribute(oName, "Contributions");
        } catch (JMException e) {
            throw new CommunicationException(e);
        } catch (IOException e) {
            throw new CommunicationException(e);
        }
    }

    public void deploy(URI uri) throws CommunicationException, DeploymentManagementException {
        try {
            if (!isConnected()) {
                throw new IllegalStateException("Not connected");
            }
            MBeanServerConnection conn = jmxc.getMBeanServerConnection();
            ObjectName oName = new ObjectName(DOMAIN_MBEAN);
            conn.invoke(oName, "deploy", new URI[]{uri}, new String[]{URI.class.getName()});
        } catch (MBeanException e) {
            if (e.getTargetException() instanceof InvalidDeploymentException) {
                throw (InvalidDeploymentException) e.getTargetException();
            } else {
                throw new DeploymentManagementException(e.getTargetException().getMessage(), e.getTargetException());
            }
        } catch (JMException e) {
            throw new CommunicationException(e);
        } catch (IOException e) {
            throw new CommunicationException(e);
        }

    }

    public void deploy(URI uri, String plan) throws CommunicationException, DeploymentManagementException {
        try {
            if (!isConnected()) {
                throw new IllegalStateException("Not connected");
            }
            MBeanServerConnection conn = jmxc.getMBeanServerConnection();
            ObjectName oName = new ObjectName(DOMAIN_MBEAN);
            conn.invoke(oName, "deploy", new Object[]{uri, plan}, new String[]{URI.class.getName(), "java.lang.String"});
        } catch (MBeanException e) {
            if (e.getTargetException() instanceof InvalidDeploymentException) {
                throw (InvalidDeploymentException) e.getTargetException();
            } else {
                throw new DeploymentManagementException(e.getTargetException().getMessage(), e.getTargetException());
            }
        } catch (JMException e) {
            throw new CommunicationException(e);
        } catch (IOException e) {
            throw new CommunicationException(e);
        }
    }

    public void undeploy(URI uri) throws CommunicationException, DeploymentManagementException {
        try {
            if (!isConnected()) {
                throw new IllegalStateException("Not connected");
            }
            MBeanServerConnection conn = jmxc.getMBeanServerConnection();
            ObjectName oName = new ObjectName(DOMAIN_MBEAN);
            conn.invoke(oName, "undeploy", new Object[]{uri}, new String[]{URI.class.getName()});
        } catch (MBeanException e) {
            throw new DeploymentManagementException(e.getTargetException().getMessage(), e.getTargetException());
        } catch (JMException e) {
            throw new CommunicationException(e);
        } catch (IOException e) {
            throw new CommunicationException(e);
        }
    }

    public void uninstall(URI name) throws CommunicationException, ContributionUninstallException {
        try {
            if (!isConnected()) {
                throw new IllegalStateException("Not connected");
            }
            MBeanServerConnection conn = jmxc.getMBeanServerConnection();
            ObjectName oName = new ObjectName(CONTRIBUTION_SERVICE_MBEAN);
            conn.invoke(oName, "uninstall", new Object[]{name}, new String[]{URI.class.getName()});
        } catch (MBeanException e) {
            if (e.getTargetException() instanceof ContributionInUseManagementException) {
                throw (ContributionInUseManagementException) e.getTargetException();
            } else if (e.getTargetException() instanceof ContributionLockedManagementException) {
                throw (ContributionLockedManagementException) e.getTargetException();
            } else {
                throw new ContributionUninstallException(e.getTargetException().getMessage(), e.getTargetException());
            }
        } catch (JMException e) {
            throw new CommunicationException(e);
        } catch (IOException e) {
            throw new CommunicationException(e);
        }
    }

    public void remove(URI name) throws CommunicationException {
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
        JMXServiceURL url = new JMXServiceURL(domainAddress);
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
