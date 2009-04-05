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
package org.fabric3.spi.model.instance;

import java.net.URI;
import java.util.Map;

import javax.xml.namespace.QName;

import org.w3c.dom.Document;

/**
 * Utilities for copying a logical model graph.
 *
 * @version $Revision$ $Date$
 */
public class CopyUtil {
    private CopyUtil() {
    }

    /**
     * Copies the instance graph, making a complete replica, including preservation of parent-child relationships.
     *
     * @param composite the composite to copy
     * @return the copy
     */
    public static LogicalCompositeComponent copy(LogicalCompositeComponent composite) {
        return copy(composite, composite.getParent());
    }


    /**
     * Recursively performs the actual copy.
     *
     * @param composite the composite to copy
     * @param parent    the parent of the copy
     * @return the copy
     */
    private static LogicalCompositeComponent copy(LogicalCompositeComponent composite, LogicalCompositeComponent parent) {
        LogicalCompositeComponent copy =
                new LogicalCompositeComponent(composite.getUri(), composite.getDefinition(), parent);
        copy.setAutowireOverride(composite.getAutowireOverride());
        copy.setState(composite.getState());
        copy.setZone(composite.getZone());
        copy.addIntents(composite.getIntents());
        copy.addPolicySets(composite.getPolicySets());
        for (Map.Entry<String, Document> entry : composite.getPropertyValues().entrySet()) {
            copy.setPropertyValue(entry.getKey(), entry.getValue());
        }
        for (LogicalComponent<?> component : composite.getComponents()) {
            copy(component, copy);
        }
        for (LogicalReference reference : composite.getReferences()) {
            copy(reference, copy);
        }
        for (LogicalResource<?> resource : composite.getResources()) {
            copy(resource, copy);
        }
        for (LogicalService service : composite.getServices()) {
            copy(service, copy);
        }
        for (LogicalReference reference : copy.getReferences()) {
            copyWires(composite, reference, copy);
        }
        return copy;
    }

    @SuppressWarnings({"unchecked"})
    private static void copy(LogicalComponent<?> component, LogicalCompositeComponent parent) {
        LogicalComponent<?> copy;
        if (component instanceof LogicalCompositeComponent) {
            copy = copy((LogicalCompositeComponent) component, parent);
        } else {
            copy = new LogicalComponent(component.getUri(), component.getDefinition(), parent);
            copy.setZone(component.getZone());
            copy.addIntents(component.getIntents());
            copy.addPolicySets(component.getPolicySets());
        }
        parent.addComponent(copy);
    }

    private static void copy(LogicalReference reference, LogicalCompositeComponent parent) {
        LogicalReference copy = new LogicalReference(reference.getUri(), reference.getDefinition(), parent);
        for (URI uri : reference.getPromotedUris()) {
            copy.addPromotedUri(uri);
        }
        copy.addIntents(reference.getIntents());
        copy.addPolicySets(reference.getPolicySets());
        copy(reference, copy);
        parent.addReference(copy);
    }

    @SuppressWarnings({"unchecked"})
    private static void copy(LogicalResource<?> resource, LogicalCompositeComponent parent) {
        LogicalResource copy = new LogicalResource(resource.getUri(), resource.getResourceDefinition(), parent);
        copy.setTarget(resource.getTarget());
        parent.addResource(copy);
    }


    private static void copy(LogicalService service, LogicalCompositeComponent parent) {
        LogicalService copy = new LogicalService(service.getUri(), service.getDefinition(), parent);
        copy.setPromotedUri(service.getPromotedUri());
        copy(service, copy);
        copy.addIntents(service.getIntents());
        copy.addPolicySets(service.getPolicySets());
        parent.addService(copy);
    }

    @SuppressWarnings({"unchecked"})
    private static void copy(Bindable from, Bindable to) {
        for (LogicalBinding<?> binding : from.getBindings()) {
            LogicalBinding<?> copy = new LogicalBinding(binding.getDefinition(), to);
            copy.setState(binding.getState());
            to.addBinding(copy);
            copy.addIntents(binding.getIntents());
            copy.addPolicySets(binding.getPolicySets());
        }
        for (LogicalBinding<?> binding : from.getCallbackBindings()) {
            LogicalBinding<?> copy = new LogicalBinding(binding.getDefinition(), to);
            copy.setState(binding.getState());
            to.addCallbackBinding(copy);
            copy.setAssigned(binding.isAssigned());
            copy.addIntents(binding.getIntents());
            copy.addPolicySets(binding.getPolicySets());
        }
    }

    private static void copyWires(LogicalCompositeComponent composite, LogicalReference reference, LogicalCompositeComponent parent) {
        for (LogicalWire wire : composite.getWires(reference)) {
            URI targetUri = wire.getTargetUri();
            QName deployable = wire.getTargetDeployable();
            LogicalWire wireCopy = new LogicalWire(parent, reference, targetUri, deployable);
            parent.addWire(reference, wireCopy);
        }
    }

}
