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
package org.fabric3.fabric.services.archive;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import org.fabric3.fabric.util.IOHelper;

/**
 * Default JarService implementation
 *
 * @version $Rev$ $Date$
 */
public class JarServiceImpl implements JarService {

    public void expand(URL jar, File expandDirectory, boolean deleteOnExit) throws IOException {
        JarInputStream stream = new JarInputStream(jar.openStream());
        JarEntry entry;
        while ((entry = stream.getNextJarEntry()) != null) {
            String path = entry.getName();
            File file = new File(expandDirectory, path);
            if (entry.isDirectory()) {
                // entry is a directory, create it
                if (!file.exists()) {
                    file.mkdirs();
                }
            } else {
                File dir = new File(file.getParent());
                if (!dir.exists()) {
                    // create parent directory if it does not exist
                    dir.mkdirs();
                }
                FileOutputStream fout = new FileOutputStream(file);
                IOHelper.copy(stream, fout);
                fout.close();
                if (entry.getTime() >= 0) {
                    // set timestamp
                    file.setLastModified(entry.getTime());
                }
            }
        }
        if (expandDirectory.exists() && deleteOnExit) {
            delete(expandDirectory);
        }
    }

    private void delete(File file) {
        // delete first before recursing as deleteOnExit deletes in reverse order
        file.deleteOnExit();
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            for (File child : children) {
                delete(child);
            }
        }
    }


}
