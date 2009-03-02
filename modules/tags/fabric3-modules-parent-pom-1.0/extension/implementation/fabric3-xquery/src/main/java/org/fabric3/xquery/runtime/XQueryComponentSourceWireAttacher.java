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
package org.fabric3.xquery.runtime;

import java.net.URI;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.spi.ObjectCreationException;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.builder.component.SourceWireAttacher;
import org.fabric3.spi.model.physical.InteractionType;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.services.componentmanager.ComponentManager;
import org.fabric3.spi.util.UriHelper;
import org.fabric3.spi.wire.Wire;
import org.fabric3.xquery.provision.XQueryComponentWireSourceDefinition;

/**
 * @version $Rev$ $Date$
 */
@EagerInit
public class XQueryComponentSourceWireAttacher implements SourceWireAttacher<XQueryComponentWireSourceDefinition> {

    private ComponentManager manager;

    public XQueryComponentSourceWireAttacher(@Reference ComponentManager manager) {
        this.manager = manager;
    }

    public void attachToSource(XQueryComponentWireSourceDefinition source, PhysicalWireTargetDefinition target, Wire wire) throws WiringException {
        URI sourceUri = UriHelper.getDefragmentedName(source.getUri());
        String referenceName = source.getUri().getFragment();
        InteractionType interactionType = source.getInteractionType();
        String callbackUri = null;
        if (target.getCallbackUri() != null) {
            callbackUri = target.getCallbackUri().toString();
        }
        XQueryComponent component = (XQueryComponent) manager.getComponent(sourceUri);
        component.attachSourceWire(referenceName, interactionType, callbackUri, wire);
    }

    public void detachFromSource(XQueryComponentWireSourceDefinition source, PhysicalWireTargetDefinition target) throws WiringException {
        throw new AssertionError();
    }

    public void attachObjectFactory(XQueryComponentWireSourceDefinition source, ObjectFactory<?> objectFactory, PhysicalWireTargetDefinition target)
            throws WiringException {
        URI sourceUri = UriHelper.getDefragmentedName(source.getUri());
        String referenceName = source.getUri().getFragment();
        XQueryComponent component = (XQueryComponent) manager.getComponent(sourceUri);
        try {
            component.attachObjectFactory(referenceName, objectFactory);
        } catch (ObjectCreationException e) {
            throw new WiringException(e);
        }
    }

    public void detachObjectFactory(XQueryComponentWireSourceDefinition source, PhysicalWireTargetDefinition target) throws WiringException {
        throw new AssertionError();
    }

}