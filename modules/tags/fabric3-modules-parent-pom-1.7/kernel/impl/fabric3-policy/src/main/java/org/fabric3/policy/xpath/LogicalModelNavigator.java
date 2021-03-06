/*
* Fabric3
* Copyright (c) 2009-2011 Metaform Systems
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
package org.fabric3.policy.xpath;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.jaxen.DefaultNavigator;
import org.jaxen.FunctionCallException;
import org.jaxen.JaxenConstants;
import org.jaxen.NamedAccessNavigator;
import org.jaxen.Navigator;
import org.jaxen.UnsupportedAxisException;
import org.jaxen.XPath;
import org.jaxen.javabean.Element;
import org.jaxen.util.SingleObjectIterator;

import org.fabric3.spi.model.instance.Bindable;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalScaArtifact;
import org.fabric3.spi.model.instance.LogicalService;

/**
 * Interface for navigating the domain logical model using Jaxen.
 */
public class LogicalModelNavigator extends DefaultNavigator implements NamedAccessNavigator {
    private static final long serialVersionUID = 1755511737841941331L;

    /**
     * Singleton implementation.
     */
    private static final LogicalModelNavigator instance = new LogicalModelNavigator();

    /**
     * Retrieve the singleton instance of this DocumentNavigator.
     *
     * @return the singleton
     */
    public static Navigator getInstance() {
        return instance;
    }

    public boolean isElement(Object obj) {
        return (obj instanceof LogicalComponent);
    }

    public boolean isComment(Object obj) {
        return false;
    }

    public boolean isText(Object obj) {
        return (obj instanceof String);
    }

    public boolean isAttribute(Object obj) {
        return false;
    }

    public boolean isProcessingInstruction(Object obj) {
        return false;
    }

    public boolean isDocument(Object obj) {
        return false;
    }

    public boolean isNamespace(Object obj) {
        return false;
    }

    public String getElementName(Object obj) {
        return ((Element) obj).getName();
    }

    public String getElementNamespaceUri(Object obj) {
        return "";
    }

    public String getElementQName(Object obj) {
        return "";
    }

    public String getAttributeName(Object obj) {
        return "";
    }

    public String getAttributeNamespaceUri(Object obj) {
        return "";
    }

    public String getAttributeQName(Object obj) {
        return "";
    }

