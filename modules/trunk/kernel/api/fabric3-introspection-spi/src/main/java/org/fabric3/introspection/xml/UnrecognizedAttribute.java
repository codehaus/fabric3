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
package org.fabric3.introspection.xml;

import javax.xml.stream.XMLStreamReader;

/**
 * Failure that indicates an attribute was encountered that could not be handled.
 *
 * @version $Rev$ $Date$
 */
public class UnrecognizedAttribute extends XmlValidationFailure<String> {

    /**
     * Constructor.
     *
     * @param name   the attrbiute name
     * @param reader the StAX reader positioned on the unrecognized element
     */
    public UnrecognizedAttribute(String name, XMLStreamReader reader) {
        super("Unrecognized element", name, reader);
    }

    public String getMessage() {
        StringBuffer b = new StringBuffer("The attribute ").append(getValidatable()).append(" specified in ").append(getResourceURI()).append(
                " at ").append(getLine()).append(",").append(getColumn()).append(" was not recognized.");
        return b.toString();
    }
}