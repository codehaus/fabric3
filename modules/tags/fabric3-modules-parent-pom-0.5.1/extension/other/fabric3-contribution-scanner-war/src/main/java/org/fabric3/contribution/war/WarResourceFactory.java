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
package org.fabric3.contribution.war;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.spi.scanner.FileResource;
import org.fabric3.spi.scanner.FileSystemResource;
import org.fabric3.spi.scanner.FileSystemResourceFactory;
import org.fabric3.spi.scanner.FileSystemResourceFactoryRegistry;

/**
 * Creates a FileResource for SCA contribution jars
 *
 * @version $Rev: 1343 $ $Date: 2007-09-15 16:30:11 -0700 (Sat, 15 Sep 2007) $
 */
@EagerInit
public class WarResourceFactory implements FileSystemResourceFactory {

    public WarResourceFactory(@Reference FileSystemResourceFactoryRegistry registry) {
        registry.register(this);
    }

    public FileSystemResource createResource(File file) {
        if (!file.getName().endsWith(".war")) {
            return null;
        }
        JarFile jarFile = null;
        try {
            jarFile = new JarFile(file.getCanonicalPath());
            JarEntry entry = jarFile.getJarEntry("WEB-INF/sca-contribution.xml");
            if (entry == null) {
                return null;
            }
        } catch (FileNotFoundException e) {
            // no sca-contribution, ignore
            return null;
        } catch (IOException e) {
            throw new AssertionError(e);
        } finally {
            try {
                if (jarFile != null) {
                    jarFile.close();
                }
            } catch (IOException e) {
                // ignore
            }
        }
        return new FileResource(file);
    }
}