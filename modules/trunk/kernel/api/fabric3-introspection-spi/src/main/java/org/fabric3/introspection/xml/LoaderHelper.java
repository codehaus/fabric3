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

import java.net.URI;
import java.util.Set;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;

import org.fabric3.scdl.PolicyAware;

/**
 * Interface for the helper class for loading intents and policy sets into elements aginst which intents and policies can be declared.
 *
 * @version $Revision$ $Date$
 */
public interface LoaderHelper {

    /**
     * Loads policy sets and intents defined against bindings, implementations, services, references and components.
     *
     * @param policyAware Element against which policy sets and intents are declared.
     * @param reader      XML stream reader from where the attributes are read.
     * @throws LoaderException if there was a problem with the policy set or intents
     */
    void loadPolicySetsAndIntents(PolicyAware policyAware, XMLStreamReader reader) throws LoaderException;

    /**
     * Convert a component URI in the form ${componentName}/${serviceName} to a URI of the form ${componentName}#${serviceName}
     *
     * @param target the target URI to convert
     * @return a URI where the fragment represents the service name
     */
    URI getURI(String target);

    /**
     * Parses a list of qualified names.
     *
     * @param reader    XML stream reader.
     * @param attribute Attribute that contains the list of qualified names.
     * @return Set containing the qualified names.
     * @throws LoaderException If the qualified name cannot be resolved.
     */
    Set<QName> parseListOfQNames(XMLStreamReader reader, String attribute) throws LoaderException;

    /**
     * Constructs a QName from the given name. If a namespace prefix is not specified in the name, the namespace context is used
     *
     * @param name   the name to parse
     * @param reader the XML stream reader
     * @return the parsed QName
     * @throws LoaderException if a specified namespace prefix is invalid
     */
    QName createQName(String name, XMLStreamReader reader) throws LoaderException;
}
