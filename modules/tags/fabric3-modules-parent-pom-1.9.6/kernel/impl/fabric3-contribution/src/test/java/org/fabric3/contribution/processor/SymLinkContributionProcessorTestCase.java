/*
* Fabric3
* Copyright (c) 2009-2012 Metaform Systems
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
package org.fabric3.contribution.processor;

import java.net.URI;
import java.net.URL;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.host.stream.UrlSource;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.ProcessorRegistry;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;

/**
 * @version $Rev: 9763 $ $Date: 2011-01-03 01:48:06 +0100 (Mon, 03 Jan 2011) $
 */
public class SymLinkContributionProcessorTestCase extends TestCase {
    private SymLinkContributionProcessor processor;
    private ProcessorRegistry registry;
    private URL file;
    private Contribution contribution;
    private IntrospectionContext context;

    public void testInit() throws Exception {
        registry.register(processor);
        registry.unregister(processor);
        EasyMock.replay(registry);

        processor.init();
        processor.destroy();
        EasyMock.verify(registry);
    }

    public void testCanProcess() throws Exception {
        EasyMock.replay(registry);
        assertTrue(processor.canProcess(contribution));
        EasyMock.verify(registry);
    }

    public void testProcessManifest() throws Exception {
        registry.processManifest(EasyMock.isA(Contribution.class), EasyMock.isA(IntrospectionContext.class));
        EasyMock.replay(registry);
        processor.processManifest(contribution, context);

        assertNotNull(contribution.getMetaData(Contribution.class, contribution.getUri()));
        EasyMock.verify(registry);
    }

    public void testIndex() throws Exception {
        registry.indexContribution(EasyMock.isA(Contribution.class), EasyMock.isA(IntrospectionContext.class));
        EasyMock.replay(registry);

        Contribution synthetic = new Contribution(URI.create("synthetic"));
        contribution.addMetaData(URI.create("contribution"), synthetic);
        processor.index(contribution, context);
        EasyMock.verify(registry);
    }

    public void testProcess() throws Exception {
        registry.processContribution(EasyMock.isA(Contribution.class), EasyMock.isA(IntrospectionContext.class));
        EasyMock.replay(registry);

        Contribution synthetic = new Contribution(URI.create("synthetic"));
        contribution.addMetaData(URI.create("contribution"), synthetic);
        processor.process(contribution, context);
        EasyMock.verify(registry);
    }

    protected void setUp() throws Exception {
        super.setUp();
        registry = EasyMock.createMock(ProcessorRegistry.class);
        processor = new SymLinkContributionProcessor(registry);
        file = getClass().getResource("sym.contribution");
        contribution = new Contribution(URI.create("contribution"), new UrlSource(file), file, -1, "application/xml", false);
        context = new DefaultIntrospectionContext();
    }
}
