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
package org.fabric3.binding.ws.axis2.physical;

import java.net.URI;

import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;

/**
 * @version $Revision$ $Date$
 */
public class Axis2WireTargetDefinition extends PhysicalWireTargetDefinition {

    private String referenceInterface;
    private URI classloaderURI;

    /**
     * @return Reference interface for the wire target.
     */
    public String getReferenceInterface() {
        return referenceInterface;
    }

    /**
     * @param referenceInterface Reference interface for the wire target.
     */
    public void setReferenceInterface(String referenceInterface) {
        this.referenceInterface = referenceInterface;
    }

    /**
     * @return Classloader URI.
     */
    public URI getClassloaderURI() {
        return classloaderURI;
    }

    /**
     * @param classloaderURI Classloader URI.
     */
    public void setClassloaderURI(URI classloaderURI) {
        this.classloaderURI = classloaderURI;
    }

}
