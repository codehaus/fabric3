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
package org.fabric3.management.rest.framework.domain.contribution;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.xml.namespace.QName;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.host.contribution.ContributionService;
import org.fabric3.host.contribution.ContributionSource;
import org.fabric3.host.contribution.Deployable;
import org.fabric3.management.rest.model.HttpStatus;
import org.fabric3.management.rest.model.Resource;
import org.fabric3.management.rest.model.Response;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.ContributionState;
import org.fabric3.spi.contribution.MetaDataStore;

/**
 * @version $Rev: 9923 $ $Date: 2011-02-03 17:11:06 +0100 (Thu, 03 Feb 2011) $
 */
public class ProfilesResourceServiceTestCase extends TestCase {
    private static final URI PROFILE_URI = URI.create("profile");
    private ProfilesResourceService service;
    private ContributionService contributionService;
    private MetaDataStore store;

    @SuppressWarnings({"unchecked"})
    public void testGetProfiles() throws Exception {
        URI contributionUri = URI.create("thecontribution");
        Contribution contribution = createContribution(contributionUri);

        EasyMock.expect(store.getContributions()).andReturn(Collections.singleton(contribution));

        HttpServletRequest request = EasyMock.createMock(HttpServletRequest.class);
        EasyMock.expect(request.getRequestURL()).andReturn(new StringBuffer("http:/localhost/management/domain/contributions")).atLeastOnce();
        EasyMock.replay(contributionService, store, request);


        Resource resource = service.getProfiles(request);
        Set<URI> profiles = (Set<URI>) resource.getProperties().get("profiles");
        assertTrue(profiles.contains(PROFILE_URI));
        EasyMock.verify(contributionService, store, request);
    }

    @SuppressWarnings({"unchecked"})
    public void testCreateProfile() throws Exception {
        URI profileUri = URI.create("theprofile");
        URI contributionUri = URI.create("contribution1.jar");

        EasyMock.expect(contributionService.exists(EasyMock.eq(contributionUri))).andReturn(false);
        EasyMock.expect(contributionService.store(EasyMock.isA(ContributionSource.class))).andReturn(contributionUri);
        contributionService.registerProfile(EasyMock.eq(profileUri), EasyMock.isA(List.class));
        contributionService.installProfile(profileUri);

        HttpServletRequest request = EasyMock.createMock(HttpServletRequest.class);
        EasyMock.expect(request.getPathInfo()).andReturn("/theprofile").atLeastOnce();
        ClassLoader loader = getClass().getClassLoader();
        InputStream resourceStream = loader.getResourceAsStream("org/fabric3/management/rest/framework/domain/contribution/test.jar");
        MockStream mockStream = new MockStream(resourceStream);
        EasyMock.expect(request.getInputStream()).andReturn(mockStream).atLeastOnce();

        EasyMock.replay(contributionService, store, request);

        Response response = service.createProfile(request);
        assertEquals(HttpStatus.CREATED, response.getStatus());
        assertEquals("/theprofile", response.getHeaders().get("Location"));
        EasyMock.verify(contributionService, store, request);

    }

    @SuppressWarnings({"unchecked"})
    public void testDeleteProfile() throws Exception {
        URI profileUri = URI.create("theprofile");

        contributionService.uninstallProfile(profileUri);
        contributionService.removeProfile(profileUri);

        EasyMock.replay(contributionService, store);

        service.deleteProfile("theprofile");
        EasyMock.verify(contributionService, store);

    }


    @Override
    protected void setUp() throws Exception {
        super.setUp();

        ContributionsResourceMonitor monitor = EasyMock.createNiceMock(ContributionsResourceMonitor.class);
        EasyMock.replay(monitor);

        contributionService = EasyMock.createMock(ContributionService.class);
        store = EasyMock.createMock(MetaDataStore.class);

        service = new ProfilesResourceService(contributionService, store, monitor);
    }

    private Contribution createContribution(URI contributionUri) {
        Contribution contribution = new Contribution(contributionUri);
        QName compositeName = new QName("test", "composite");
        Deployable deployable = new Deployable(compositeName);
        contribution.getManifest().addDeployable(deployable);
        contribution.setState(ContributionState.INSTALLED);
        contribution.addProfile(PROFILE_URI);
        return contribution;
    }


    private class MockStream extends ServletInputStream {
        private InputStream stream;

        private MockStream(InputStream stream) {
            this.stream = stream;
        }

        @Override
        public int read() throws IOException {
            return 0;
        }

        @Override
        public int read(byte[] b) throws IOException {
            return stream.read(b);
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            return stream.read(b, off, len);
        }

        @Override
        public long skip(long n) throws IOException {
            return stream.skip(n);
        }

        @Override
        public int available() throws IOException {
            return stream.available();
        }

        @Override
        public void close() throws IOException {
            stream.close();
        }

        @Override
        public void mark(int readlimit) {
            stream.mark(readlimit);
        }

        @Override
        public void reset() throws IOException {
            stream.reset();
        }

        @Override
        public boolean markSupported() {
            return stream.markSupported();
        }
    }


}
