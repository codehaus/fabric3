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
package org.fabric3.binding.ws.axis2.policy;

import java.util.Iterator;

import javax.imageio.spi.ServiceRegistry;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.description.AxisDescription;
import org.apache.axis2.util.XMLUtils;
import org.apache.neethi.PolicyEngine;
import org.apache.neethi.builders.AssertionBuilder;
import org.apache.neethi.builders.xml.XMLPrimitiveAssertionBuilder;
import org.fabric3.spi.services.factories.xml.XMLFactory;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;
import org.w3c.dom.Element;

/**
 * Applies policies based on WS-Policy.
 * 
 * @version $Revision$ $Date$
 */
@EagerInit
public class NeethiPolicyApplier implements PolicyApplier {
    
    /* This xmlInputFactory is injected to ensure correct implementation is used by axis2*/
    private final XMLInputFactory xmlInputFactory;

    static {
        buildAssertionBuilders();
    }
    
    public NeethiPolicyApplier(@Reference XMLFactory xmlFactory){
        xmlInputFactory = xmlFactory.newInputFactoryInstance();
    }

    public void applyPolicy(AxisDescription axisDescription, Element policy) {

        try {
            OMElement policyElement = XMLUtils.toOM(policy);

            axisDescription.applyPolicy(PolicyEngine.getPolicy(policyElement));

        } catch (Exception e) {
            // TODO Handle exception properly
            throw new AssertionError(e);
        }

    }

    /*
     * Load assertion builders associated with WS-SP.This is normally done when AssertionBuilderFactory is loaded 
     * but since the Thread Context class loader at that time is the Boot Class loader, so it is not able to find 
     * extensions jars and hence this is done here!!
     * 
     * @see org.apache.neethi.AssertionBuilderFactory
     */
    private static void buildAssertionBuilders() {
        
        // Get the current context class loader
        ClassLoader originalCL = Thread.currentThread().getContextClassLoader();

        try {
            
            Thread.currentThread().setContextClassLoader(NeethiPolicyApplier.class.getClassLoader());
            QName XML_ASSERTION_BUILDER = new QName("http://test.org/test", "test");
            
            Iterator<AssertionBuilder> asseryionBuilders = ServiceRegistry.lookupProviders(AssertionBuilder.class);
            while (asseryionBuilders.hasNext()) {
                AssertionBuilder builder = asseryionBuilders.next();
                for (QName knownElement : builder.getKnownElements()) {
                    PolicyEngine.registerBuilder(knownElement, builder);
                }
            }
            
            PolicyEngine.registerBuilder(XML_ASSERTION_BUILDER, new XMLPrimitiveAssertionBuilder());
            
        } finally {
            // Change class loader back to what it was !!
            Thread.currentThread().setContextClassLoader(originalCL);
        }
    }
    
}
