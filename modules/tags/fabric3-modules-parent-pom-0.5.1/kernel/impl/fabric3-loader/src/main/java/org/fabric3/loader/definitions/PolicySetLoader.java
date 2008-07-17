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
package org.fabric3.loader.definitions;

import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.xml.InvalidPrefixException;
import org.fabric3.introspection.xml.LoaderHelper;
import org.fabric3.introspection.xml.TypeLoader;
import org.fabric3.loader.impl.InvalidQNamePrefix;
import org.fabric3.scdl.definitions.PolicyPhase;
import org.fabric3.scdl.definitions.PolicySet;
import org.fabric3.spi.Constants;
import org.fabric3.transform.TransformationException;
import org.fabric3.transform.xml.Stream2Document;

/**
 * Loader for definitions.
 *
 * @version $Revision$ $Date$
 */
@EagerInit
public class PolicySetLoader implements TypeLoader<PolicySet> {

    private final LoaderHelper helper;
    private Stream2Document transformer = new Stream2Document();

    public PolicySetLoader(@Reference LoaderHelper helper) {
        this.helper = helper;
    }

    public PolicySet load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {

        Element policyElement;
        try {
            policyElement = transformer.transform(reader, null).getDocumentElement();
        } catch (TransformationException e) {
            DefinitionProcessingFailure failure = new DefinitionProcessingFailure("Error processing policy set", e, reader);
            context.addError(failure);
            return null;
        }

        String name = policyElement.getAttribute("name");
        QName qName = new QName(context.getTargetNamespace(), name);

        Set<QName> provides = new HashSet<QName>();
        StringTokenizer tok = new StringTokenizer(policyElement.getAttribute("provides"));
        while (tok.hasMoreElements()) {
            try {
                provides.add(helper.createQName(tok.nextToken(), reader));
            } catch (InvalidPrefixException e) {
                context.addError(new InvalidQNamePrefix(e.getPrefix(), reader));
                return null;
            }
        }

        String appliesTo = policyElement.getAttribute("appliesTo");

        String sPhase = policyElement.getAttributeNS(Constants.FABRIC3_NS, "phase");
        PolicyPhase phase = null;
        if (sPhase != null && !"".equals(sPhase.trim())) {
            phase = PolicyPhase.valueOf(sPhase);
        } else {
            phase = PolicyPhase.PROVIDED;
        }

        Element extension = null;
        NodeList children = policyElement.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i) instanceof Element) {
                extension = (Element) children.item(i);
                break;
            }
        }

        return new PolicySet(qName, provides, appliesTo, extension, phase);

    }

}
