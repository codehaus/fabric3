/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the ñLicenseî), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an ñas isî basis,
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
package org.fabric3.spi;

/**
 * 
 * Namespace URIs used in Fabric3.
 *
 */
public class Namespaces {
	   
    /**
     * Namespace URI used for core Fabric3.
     * Intended usage for map keys in SCDLs etc. Recommended prefix f3-core.
     */
    public static final String CORE = "urn:fabric3.org:core";
   
    /**
     * Namespace URI used for fabric3 binding extensions.
     * Intended usage for non-standard bindings like hessian, burlap, ftp etc, Recommended prefix f3-binding.
     */
    public static final String BINDING = "urn:fabric3.org:binding";
   
    /**
     * Namespace URI used for fabric3 implementation extensions.
     * Intended usage for non-standard implementations like system, groovy, junit etc, Recommended prefix f3-implementation.
     */
    public static final String IMPLEMENTATION = "urn:fabric3.org:implementation";
   
    /**
     * Namespace URI used for fabric3 policy extensions.
     * Intended usage for non-standard SCA intents and policies like dataBinding.jaxb, authorization.message, Recommended prefix f3-policy.
     */
    public static final String POLICY = "urn:fabric3.org:policy";
   
    /**
     * Namespace URI used for other extensions like implementation.cache and implementation.jpa. Recommended prefix f3-other.
     */
    public static final String OTHER = "urn:fabric3.org:other";
   
    /**
     * Private constructor.
     */
    private Namespaces() {
    }

}
