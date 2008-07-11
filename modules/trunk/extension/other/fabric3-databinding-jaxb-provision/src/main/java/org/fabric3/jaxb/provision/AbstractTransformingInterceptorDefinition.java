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
import javax.xml.namespace.QName;

import org.fabric3.spi.model.physical.PhysicalInterceptorDefinition;

/**
 * Base definition for an interceptor that performs a data transformation to or from JAXB objects.
 *
 * @version $Revision$ $Date$
 */
public abstract class AbstractTransformingInterceptorDefinition extends PhysicalInterceptorDefinition {
    private URI classLoaderId;
    private QName dataType;
    private Set<String> classNames;

    /**
     * Cosntructor.
     *
     * @param classLoaderId classloader id for loading parameter and fault types.
     * @param dataType      the data type the transformer must convert to and from
     * @param classNames    set of parameter and fault types the transformer must be able to convert
     */
    public AbstractTransformingInterceptorDefinition(URI classLoaderId, QName dataType, Set<String> classNames) {
        this.classLoaderId = classLoaderId;
        this.dataType = dataType;
        this.classNames = classNames;
    }

    /**
     * The classloader id for loading parameter and fault types.
     *
     * @return the classlaoder id
     */
    public URI getClassLoaderId() {
        return classLoaderId;
    }

    /**
     * The set of parameter and fault types the transformer must be able to convert.
     *
     * @return the parameter and fault types
     */
    public Set<String> getClassNames() {
        return classNames;
    }

    /**
     * Returns the data type the transformer must convert to and from.
     *
     * @return the data type
     */
    public QName getDataType() {
        return dataType;
    }
}
