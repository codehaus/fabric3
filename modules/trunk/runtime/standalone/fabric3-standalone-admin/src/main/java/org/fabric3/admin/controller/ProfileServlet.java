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
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.fabric3.host.contribution.ContributionService;
import org.fabric3.host.contribution.ContributionSource;
import org.fabric3.host.contribution.StoreException;

/**
 * Responsible for soring contributions in a profile with the ContributionService.
 *
 * @version $Revision$ $Date$
 */
public class ProfileServlet extends HttpServlet {
    private static final long serialVersionUID = -8286023912719635905L;

    private ContributionService contributionService;

    public ProfileServlet(ContributionService contributionService) {
        this.contributionService = contributionService;
    }


    /**
     * Stores a the contents of a profile via an HTTP POST operation.
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
            if (!substr.endsWith(".jar") && !substr.endsWith(".zip")) {
                resp.setStatus(422);
                resp.getWriter().write("<?xml version=\"1.0\" encoding=\"ASCII\"?><description>Profile must be a zip or jar</description>");
                return;
            }
            URI uri = new URI(substr);  // remove the leading "/"
            store(uri, req.getInputStream());
            resp.setStatus(201);
        } catch (URISyntaxException e) {
            resp.setStatus(400);
            resp.getWriter().write("<?xml version=\"1.0\" encoding=\"ASCII\"?><description>Invalid URI: " + substr + "</description>");
            throw new ServletException("Invalid contribution name", e);
        } catch (IOException e) {
            resp.setStatus(422);
            resp.getWriter().write("<?xml version=\"1.0\" encoding=\"ASCII\"?><description>Error storing profile</description>");
        } catch (StoreException e) {
            resp.setStatus(422);
            resp.getWriter().write("<?xml version=\"1.0\" encoding=\"ASCII\"?><description>Error storing profile</description>");
        }
    }

    /**
     * Reads the profile stram and stores the contained contributions.
     *
     * @param profileUri the profile URI
     * @param stream     the profile input stream
     * @return the list of URIs of contributions stored from the profile
     * @throws IOException        if an error occurs reading the input stream
     * @throws URISyntaxException if a contained contribution name is invalid
     * @throws StoreException     if an error occurs during the store operation
     */
    private synchronized List<URI> store(URI profileUri, InputStream stream) throws IOException, URISyntaxException, StoreException {
        JarInputStream jarStream = null;
        try {
            jarStream = new JarInputStream(stream);
            JarEntry entry;
            List<URI> contributionUris = new ArrayList<URI>();
            while ((entry = jarStream.getNextJarEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }
                String[] tokens = entry.getName().split("/");
                if (tokens.length == 2 && "extensions".equals(tokens[0])) {
                    URI contributionUri = new URI(tokens[1]);
                    if (!contributionService.exists(contributionUri)) {
                        // the contribution does not exist, otherwise skip it
                        ContributionSource contributionSource = new StreamContributionSource(contributionUri, jarStream);
                        contributionService.store(contributionSource);
                    }
                    contributionUris.add(contributionUri);
                }
            }
            // add the profile
            contributionService.registerProfile(profileUri, contributionUris);
            return contributionUris;
        } finally {
            try {
                if (jarStream != null) {
                    jarStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}