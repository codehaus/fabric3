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
import java.util.TreeSet;
import javax.xml.namespace.QName;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;

import org.fabric3.api.annotation.Monitor;
import org.fabric3.host.contribution.ArtifactValidationFailure;
import org.fabric3.host.contribution.ContributionException;
import org.fabric3.host.contribution.ContributionInUseException;
import org.fabric3.host.contribution.ContributionLockedException;
import org.fabric3.host.contribution.ContributionService;
import org.fabric3.host.contribution.Deployable;
import org.fabric3.host.contribution.ValidationException;
import org.fabric3.host.contribution.ValidationFailure;
import org.fabric3.jetty.JettyService;
import org.fabric3.management.contribution.ArtifactErrorInfo;
import org.fabric3.management.contribution.ContributionInUseManagementException;
import org.fabric3.management.contribution.ContributionInfo;
import org.fabric3.management.contribution.ContributionInstallException;
import org.fabric3.management.contribution.ContributionLockedManagementException;
import org.fabric3.management.contribution.ContributionRemoveException;
import org.fabric3.management.contribution.ContributionServiceMBean;
import org.fabric3.management.contribution.ContributionUninstallException;
import org.fabric3.management.contribution.ErrorInfo;
import org.fabric3.management.contribution.InvalidContributionException;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.MetaDataStore;

/**
 * @version $Revision$ $Date$
 */
@EagerInit
public class ContibutionServiceMBeanImpl implements ContributionServiceMBean {
    private static final String REPOSITORY = "/admin/repository";

    private JettyService jettyService;
    private ContributionService contributionService;
    private MetaDataStore metaDataStore;
    private ContributionServiceMBeanMonitor monitor;
    private String hostName;
    private String address;

    public ContibutionServiceMBeanImpl(@Reference JettyService jettyService,
                                       @Reference ContributionService contributionService,
                                       @Reference MetaDataStore metaDataStore,
                                       @Monitor ContributionServiceMBeanMonitor monitor) {
        this.jettyService = jettyService;
        this.contributionService = contributionService;
        this.metaDataStore = metaDataStore;
        this.monitor = monitor;
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
        jettyService.registerMapping(REPOSITORY + "/*", servlet);
    }

    public String getContributionServiceAddress() {
        return address;
    }

    public Set<ContributionInfo> getContributions() {
        Set<Contribution> contributions = metaDataStore.getContributions();
        Set<ContributionInfo> infos = new TreeSet<ContributionInfo>();
        for (Contribution contribution : contributions) {
            URI uri = contribution.getUri();
            String state = contribution.getState().toString();
            long timestamp = contribution.getTimestamp();
            List<QName> deployables = new ArrayList<QName>();
            for (Deployable deployable : contribution.getManifest().getDeployables()) {
                deployables.add(deployable.getName());
            }
            ContributionInfo info = new ContributionInfo(uri, state, deployables, timestamp);
            infos.add(info);
        }
        return infos;
    }

    public void install(URI uri) throws ContributionInstallException {
        try {
            contributionService.install(uri);
        } catch (ValidationException e) {
            // propagate validaton error messages to the client
            List<ErrorInfo> errors = new ArrayList<ErrorInfo>();
            for (ValidationFailure failure : e.getErrors()) {
                if (failure instanceof ArtifactValidationFailure) {
                    ArtifactValidationFailure avf = (ArtifactValidationFailure) failure;
                    ArtifactErrorInfo error = new ArtifactErrorInfo(avf.getArtifactName());
                    for (ValidationFailure entry : avf.getFailures()) {
                        ErrorInfo info = new ErrorInfo(entry.getMessage());
                        error.addError(info);
                    }
                    errors.add(error);
                } else {
                    ErrorInfo info = new ErrorInfo(failure.getMessage());
                    errors.add(info);
                }
            }
            throw new InvalidContributionException("Error installing " + uri, errors);
        } catch (ContributionException e) {
            monitor.error("Error installing: " + uri, e);
            // don't rethrow the original exception since the class will not be available on the client's classpath
            throw new ContributionInstallException(getErrorMessage(e));
        }
    }

    public void uninstall(URI uri) throws ContributionUninstallException {
        try {
            contributionService.uninstall(uri);
        } catch (ContributionInUseException e) {
            throw new ContributionInUseManagementException(e.getMessage(), e.getUri(), e.getContributions());
        } catch (ContributionLockedException e) {
            throw new ContributionLockedManagementException(e.getMessage(), e.getUri(), e.getDeployables());
        } catch (ContributionException e) {
            // log the exception as it is not recoverable
            monitor.error("Error uninstalling contribution: " + uri, e);
            // don't rethrow the original exception since the class will not be available on the client's classpath
            throw new ContributionUninstallException(getErrorMessage(e));
        }
    }

    public void remove(URI uri) throws ContributionRemoveException {
        try {
            contributionService.remove(uri);
        } catch (ContributionException e) {
            monitor.error("Error removing contribution: " + uri, e);
            // don't rethrow the original exception since the class will not be available on the client's classpath
            throw new ContributionRemoveException(getErrorMessage(e));
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
        address = "http://" + baseAddress + ":" + jettyService.getHttpPort() + REPOSITORY;
    }

    private String getErrorMessage(ContributionException e) {
        Throwable cause = e;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
        return cause.getMessage();
    }
}