    public Iterator getChildAxisIterator(Object contextNode) {
        if (contextNode instanceof LogicalCompositeComponent) {
            final LogicalCompositeComponent composite = (LogicalCompositeComponent) contextNode;
            return new Iterator() {
                int pos = 0;

                public boolean hasNext() {
                    return pos < composite.getComponents().size();
                }

                public Object next() {
                    if (!hasNext()) {
                        throw new IndexOutOfBoundsException();
                    }
                    int i = 0;
                    for (LogicalComponent<?> component : composite.getComponents()) {
                        if (i == pos) {
                            pos++;
                            return component;
                        }
                        i++;
                    }
                    throw new AssertionError();
                }

                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }
        return JaxenConstants.EMPTY_ITERATOR;
    }

    public Iterator getChildAxisIterator(Object contextNode, String localName, String namespacePrefix, String namespaceURI) {
        if (contextNode instanceof LogicalCompositeComponent) {
            LogicalCompositeComponent composite = (LogicalCompositeComponent) contextNode;
            if ("component".equals(localName)) {
                Collection<LogicalComponent<?>> result = composite.getComponents();
                if (result == null) {
                    return JaxenConstants.EMPTY_ITERATOR;
                }
                return result.iterator();
            }
        } else if (contextNode instanceof LogicalComponent) {
            // handle keywords: binding, implementation, reference, and service
            if (localName.startsWith("binding.")) {
                LogicalComponent<?> component = (LogicalComponent<?>) contextNode;
                List<LogicalBinding> bindings = new ArrayList<LogicalBinding>();
                for (LogicalService service : component.getServices()) {
                    for (LogicalBinding<?> binding : service.getBindings()) {
                        if (localName.equals(binding.getDefinition().getType().getLocalPart())) {
                            bindings.add(binding);
                        }
                    }
                }
                for (LogicalReference reference : component.getReferences()) {
                    for (LogicalBinding<?> binding : reference.getBindings()) {
                        if (localName.equals(binding.getDefinition().getType().getLocalPart())) {
                            bindings.add(binding);
                        }
                    }
                }
                return bindings.iterator();
            } else if (localName.startsWith("implementation.")) {
                LogicalComponent<?> component = (LogicalComponent<?>) contextNode;
                if (localName.equals(component.getDefinition().getImplementation().getType().getLocalPart())) {
                    return new SingleObjectIterator(component);
                }
            } else if (localName.equals("reference")) {
                LogicalComponent<?> component = (LogicalComponent<?>) contextNode;
                return component.getReferences().iterator();
            } else if (localName.equals("service")) {
                LogicalComponent<?> component = (LogicalComponent<?>) contextNode;
                return component.getServices().iterator();
            }
        } else if (contextNode instanceof Bindable) {
            Bindable bindable = (Bindable) contextNode;
            if (localName.startsWith("binding.")) {
                List<LogicalBinding<?>> bindings = new ArrayList<LogicalBinding<?>>();
                for (LogicalBinding<?> binding : bindable.getBindings()) {
                    // TODO use strict namespaces?
                    if (localName.equals(binding.getDefinition().getType().getLocalPart())) {
                        bindings.add(binding);
                    }
                }
                return bindings.iterator();
            } else {
                // assume it is an operation name
                
            }
        } else if (contextNode instanceof LogicalBinding) {
            LogicalBinding<?> binding = (LogicalBinding<?>) contextNode;
            if (localName.equals(binding.getDefinition().getType().getLocalPart())) {
                List<LogicalBinding<?>> bindings = new ArrayList<LogicalBinding<?>>();
                bindings.add(binding);
                return bindings.iterator();
            }
        }
        return JaxenConstants.EMPTY_ITERATOR;
    }

    public Iterator getParentAxisIterator(Object contextNode) {
        if (contextNode instanceof LogicalComponent) {
            return new SingleObjectIterator(((LogicalComponent) contextNode).getParent());
        }
        return JaxenConstants.EMPTY_ITERATOR;
    }

    public Iterator getAttributeAxisIterator(Object contextNode) {
        return JaxenConstants.EMPTY_ITERATOR;
    }

    public Iterator getAttributeAxisIterator(Object contextNode, String localName, String namespacePrefix, String namespaceURI)
            throws UnsupportedAxisException {
        if (contextNode instanceof LogicalComponent) {
            LogicalComponent<?> component = (LogicalComponent) contextNode;
            Object attr;
            if ("uri".equals(localName)) {
                attr = component.getUri().getSchemeSpecificPart();
            } else if ("name".equals(localName)) {
                attr = component.getDefinition().getName();
            } else {
                throw new UnsupportedAxisException("Unknown attribute specified: " + localName);
            }

            if (attr == null) {
                return JaxenConstants.EMPTY_ITERATOR;
            }
            return new SingleObjectIterator(attr);
        } else if (contextNode instanceof LogicalService) {
            LogicalService service = (LogicalService) contextNode;
            Object attr;
            if ("uri".equals(localName)) {
                attr = service.getUri().getSchemeSpecificPart();
            } else if ("name".equals(localName)) {
                attr = service.getDefinition().getName();
            } else {
                throw new UnsupportedAxisException("Unknown attribute specified: " + localName);
            }
            return new SingleObjectIterator(attr);
        } else if (contextNode instanceof LogicalReference) {
            LogicalReference reference = (LogicalReference) contextNode;
            Object attr;
            if ("uri".equals(localName)) {
                attr = reference.getUri().getSchemeSpecificPart();
            } else if ("name".equals(localName)) {
                attr = reference.getDefinition().getName();
            } else {
                throw new UnsupportedAxisException("Unknown attribute specified: " + localName);
            }
            return new SingleObjectIterator(attr);
        } else if (contextNode instanceof Bindable) {
            Bindable bindable = (Bindable) contextNode;
            List<LogicalBinding<?>> bindings = new ArrayList<LogicalBinding<?>>();
            for (LogicalBinding<?> binding : bindable.getBindings()) {
                // TODO use strict namespaces?
                if (localName.equals(binding.getDefinition().getType().getLocalPart())) {
                    bindings.add(binding);
                }
            }
            return bindings.iterator();
        }
        return JaxenConstants.EMPTY_ITERATOR;
    }

    public Iterator getNamespaceAxisIterator(Object contextNode) {
        return JaxenConstants.EMPTY_ITERATOR;
    }

    public Object getDocumentNode(Object contextNode) {
        LogicalComponent component = (LogicalComponent) contextNode;
        while (component.getParent() != null) {
            // xcv z FIXME need to navigate services and references
            component = (LogicalComponent) component.getParent();
        }
        return component;
    }

    public Object getParentNode(Object contextNode) {
        if (contextNode instanceof LogicalScaArtifact) {
            return ((LogicalScaArtifact) contextNode).getParent();
        }

        return null;
    }

    public String getTextStringValue(Object obj) {
        return obj.toString();
    }

    public String getElementStringValue(Object obj) {
        return obj.toString();
    }

    public String getAttributeStringValue(Object obj) {
        return obj.toString();
    }

    public String getNamespaceStringValue(Object obj) {
        return obj.toString();
    }

    public String getNamespacePrefix(Object obj) {
        return null;
    }

    public String getCommentStringValue(Object obj) {
        return null;
    }

    public String translateNamespacePrefixToUri(String prefix, Object context) {
        return null;
    }

    public short getNodeType(Object node) {
        return 0;
    }

    public Object getDocument(String uri) throws FunctionCallException {
        return null;
    }

    public String getProcessingInstructionTarget(Object obj) {
        return null;
    }

    public String getProcessingInstructionData(Object obj) {
        return null;
    }

    public XPath parseXPath(String xpath) throws org.jaxen.saxpath.SAXPathException {
        return new LogicalModelXPath(xpath);
    }

}