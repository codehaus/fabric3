/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the ÒLicenseÓ), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an Òas isÓ basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 * --- Original Apache License ---
 *
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
package org.fabric3.spi.util;

import java.net.URI;

/**
 * Utility methods for handling URIs
 *
 * @version $Rev$ $Date$
 */
public final class UriHelper {

    private UriHelper() {
    }

    /**
     * Returns the base name for a component URI, e.g. 'Bar' for 'sca://foo/Bar'
     *
     * @param uri the URI to parse
     * @return the base name
     */
    public static String getBaseName(URI uri) {
        String s = uri.toString();
        int pos = s.lastIndexOf('/');
        if (pos > -1) {
            return s.substring(pos + 1);
        } else {
            return s;
        }
    }

    public static String getParentName(URI uri) {
        String s = uri.toString();
        int pos = s.lastIndexOf('/');
        if (pos > -1) {
            return s.substring(0, pos);
        } else {
            return null;
        }
    }


    public static URI getDefragmentedName(URI uri) {
        if (uri.getFragment() == null) {
            return uri;
        }
        return URI.create(getDefragmentedNameAsString(uri));
    }

    public static String getDefragmentedNameAsString(URI uri) {
        if (uri.getFragment() == null) {
            return uri.toString();
        }
        String s = uri.toString();
        int pos = s.lastIndexOf('#');
        return s.substring(0, pos);
    }

}
