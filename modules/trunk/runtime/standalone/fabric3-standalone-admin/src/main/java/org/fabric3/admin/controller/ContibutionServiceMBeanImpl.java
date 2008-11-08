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
package org.fabric3.admin.controller;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;

import org.fabric3.api.annotation.Monitor;
import org.fabric3.host.contribution.ContributionException;
import org.fabric3.host.contribution.ContributionService;
import org.fabric3.host.contribution.ValidationException;
import org.fabric3.host.contribution.ValidationFailure;
import org.fabric3.jetty.JettyService;
import org.fabric3.management.contribution.ContributionManagementException;
import org.fabric3.management.contribution.ContributionServiceMBean;
import org.fabric3.management.contribution.InvalidContributionException;

/**
 * @version $Revision$ $Date$
 */
@EagerInit
public class ContibutionServiceMBeanImpl implements ContributionServiceMBean {
    private JettyService jettyService;
    private ContributionService contributionService;
    private ContributionServiceMBeanMonitor monitor;
    private int httpPort = 8080;
    private String hostName;
    private String address;

    public ContibutionServiceMBeanImpl(@Reference JettyService jettyService,
                                       @Reference ContributionService contributionService,
                                       @Monitor ContributionServiceMBeanMonitor monitor) {
        this.jettyService = jettyService;
        this.contributionService = contributionService;
        this.monitor = monitor;
    }

    /**
     * Optionally sets the port for uploading contributions.
     *
     * @param httpPort the port
     */
    @Property
    public void setHttpPort(String httpPort) {
        this.httpPort = Integer.parseInt(httpPort);
    }

    /**
     * Optionally sets the host name to use for determing the IP address for clients to use when uploading contributions to a multi-homed machine.
     *
     * @param hostName the host name
     */
    @Property
    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    @Init
    public void init() throws Exception {
//        ServletHandler handler = new ServletHandler();
//        ServletHolder holder = new ServletHolder(new ContributionServlet());
//        handler.addServlet(holder);
//        Constraint constraint = new Constraint();
//        constraint.setName(Constraint.__BASIC_AUTH);
//
//        constraint.setRoles(new String[]{"admin"});
//        constraint.setAuthenticate(true);
//
//        ConstraintMapping cm = new ConstraintMapping();
//        cm.setConstraint(constraint);
//        cm.setPathSpec("/*");
//
//        SecurityHandler sh = new SecurityHandler();
//        sh.setUserRealm(new HashUserRealm("MyRealm"));
//        sh.setConstraintMappings(new ConstraintMapping[]{cm});
//
//
//        HandlerCollection handlers = new HandlerCollection();
//        handlers.setHandlers(new Handler[]{sh, handler});
//
//        jettyService.registerHandler(handlers);
//        handlers.start();

        initAddress();

        // map a servlet for uploading contributions
        ContributionServlet servlet = new ContributionServlet(contributionService);
        jettyService.registerMapping("/admin/repository/*", servlet);
    }

    public String getContributionServiceAddress() {
        return address;
    }

    public Set<URI> getContributions() {
        return contributionService.getContributions();
    }

    public void install(URI uri) throws ContributionManagementException {
        try {
            contributionService.install(uri);
        } catch (ValidationException e) {
            // propagate validaton error messages to the client
            List<String> errors = new ArrayList<String>();
            for (ValidationFailure failure : e.getErrors()) {
                errors.add(failure.getMessage());
            }
            throw new InvalidContributionException("Error installing " + uri, errors);
        } catch (ContributionException e) {
            monitor.error("Error installing: " + uri, e);
            // don't rethrow the original exception since the class will not be available on the client's classpath
            reportError(e);
        }
    }

    public void uninstall(URI uri) throws ContributionManagementException {
        try {
            contributionService.uninstall(uri);
        } catch (ContributionException e) {
            monitor.error("Error uninstalling contribution: " + uri, e);
            // don't rethrow the original exception since the class will not be available on the client's classpath
            reportError(e);
        }
    }

    public void remove(URI uri) throws ContributionManagementException {
        try {
            contributionService.remove(uri);
        } catch (ContributionException e) {
            monitor.error("Error removing contribution: " + uri, e);
            // don't rethrow the original exception since the class will not be available on the client's classpath
            reportError(e);
        }
    }

    /**
     * Calculates the address clients use to upload contributions.
     *
     * @throws UnknownHostException if the specified host is not found
     */
    private void initAddress() throws UnknownHostException {
        String baseAddress;
        if (hostName == null) {
            baseAddress = InetAddress.getLocalHost().getHostAddress();
        } else {
            baseAddress = InetAddress.getByName(hostName).getHostAddress();
        }
        address = "http://" + baseAddress + ":" + httpPort + "/admin/repository";
    }

    private void reportError(ContributionException e) throws ContributionManagementException {
        Throwable cause = e;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
        throw new ContributionManagementException(cause.getMessage());
    }
}
