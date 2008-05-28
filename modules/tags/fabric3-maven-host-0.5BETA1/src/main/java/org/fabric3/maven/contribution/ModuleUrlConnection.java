/*
 * See the NOTICE file distributed with this work for information
 * regarding copyright ownership.  This file is licensed
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
package org.fabric3.maven.contribution;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Connection to a Maven module URL.
 *
 * @version $Rev$ $Date$
 */
public class ModuleUrlConnection extends URLConnection {
    public static final String CONTENT_TYPE = "application/vnd.fabric3.maven-project";

    protected ModuleUrlConnection(URL url) {
        super(url);
    }

    public InputStream getInputStream() throws IOException {
        throw new UnsupportedOperationException();
    }

    public String getContentType() {
        return CONTENT_TYPE;
    }

    public void connect() throws IOException {

    }
}