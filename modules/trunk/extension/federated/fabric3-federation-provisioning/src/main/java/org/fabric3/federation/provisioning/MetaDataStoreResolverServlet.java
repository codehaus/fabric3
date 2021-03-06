/*
* Fabric3
* Copyright (c) 2009-2012 Metaform Systems
*
* Fabric3 is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as
* published by the Free Software Foundation, either version 3 of
* the License, or (at your option) any later version, with the
* following exception:
*
* Linking this software statically or dynamically with other
* modules is making a combined work based on this software.
* Thus, the terms and conditions of the GNU General Public
* License cover the whole combination.
*
* As a special exception, the copyright holders of this software
* give you permission to link this software with independent
* modules to produce an executable, regardless of the license
* terms of these independent modules, and to copy and distribute
* the resulting executable under terms of your choice, provided
* that you also meet, for each linked independent module, the
* terms and conditions of the license of that module. An
* independent module is a module which is not derived from or
* based on this software. If you modify this software, you may
* extend this exception to your version of the software, but
* you are not obligated to do so. If you do not wish to do so,
* delete this exception statement from your version.
*
* Fabric3 is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty
* of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
* See the GNU General Public License for more details.
*
* You should have received a copy of the
* GNU General Public License along with Fabric3.
* If not, see <http://www.gnu.org/licenses/>.
*/
package org.fabric3.federation.provisioning;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.fabric3.host.util.IOHelper;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.MetaDataStore;
import org.fabric3.spi.security.AuthenticationService;
import org.fabric3.spi.security.AuthorizationService;

/**
 * Used on a controller to return the contents of a contribution associated with the encoded servlet path from the metadata store. The servlet path
 * corresponds to the contribution URI.
 *
 * @version $Rev$ $Date$
 */
public class MetaDataStoreResolverServlet extends AbstractResolverServlet {
    private static final long serialVersionUID = -5822568715938454572L;
    private MetaDataStore store;
    private boolean secure;

    protected MetaDataStoreResolverServlet(MetaDataStore store,
                                           AuthenticationService authenticationService,
                                           AuthorizationService authorizationService,
                                           String role,
                                           ProvisionMonitor monitor) {
        super(authenticationService, authorizationService, role, monitor);
        this.store = store;
        this.secure = true;
    }

    public MetaDataStoreResolverServlet(MetaDataStore store, ProvisionMonitor monitor) {
        super(null, null, null, monitor);
        this.store = store;
        this.secure = false;
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getPathInfo();
        if (path == null) {
            monitor.errorMessage("Path info was null");
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        int pos = path.lastIndexOf("/");
        if (pos < 0) {
            monitor.errorMessage("Invalid path info");
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        if (secure && !checkAccess(req)) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String info = path.substring(pos + 1);

        try {
            URI uri = new URI(info);
            Contribution contribution = store.find(uri);
            if (contribution == null) {
                monitor.errorMessage("Contribution not found: " + info + ". Request URL was: " + info);
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            URL url = contribution.getLocation();
            if (url == null) {
                throw new IOException("Contribution is not a physical artifact: " + uri);
            }
            IOHelper.copy(url.openStream(), resp.getOutputStream());
        } catch (URISyntaxException e) {
            monitor.error("Invalid URI", e);
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

}
