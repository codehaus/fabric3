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
package org.fabric3.fabric.collector;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;

import org.fabric3.fabric.instantiator.LogicalChange;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalState;

/**
 * Default Collector implementation.
 *
 * @version $Revision$ $Date$
 */
public class CollectorImpl implements Collector {

    public LogicalChange mark(QName deployable, LogicalCompositeComponent composite) {
        LogicalChange change = new LogicalChange(composite);
        mark(deployable, composite, change);
        return change;
    }

    public void collect(LogicalCompositeComponent composite) {
        List<URI> remove = new ArrayList<URI>();
        for (LogicalComponent<?> component : composite.getComponents()) {
            if (component instanceof LogicalCompositeComponent) {
                collect((LogicalCompositeComponent) component);
            }
            remove.add(component.getUri());
        }
        for (URI uri : remove) {
            composite.removeComponent(uri);
        }
    }

    /**
     * Iterates through the components and their children belonging to the deployable and marks them for removal.
     *
     * @param deployable the deployable being undeployed
     * @param composite  the root composite, typically the domain component
     * @param change     the change set to update
     */
    private void mark(QName deployable, LogicalCompositeComponent composite, LogicalChange change) {
        for (LogicalComponent<?> component : composite.getComponents()) {
            if (deployable.equals(component.getDeployable())) {
                if (component instanceof LogicalCompositeComponent) {
                    mark(deployable, (LogicalCompositeComponent) component, change);
                }
                component.setState(LogicalState.MARKED);
                change.removeComponent(component);
            }
        }

    }

}
