/*
 * Fabric3
 * Copyright � 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the �License�), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an �as is� basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 */
package org.fabric3.binding.ejb.runtime;

import java.net.URI;
import java.util.Map;
import javax.xml.namespace.QName;

import org.osoa.sca.ComponentContext;

import org.fabric3.scdl.PropertyValue;
import org.fabric3.spi.AbstractLifecycle;
import org.fabric3.spi.ObjectCreationException;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.component.AtomicComponent;
import org.fabric3.spi.invocation.WorkContext;

/**
 * @version $Revision Date: Oct 29, 2007 Time: 4:26:44 PM
 */
public class EjbStatefulComponent extends AbstractLifecycle implements AtomicComponent<EjbStatefulInstanceWrapper> {

    private final QName deployable;
    private URI classLoaderId;

    public EjbStatefulComponent(QName deployable) {
        this.deployable = deployable;
    }

    public QName getDeployable() {
        return deployable;
    }

    public URI getClassLoaderId() {
        return classLoaderId;
    }

    public void setClassLoaderId(URI classLoaderId) {
        this.classLoaderId = classLoaderId;
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

    public EjbStatefulInstanceWrapper createInstanceWrapper(WorkContext workContext) throws ObjectCreationException {
        return new EjbStatefulInstanceWrapper();
    }

    public ObjectFactory<EjbStatefulInstanceWrapper> createObjectFactory() {
        return new ObjectFactory<EjbStatefulInstanceWrapper>() {
            public EjbStatefulInstanceWrapper getInstance() {
                return new EjbStatefulInstanceWrapper();
            }
        };
    }

    public URI getUri() {
        return null;
    }

    public ComponentContext getComponentContext() {
        return null;
    }

    public Map<String, PropertyValue> getDefaultPropertyValues() {
        return null;
    }

    public void setDefaultPropertyValues(Map<String, PropertyValue> defaultPropertyValues) {

    }


}
