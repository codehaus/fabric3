/*
* Fabric3
* Copyright (c) 2009 Metaform Systems
*
* Fabric3 is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as
* published by the Free Software Foundation, either version 3 of
* the License, or (at your option) any later version, with the
* following exception:
*
* Linking this software statically or dynamically with other
* modules is making a combined work based on this software.
* Thus, the terms and conditions of the GNU General Public
* License cover the whole combination.
*
* As a special exception, the copyright holders of this software
* give you permission to link this software with independent
* modules to produce an executable, regardless of the license
* terms of these independent modules, and to copy and distribute
* the resulting executable under terms of your choice, provided
* that you also meet, for each linked independent module, the
* terms and conditions of the license of that module. An
* independent module is a module which is not derived from or
* based on this software. If you modify this software, you may
* extend this exception to your version of the software, but
* you are not obligated to do so. If you do not wish to do so,
* delete this exception statement from your version.
*
* Fabric3 is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty
* of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
* See the GNU General Public License for more details.
*
* You should have received a copy of the
* GNU General Public License along with Fabric3.
* If not, see <http://www.gnu.org/licenses/>.
*/
package org.fabric3.binding.ws.axis2.runtime.policy;

import java.util.Iterator;
import javax.imageio.spi.ServiceRegistry;
import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.dom.factory.OMDOMFactory;
import org.apache.axis2.description.AxisDescription;
import org.apache.neethi.PolicyEngine;
import org.apache.neethi.builders.AssertionBuilder;
import org.apache.neethi.builders.xml.XMLPrimitiveAssertionBuilder;
import org.osoa.sca.annotations.EagerInit;
import org.w3c.dom.Element;

/**
 * Applies policies based on WS-Policy.
 *
 * @version $Revision$ $Date$
 */
@EagerInit
public class NeethiPolicyApplier implements PolicyApplier {

    static {
        buildAssertionBuilders();
    }

    public NeethiPolicyApplier() {
    }

    public void applyPolicy(AxisDescription axisDescription, Element policy) {

        try {
            OMElement policyElement =
                    (OMElement) new OMDOMFactory().getDocument().importNode(policy, true);

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
