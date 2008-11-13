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
package org.fabric3.provisioning.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.fabric3.spi.services.contribution.Contribution;
import org.fabric3.spi.services.contribution.MetaDataStore;

/**
 * Returns the contents of a contribution associated with the encoded servlet path. The servlet path corresponds to the contribution URI.
 *
 * @version $Revision$ $Date$
 */
public class ArchiveResolverServlet extends HttpServlet {
    private static final long serialVersionUID = -5822568715938454572L;
    private MetaDataStore store;

    public ArchiveResolverServlet(MetaDataStore store) {
        this.store = store;
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String info = req.getPathInfo().substring(1);    // path info always begins with '/'
        try {
            URI uri = new URI(info);
            Contribution contribution = store.find(uri);
            if (contribution == null) {
                throw new ServletException("Contribution not found: " + info + ". Request URL was: " + info);
            }
            URL url = contribution.getLocation();
            copy(url.openStream(), resp.getOutputStream());
        } catch (URISyntaxException e) {
            throw new ServletException("Invalid URI: " + info, e);
        }
    }


    public static int copy(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[4096];
        int count = 0;
        int n;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }
}
