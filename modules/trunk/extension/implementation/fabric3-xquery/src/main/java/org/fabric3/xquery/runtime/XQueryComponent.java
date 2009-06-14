  /*
   * Fabric3
   * Copyright (C) 2009 Metaform Systems
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
