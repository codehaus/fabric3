/*
* Fabric3
* Copyright (c) 2009-2011 Metaform Systems
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
package org.fabric3.contribution.archive;

import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.regex.Pattern;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.host.stream.Source;
import org.fabric3.host.stream.UrlSource;
import org.fabric3.spi.contribution.ContentTypeResolver;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.ContributionManifest;
import org.fabric3.spi.contribution.archive.Action;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.Loader;

import static org.fabric3.spi.contribution.Constants.EXPLODED_CONTENT_TYPE;

/**
 * @version $Rev: 7146 $ $Date: 2009-06-15 01:28:54 +0200 (Mon, 15 Jun 2009) $
 */
public class ExplodedArchiveContributionHandlerTestCase extends TestCase {
    private ExplodedArchiveContributionHandler handler;
    private Loader loader;

    public void testCanProcess() throws Exception {
        Contribution contribution = createContribution();
        assertTrue(handler.canProcess(contribution));
    }

    public void testProcessManifest() throws Exception {
        ContributionManifest manifest = new ContributionManifest();
        loader.load(EasyMock.isA(Source.class), EasyMock.eq(ContributionManifest.class), EasyMock.isA(IntrospectionContext.class));
        EasyMock.expectLastCall().andReturn(manifest);
        EasyMock.replay(loader);
        Contribution contribution = createContribution();

        DefaultIntrospectionContext context = new DefaultIntrospectionContext();
        handler.processManifest(contribution, context);

        EasyMock.verify(loader);
    }

    public void testIterateAllContents() throws Exception {
        Contribution contribution = createContribution();

        Action action = EasyMock.createMock(Action.class);
        action.process(EasyMock.isA(Contribution.class), EasyMock.isA(String.class), EasyMock.isA(URL.class));
        EasyMock.expectLastCall().times(2);  // 2 items in the archive minus the contribution manifest
        EasyMock.replay(action);

        handler.iterateArtifacts(contribution, action);

        EasyMock.verify(action);
    }

    public void testExcludeContents() throws Exception {
        Contribution contribution = createContribution();
        contribution.getManifest().setScanExcludes(Collections.singletonList(Pattern.compile("META-INF/test1.*.composite")));

        Action action = EasyMock.createMock(Action.class);
        action.process(EasyMock.isA(Contribution.class), EasyMock.isA(String.class), EasyMock.isA(URL.class));
        // Should only be 1 item in the archive minus the contribution manifest and the excluded file
        EasyMock.expectLastCall().times(1);
        EasyMock.replay(action);

        handler.iterateArtifacts(contribution, action);

        EasyMock.verify(action);
    }

    public void testDirectoryContents() throws Exception {
        Contribution contribution = createContribution();
        contribution.getManifest().setScanExcludes(Collections.singletonList(Pattern.compile("META-INF/.*")));

        Action action = EasyMock.createMock(Action.class);
        // no contents should be scanned
        EasyMock.replay(action);

        handler.iterateArtifacts(contribution, action);

        EasyMock.verify(action);
    }

    protected void setUp() throws Exception {
        super.setUp();
        ContentTypeResolver resolver = EasyMock.createMock(ContentTypeResolver.class);
        EasyMock.expect(resolver.getContentType(EasyMock.isA(URL.class))).andReturn("application/xml").anyTimes();
        loader = EasyMock.createMock(Loader.class);
        EasyMock.replay(resolver);

        handler = new ExplodedArchiveContributionHandler(loader, resolver);
    }

    private Contribution createContribution() {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        URL location = cl.getResource("./exploded");
        URI uri = URI.create("test");
        UrlSource source = new UrlSource(location);
        Contribution contribution = new Contribution(uri, source, location, -1, EXPLODED_CONTENT_TYPE, false);
        ContributionManifest manifest = new ContributionManifest();
        contribution.setManifest(manifest);
        return contribution;
    }

}
