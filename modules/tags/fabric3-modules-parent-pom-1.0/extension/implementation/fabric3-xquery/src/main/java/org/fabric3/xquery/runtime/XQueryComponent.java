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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.xml.namespace.QName;

import org.fabric3.model.type.component.PropertyValue;
import org.fabric3.spi.AbstractLifecycle;
import org.fabric3.spi.ObjectCreationException;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.component.AtomicComponent;
import org.fabric3.spi.component.InstanceWrapper;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.model.physical.InteractionType;
import org.fabric3.spi.wire.Wire;

/**
 * @version $Rev$ $Date$
 */
public abstract class XQueryComponent<T> extends AbstractLifecycle implements AtomicComponent<T> {

    protected final URI uri;
    protected URI classLoaderId;
    protected final QName deployable;
    protected final Map<String, ObjectFactory<?>> referenceFactories;

    public XQueryComponent(URI uri, QName deployable) {
        this.uri = uri;
        this.deployable = deployable;
        referenceFactories = new ConcurrentHashMap<String, ObjectFactory<?>>();
    }

    public URI getUri() {
        return uri;
    }

    public URI getClassLoaderId() {
        return classLoaderId;
    }

    public void setClassLoaderId(URI classLoaderId) {
        this.classLoaderId = classLoaderId;
    }

    public Map<String, PropertyValue> getDefaultPropertyValues() {
        return null;
    }

    public void setDefaultPropertyValues(Map<String, PropertyValue> defaultPropertyValues) {

    }

    public abstract void attachSourceWire(String name, InteractionType interactionType, String callbackUri, Wire wire) throws WiringException;

    public abstract void attachTargetWire(String name, InteractionType interactionType, Wire wire) throws WiringException;

    public void attachObjectFactory(String name, ObjectFactory<?> factory) throws ObjectCreationException {
        referenceFactories.put(name, factory);
    }

    public <B> ObjectFactory<B> createWireFactory(String refrenceName) throws ObjectCreationException {
        return null;
    }

    public QName getDeployable() {
        return deployable;
    }

    public boolean isEagerInit() {
        return false;
    }

    public int getInitLevel() {
        return 0;
    }

    public long getMaxIdleTime() {
        return 0;
    }

    public long getMaxAge() {
        return 0;
    }

    public InstanceWrapper<T> createInstanceWrapper(WorkContext workContext) throws ObjectCreationException {
        throw new UnsupportedOperationException();
    }

    public ObjectFactory<T> createObjectFactory() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return "[" + uri.toString() + "] in state [" + super.toString() + ']';
    }
}
