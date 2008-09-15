/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
        int index = info.indexOf("/");
        if (index < 1) {
            throw new ServletException("Invalid URI: " + info);
        }
        // parse the encoded scheme, which is the first segment in the path
        String scheme = info.substring(0, index);
        if (EncodingConstants.DEFAULT_SCHEME.equals(scheme)) {
            // the contribution URI does not have an explicit encoded scheme
            scheme = null;
        }
        String base = info.substring(index + 1);
        try {
            URI uri = new URI(scheme, null, base, null);
            Contribution contribution = store.find(uri);
            if (contribution == null) {
                throw new ServletException("Contribution not found for: " + info);
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
