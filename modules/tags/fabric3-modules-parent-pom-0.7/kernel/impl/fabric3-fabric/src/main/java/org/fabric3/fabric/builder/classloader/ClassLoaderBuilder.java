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
package org.fabric3.fabric.builder.classloader;

import java.net.URI;

import org.fabric3.spi.model.physical.PhysicalClassLoaderDefinition;

/**
 * Instantiates a classloader on a runtime node.
 *
 * @version $Revision$ $Date$
 */
public interface ClassLoaderBuilder {

    /**
     * Creates or updates a classloader based on the classloader definition.
     *
     * @param definition the classloader definition
     * @throws ClassLoaderBuilderException if the classloader cannot be created
     */
    void build(PhysicalClassLoaderDefinition definition) throws ClassLoaderBuilderException;


    /**
     * Removes a classloader if it is not referenced by any other registered classloader.
     *
     * @param uri the classloader uri
     */
    void destroy(URI uri);
}
