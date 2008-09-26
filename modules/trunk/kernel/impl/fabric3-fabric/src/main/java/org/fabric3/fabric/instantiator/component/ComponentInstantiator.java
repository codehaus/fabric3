/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the “License”), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an “as is” basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 */
package org.fabric3.fabric.instantiator.component;

import java.util.Map;

import org.w3c.dom.Document;

import org.fabric3.fabric.instantiator.LogicalChange;
import org.fabric3.scdl.ComponentDefinition;
import org.fabric3.scdl.Implementation;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;

/**
 * @version $Revision$ $Date$
 */
public interface ComponentInstantiator {

    /**
     * Instantiates a logical component from a component definition
     *
     * @param parent     the parent logical component
     * @param properties the collection of properties associated with the component
     * @param definition the component definition to instantiate from @return the instantiated logical component
     * @param change     the logical change the instantiation should mutate
     * @return an instantiated logical component
     */
    <I extends Implementation<?>> LogicalComponent<I> instantiate(LogicalCompositeComponent parent,
                                                                  Map<String, Document> properties,
                                                                  ComponentDefinition<I> definition,
                                                                  LogicalChange change);

}