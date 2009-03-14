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
package org.fabric3.admin.controller;

import java.net.URI;
import java.util.List;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.host.runtime.HostInfo;
import org.fabric3.management.domain.ComponentInfo;
import org.fabric3.management.domain.DomainMBean;
import org.fabric3.management.domain.InvalidPathException;
import org.fabric3.model.type.component.ComponentDefinition;
import org.fabric3.model.type.component.CompositeImplementation;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.services.lcm.LogicalComponentManager;

/**
 * @version $Revision$ $Date$
 */
public class DomainMBeanImplTestCase extends TestCase {
    private DomainMBean mBean;
    private static final URI DOMAIN = URI.create("fabric3://domain");
    private static final URI CHILD1 = URI.create("fabric3://domain/child1");
    private static final URI CHILD2 = URI.create("fabric3://domain/child2");
    private static final URI GRAND_CHILD1 = URI.create("fabric3://domain/child1/grandChild1");
    private static final URI GRAND_GRAND_CHILD = URI.create("fabric3://domain/child1/grandChild1/grandGrandChild");

    public void testListPath() throws Exception {
        List<ComponentInfo> infos = mBean.getDeployedComponents("/child1");
        assertEquals(1, infos.size());
        assertEquals(GRAND_CHILD1, infos.get(0).getUri());
    }

    public void testListDomain() throws Exception {
        List<ComponentInfo> infos = mBean.getDeployedComponents("/");
        assertEquals(2, infos.size());
        URI uri = infos.get(0).getUri();
        assertTrue(CHILD1 == uri || CHILD2 == uri);
        uri = infos.get(1).getUri();
        assertTrue(CHILD1 == uri || CHILD2 == uri);
    }

    public void testListGrandGrandChild() throws Exception {
        List<ComponentInfo> infos = mBean.getDeployedComponents("/child1/grandChild1");
        assertEquals(1, infos.size());
    }

    public void testListEmptyPath() throws Exception {
        List<ComponentInfo> infos = mBean.getDeployedComponents("/child2");
        assertEquals(0, infos.size());
    }

    public void testListNonExistentEmptyPath() throws Exception {
        try {
            mBean.getDeployedComponents("/child3");
            fail();
        } catch (InvalidPathException e) {
            // expected
        }
    }

    public void testInvalidPath() throws Exception {
        try {
            mBean.getDeployedComponents("invalid");
            fail();
        } catch (InvalidPathException e) {
            // expected
        }
    }

    public void testListNonCompositePath() throws Exception {
        try {
            mBean.getDeployedComponents("/child1/grandChild1/grandGrandChild");
            fail();
        } catch (InvalidPathException e) {
            // expected
        }
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        HostInfo info = EasyMock.createMock(HostInfo.class);
        EasyMock.expect(info.getDomain()).andReturn(DOMAIN);
        EasyMock.replay(info);

        LogicalCompositeComponent domain = new LogicalCompositeComponent(DOMAIN, null, null);
        ComponentDefinition<CompositeImplementation> child1Def = new ComponentDefinition<CompositeImplementation>("child1", null);
        LogicalCompositeComponent child1 = new LogicalCompositeComponent(CHILD1, child1Def, domain);
        domain.addComponent(child1);
        ComponentDefinition<CompositeImplementation> grandChild1Def = new ComponentDefinition<CompositeImplementation>("child1", null);
        LogicalCompositeComponent grandChild1 = new LogicalCompositeComponent(GRAND_CHILD1, grandChild1Def, child1);
        child1.addComponent(grandChild1);
        ComponentDefinition<CompositeImplementation> grandGrandChild1Def = new ComponentDefinition<CompositeImplementation>("child1", null);
        LogicalComponent<?> grandGrandChild1 = new LogicalComponent(GRAND_GRAND_CHILD, grandGrandChild1Def, domain);
        grandChild1.addComponent(grandGrandChild1);
        ComponentDefinition<CompositeImplementation> child2Def = new ComponentDefinition<CompositeImplementation>("child1", null);
        LogicalCompositeComponent child2 = new LogicalCompositeComponent(CHILD2, child2Def, domain);
        domain.addComponent(child2);

        LogicalComponentManager lcm = EasyMock.createMock(LogicalComponentManager.class);
        EasyMock.expect(lcm.getRootComponent()).andReturn(domain).atLeastOnce();
        EasyMock.replay(lcm);
        mBean = new DistributedDomainMBean(null, null, lcm, info, null);
    }
}
