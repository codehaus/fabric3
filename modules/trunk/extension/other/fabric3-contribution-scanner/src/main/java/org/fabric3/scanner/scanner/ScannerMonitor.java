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
package org.fabric3.scanner.scanner;

import org.fabric3.api.annotation.LogLevel;

/**
 * Monitoring interface for the DirectoryScanner
 *
 * @version $Rev$ $Date$
 */
public interface ScannerMonitor {

    /**                
     * Called when a destination is notified of a new resource
     *
     * @param name the name of the resource
     */
    @LogLevel("INFO")
    void add(String name);

    /**
     * Called when a destination is notified of a resource removal
     *
     * @param name the name of the resource
     */
    @LogLevel("FINE")
    void remove(String name);

    /**
     * Called when a destination is notified of a resource update
     *
     * @param name the name of the resource
     */
    @LogLevel("FINE")
    void update(String name);

    /**
     * Called when a general error is encountered processing an entry
     *
     * @param e the error
     */
    @LogLevel("SEVERE")
    void error(Throwable e);

    /**
     * Called when an error is encountered during recovery
     *
     * @param e the error
     */
    @LogLevel("SEVERE")
    void recoveryError(Throwable e);

    /**
     * Called when an error is encountered removing an entry
     *
     * @param filename the file being removed
     * @param e        the error
     */
    @LogLevel("SEVERE")
    void removalError(String filename, Throwable e);

    /**
     * Called when errors are encountered processing contributions
     *
     * @param description a description of the errors
     */
    @LogLevel("SEVERE")
    void contributionErrors(String description);
}
