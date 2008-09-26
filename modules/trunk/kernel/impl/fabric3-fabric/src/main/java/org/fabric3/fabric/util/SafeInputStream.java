/*
 * Fabric3
 * Copyright � 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the �License�), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an �as is� basis,
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
package org.fabric3.fabric.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.jar.JarFile;

/**
 * @version $Rev$ $Date$
 */

/**
 * This class is a workaround for URL stream issue as illustrated below. InputStream is=url.getInputStream();
 * is.close(); // This line doesn't close the JAR file if the URL is a jar entry like "jar:file:/a.jar!/my.composite" We
 * also need to turn off the JarFile cache.
 *
 * @version $Rev$ $Date$
 * @link http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4950148
 */
public class SafeInputStream extends InputStream {
    private JarFile jarFile;
    private InputStream is;

    public SafeInputStream(URL url) throws IOException {
        String protocol = url.getProtocol();
        if (protocol != null && (protocol.equals("jar"))) {
            JarURLConnection connection = (JarURLConnection) url.openConnection();
            // We cannot use cache
            connection.setUseCaches(false);
            try {
                is = connection.getInputStream();
            } catch (IOException e) {
                throw e;
            }
            jarFile = connection.getJarFile();
        } else {
            is = url.openStream();
        }
    }

    public SafeInputStream(JarURLConnection connection) throws IOException {
        // We cannot use cache
        connection.setUseCaches(false);
        is = connection.getInputStream();
        jarFile = connection.getJarFile();
    }

    public int available() throws IOException {
        return is.available();
    }

    public void close() throws IOException {
        is.close();
        // We need to close the JAR file
        if (jarFile != null) {
            jarFile.close();
        }
    }

    public synchronized void mark(int readlimit) {
        is.mark(readlimit);
    }

    public boolean markSupported() {
        return is.markSupported();
    }

    public int read() throws IOException {
        return is.read();
    }

    public int read(byte[] b, int off, int len) throws IOException {
        return is.read(b, off, len);
    }

    public int read(byte[] b) throws IOException {
        return is.read(b);
    }

    public synchronized void reset() throws IOException {
        is.reset();
    }

    public long skip(long n) throws IOException {
        return is.skip(n);
    }
}
