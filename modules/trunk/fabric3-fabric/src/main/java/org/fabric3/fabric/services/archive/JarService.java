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
package org.fabric3.fabric.services.archive;

import java.io.File;
import java.io.IOException;

/**
 * Service for Jar file operations
 *
 * @version $Rev$ $Date$
 */
public interface JarService {

    /**
     * Expands a jar to a directory
     *
     * @param jar          the jar to expand
     * @param directory    the directory to expand the jar in
     * @param deleteOnExit true if the jar should be deleted when the runtime shutsdown
     * @throws IOException if an error occurs expanding the jar
     */
    void expand(File jar, File directory, boolean deleteOnExit) throws IOException;

}
