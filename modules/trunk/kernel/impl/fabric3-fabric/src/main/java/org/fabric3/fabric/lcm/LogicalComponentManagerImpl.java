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
package org.fabric3.fabric.lcm;

import java.net.URI;
import java.util.Collection;
import javax.xml.namespace.QName;

import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.model.type.component.Composite;
import org.fabric3.model.type.component.CompositeReference;
import org.fabric3.model.type.component.CompositeService;
import org.fabric3.spi.lcm.LogicalComponentManager;
import org.fabric3.spi.lcm.LogicalComponentManagerMBean;
import org.fabric3.spi.lcm.LogicalComponentStore;
import org.fabric3.spi.lcm.ReadException;
import org.fabric3.spi.lcm.WriteException;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.util.UriHelper;

/**
 * @version $Rev$ $Date$
 */
public class LogicalComponentManagerImpl implements LogicalComponentManager, LogicalComponentManagerMBean {

    private LogicalCompositeComponent domain;
    private final LogicalComponentStore logicalComponentStore;

    public LogicalComponentManagerImpl(@Reference LogicalComponentStore logicalComponentStore) {
        this.logicalComponentStore = logicalComponentStore;
    }

    public LogicalComponent<?> getComponent(URI uri) {

        String defragmentedUri = UriHelper.getDefragmentedNameAsString(uri);
        String domainString = domain.getUri().toString();
        String[] hierarchy = defragmentedUri.substring(domainString.length() + 1).split("/");
        String currentUri = domainString;
        LogicalComponent<?> currentComponent = domain;
        for (String name : hierarchy) {
            currentUri = currentUri + "/" + name;
            if (currentComponent instanceof LogicalCompositeComponent) {
                LogicalCompositeComponent composite = (LogicalCompositeComponent) currentComponent;
                currentComponent = composite.getComponent(URI.create(currentUri));
            }
            if (currentComponent == null) {
                return null;
            }
        }
        return currentComponent;

    }

    public Collection<LogicalComponent<?>> getComponents() {
        return domain.getComponents();
    }

    public LogicalCompositeComponent getRootComponent() {
        return domain;
    }

    public void replaceRootComponent(LogicalCompositeComponent component) throws WriteException {
        domain = component;
        logicalComponentStore.store(domain);
    }

    @Init
    public void initialize() throws ReadException {
        domain = logicalComponentStore.read();
    }

    public String getDomainURI() {
        return domain.getUri().toString();
    }

    public Composite getDomainComposite() {
        Composite composite = new Composite(new QName(getDomainURI(), "domain"));
        for (LogicalComponent<?> component : domain.getComponents()) {
            composite.add(component.getDefinition());
        }
        for (LogicalService service : domain.getServices()) {
            composite.add((CompositeService) service.getDefinition());
        }
        for (LogicalReference reference : domain.getReferences()) {
            composite.add((CompositeReference) reference.getDefinition());
        }
        return composite;
    }
}
