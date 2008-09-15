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
package org.fabric3.fabric.policy.infoset;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.fabric3.scdl.Implementation;
import org.fabric3.spi.model.instance.Bindable;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @version $Revision$ $Date$
 */
public class DefaultPolicyInfosetBuilder implements PolicyInfosetBuilder {
    
    private static final DocumentBuilderFactory FACTORY = DocumentBuilderFactory.newInstance();

    /**
     * The infoset for the binding includes, the binding, service or reference against which
     * the binding is specified, the component on which the service or reference is declared.
     * 
     * <component name="">
     *   <service|reference name="">
     *     <binding.xxx/>
     *   </service|reference>
     * </component>
     * 
     * The returned element is the element that represents the service or reference.
     */
    public Element buildInfoSet(LogicalBinding<?> logicalBinding) {
        
        try {

            DocumentBuilder builder = FACTORY.newDocumentBuilder();
            Document document = builder.newDocument();
            
            Element componentElement = document.createElement("component");
            
            Element bindableElement = null;
            Bindable bindable = logicalBinding.getParent();
            if (bindable instanceof LogicalService) {
                LogicalService logicalService = (LogicalService) bindable;
                bindableElement = document.createElement("service");
                bindableElement.setAttribute("name", logicalService.getDefinition().getName());
            } else {
                LogicalReference logicalReference = (LogicalReference) bindable;
                bindableElement = document.createElement("reference");
                bindableElement.setAttribute("name", logicalReference.getDefinition().getName());
            }
            componentElement.appendChild(bindableElement);
            
            LogicalComponent<?> logicalComponent = bindable.getParent();
            componentElement.setAttribute("name", logicalComponent.getDefinition().getName());
            
            Element bindingElement = document.createElement(logicalBinding.getBinding().getType().getLocalPart());
            bindableElement.appendChild(bindingElement);

            return bindableElement;
            
        } catch (ParserConfigurationException ex) {
            throw new AssertionError(ex);
        }
        
    }

    /**
     * The infoset for the implementation includes the component and implementation type..
     * 
     * <component name="">
     *   <impelemnation.xxx/>
     * </component>
     * 
     * The returned element is the element that represents the component.
     */
    public Element buildInfoSet(LogicalComponent<?> logicalComponent) {
        
        try {

            DocumentBuilder builder = FACTORY.newDocumentBuilder();
            Document document = builder.newDocument();
            
            Element componentElement = document.createElement("component");
            componentElement.setAttribute("name", logicalComponent.getDefinition().getName());
            
            Implementation<?> implementation = logicalComponent.getDefinition().getImplementation();
            Element implementationElement = document.createElement(implementation.getType().getLocalPart());
            
            componentElement.appendChild(implementationElement);

            return componentElement;
            
        } catch (ParserConfigurationException ex) {
            throw new AssertionError(ex);
        }
        
    }

}
