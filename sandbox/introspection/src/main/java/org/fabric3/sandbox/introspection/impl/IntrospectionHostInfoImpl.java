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
package org.fabric3.sandbox.introspection.impl;

import java.net.URI;
import java.net.URL;

import org.fabric3.sandbox.introspection.IntrospectionHostInfo;

/**
 * @version $Rev$ $Date$
 */
public class IntrospectionHostInfoImpl implements IntrospectionHostInfo {
    private static final URI DOMAIN = URI.create("fabric3://./introspection");
    public URI getDomain() {
        return DOMAIN;
    }

    @Deprecated
    public URI getRuntimeId() {
        return null;
    }

    public URL getBaseURL() {
        return null;
    }

    public boolean isOnline() {
        return false;
    }

    public String getProperty(String name, String defaultValue) {
        return null;
    }
}
