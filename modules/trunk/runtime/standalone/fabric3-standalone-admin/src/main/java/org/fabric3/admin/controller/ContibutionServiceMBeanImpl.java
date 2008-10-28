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

import java.net.URI;
import java.util.Set;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.host.contribution.ContributionService;
import org.fabric3.jetty.JettyService;
import org.fabric3.management.contribution.ContributionServiceMBean;

/**
 * @version $Revision$ $Date$
 */
@EagerInit
public class ContibutionServiceMBeanImpl implements ContributionServiceMBean {
    private JettyService jettyService;
    private ContributionService contributionService;

    public ContibutionServiceMBeanImpl(@Reference JettyService jettyService, @Reference ContributionService contributionService) {
        this.jettyService = jettyService;
        this.contributionService = contributionService;
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
        ContributionServlet servlet = new ContributionServlet(contributionService);
        jettyService.registerMapping("/admin/repository/*", servlet);
    }

    public String getContributionServiceAddress() {
        // TODO make configurable
        return "http://localhost:8180/admin/repository";
    }

    public Set<URI> getContributions() {
        return contributionService.getContributions();
    }


}
