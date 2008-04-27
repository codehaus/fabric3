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

package org.fabric3.fabric.util;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public final class IOHelper {
    private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;

    private IOHelper() {

    }

    /**
     * Copy bytes from an <code>InputStream</code> to an <code>OutputStream</code>.
     * <p/>
     * This method buffers the input internally, so there is no need to use a <code>BufferedInputStream</code>.
     *
     * @param input  the <code>InputStream</code> to read from
     * @param output the <code>OutputStream</code> to write to
     * @return the number of bytes copied
     * @throws IOException if an I/O error occurs
     */
    public static int copy(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        int count = 0;
        int n;
        while (-1 != (n = input.read(buffer))) { // NOPMD
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }
    
    public static final void closeQueitly(Closeable closeable) {
        try {
            closeable.close();
        } catch (IOException ignore) {
        }
    }

}