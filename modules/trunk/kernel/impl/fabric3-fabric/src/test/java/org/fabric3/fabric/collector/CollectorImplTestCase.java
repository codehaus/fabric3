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
package org.fabric3.fabric.collector;

import java.net.URI;
import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.fabric3.fabric.instantiator.LogicalChange;
import org.fabric3.model.type.component.Implementation;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalState;

/**
 * @version $Revision$ $Date$
 */
public class CollectorImplTestCase extends TestCase {
    private static final QName DEPLOYABLE = new QName(null, "deployable");

    private Collector collector = new CollectorImpl();

    public <I extends Implementation<?>> void testMarkAndCollect() {

        LogicalCompositeComponent domain = new LogicalCompositeComponent(URI.create("domain"), null, null);

        URI child1Uri = URI.create("child1");
        LogicalComponent<I> child1 = new LogicalComponent<I>(child1Uri, null, domain);
        child1.setState(LogicalState.PROVISIONED);
        child1.setDeployable(DEPLOYABLE);
        URI child2Uri = URI.create("child2");
        LogicalComponent<I> child2 = new LogicalComponent<I>(child2Uri, null, domain);
        child2.setState(LogicalState.PROVISIONED);

        URI childCompositeUri = URI.create("childComposite");
        LogicalCompositeComponent childComposite = new LogicalCompositeComponent(childCompositeUri, null, domain);
        childComposite.setState(LogicalState.PROVISIONED);
        childComposite.setDeployable(DEPLOYABLE);
        URI child3Uri = URI.create("child3");
        LogicalComponent<I> child3 = new LogicalComponent<I>(child3Uri, null, childComposite);
        child3.setState(LogicalState.PROVISIONED);
        child3.setDeployable(DEPLOYABLE);
        childComposite.addComponent(child3);

        domain.addComponent(child1);
        domain.addComponent(child2);
        domain.addComponent(childComposite);

        LogicalChange change = collector.mark(DEPLOYABLE, domain);

        assertEquals(LogicalState.MARKED, childComposite.getState());
        assertEquals(LogicalState.MARKED, child1.getState());
        assertEquals(LogicalState.MARKED, child3.getState());
        assertEquals(LogicalState.PROVISIONED, child2.getState());

        assertEquals(3, change.getDeletedComponents().size());

        collector.collect(domain);

        assertNull(domain.getComponent(child1Uri));
        assertNull(domain.getComponent(child2Uri));
        assertNull(domain.getComponent(childCompositeUri));

    }
}
