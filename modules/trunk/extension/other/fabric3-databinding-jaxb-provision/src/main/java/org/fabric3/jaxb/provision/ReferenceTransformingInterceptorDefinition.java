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
package org.fabric3.jaxb.provision;

import java.net.URI;
import java.util.Set;

/**
 * Definition for an interceptor that transforms a JAXB object to an XML string representation.
 *
 * @version $Revision$ $Date$
 */
public class ReferenceTransformingInterceptorDefinition extends AbstractTransformingInterceptorDefinition {

    public ReferenceTransformingInterceptorDefinition(URI classLoaderId, Set<String> classNames) {
        super(classLoaderId, classNames);
    }
}