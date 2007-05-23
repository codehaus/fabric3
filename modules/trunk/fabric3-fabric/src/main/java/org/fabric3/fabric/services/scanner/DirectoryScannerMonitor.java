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
package org.fabric3.fabric.services.scanner;

import org.fabric3.api.annotation.LogLevel;

/**
 * Monitoring interface for the DirectoryScanner
 *
 * @version $Rev$ $Date$
 */
public interface DirectoryScannerMonitor {

    /**
     * Called when a destination is notified of a new resource
     *
     * @param name the name of the resource
     */
    @LogLevel("DEBUG")
    void add(String name);

    /**
     * Called when a destination is notified of a resource removal
     *
     * @param name the name of the resource
     */
    @LogLevel("DEBUG")
    void remove(String name);

    /**
     * Called when a destination is notified of a resource update
     *
     * @param name the name of the resource
     */
    @LogLevel("DEBUG")
    void update(String name);

    /**
     * Called when a resource has been processed
     *
     * @param name the name of the resource
     */
    @LogLevel("DEBUG")
    void processed(String name);

    /**
     * Called when an error is encountered processing an entry
     *
     * @param e the error
     */
    @LogLevel("SEVERE")
    void error(Throwable e);

    /**
     * Called when an error is encountered processing an entry
     *
     * @param message the error-specific message
     * @param e       the error
     */
    @LogLevel("SEVERE")
    void error(String message, Throwable e);

    /**
     * Called when an error is encountered processing an entry
     *
     * @param message    the error-specific message
     * @param identifier an identifier
     * @param e          the error
     */
    @LogLevel("SEVERE")
    void error(String message, String identifier, Throwable e);

}
