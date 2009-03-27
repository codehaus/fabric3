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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.fabric3.host.contribution.ContributionException;
import org.fabric3.host.contribution.ContributionService;
import org.fabric3.host.contribution.ContributionSource;
import org.fabric3.host.contribution.DuplicateContributionException;

/**
 * @version $Revision$ $Date$
 */
public class ContributionServlet extends HttpServlet {
    private static final long serialVersionUID = -8286023912719635905L;

    private ContributionService contributionService;

    public ContributionServlet(ContributionService contributionService) {
        this.contributionService = contributionService;
    }


    /**
     * Stores a contribution via an HTTP POST operation.
     *
     * @param req  the servlet request
     * @param resp the servlet response
     * @throws ServletException if an unrecoverable error occurs processing the contribution.
     * @throws IOException      if an unrecoverable error occurs storing the contribution.
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getPathInfo();
        if (path.length() < 2) {
            resp.setStatus(400);
            resp.getWriter().write("<?xml version=\"1.0\" encoding=\"ASCII\"?><description>Invalid path: " + path + " </description>");
            return;
        }
        String substr = path.substring(1);
        try {
            URI uri = new URI(substr);  // remove the leading "/"
            ContributionSource source = new RemoteContributionSource(uri, req.getInputStream());
            contributionService.store(source);
            resp.setStatus(201);
        } catch (URISyntaxException e) {
            resp.setStatus(400);
            resp.getWriter().write("<?xml version=\"1.0\" encoding=\"ASCII\"?><description>Invalid URI: " + substr + "</description>");
            throw new ServletException("Invalid contribution name", e);
        } catch (DuplicateContributionException e) {
            resp.setStatus(420);
        } catch (ContributionException e) {
            resp.setStatus(422);
            resp.getWriter().write("<?xml version=\"1.0\" encoding=\"ASCII\"?><description>Error storing contribution</description>");
        }
    }
}