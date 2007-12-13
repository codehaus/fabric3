/*
 * See the NOTICE file distributed with this work for information
 * regarding copyright ownership.  This file is licensed
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.fabric3.runtime.webapp.contribution;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Set;
import javax.servlet.ServletContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.fabric.services.contribution.processor.Action;
import org.fabric3.host.contribution.ContributionException;
import org.fabric3.loader.common.LoaderContextImpl;
import org.fabric3.runtime.webapp.WebappHostInfo;
import org.fabric3.spi.loader.LoaderContext;
import org.fabric3.spi.loader.LoaderException;
import org.fabric3.spi.loader.LoaderRegistry;
import org.fabric3.spi.model.type.ContributionResourceDescription;
import org.fabric3.spi.services.contenttype.ContentTypeResolutionException;
import org.fabric3.spi.services.contenttype.ContentTypeResolver;
import org.fabric3.spi.services.contribution.Contribution;
import org.fabric3.spi.services.contribution.ContributionManifest;
import org.fabric3.spi.services.contribution.ContributionProcessor;
import org.fabric3.spi.services.contribution.ProcessorRegistry;
import org.fabric3.spi.services.contribution.Resource;
import org.fabric3.spi.services.factories.xml.XMLFactory;

/**
 * Processes a WAR contribution.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class WarContributionProcessor implements ContributionProcessor {
    public static final String[] CONTENT_TYPES = new String[]{"application/vnd.fabric3.war"};

    private WebappHostInfo info;
    private ProcessorRegistry registry;
    private ContentTypeResolver contentTypeResolver;
    private XMLFactory xmlFactory;
    private LoaderRegistry loaderRegistry;

    public WarContributionProcessor(@Reference WebappHostInfo info,
                                    @Reference ProcessorRegistry registry,
                                    @Reference ContentTypeResolver contentTypeResolver,
                                    @Reference XMLFactory xmlFactory,
                                    @Reference LoaderRegistry loaderRegistry) {
        this.info = info;
        this.registry = registry;
        this.contentTypeResolver = contentTypeResolver;
        this.xmlFactory = xmlFactory;
        this.loaderRegistry = loaderRegistry;
    }

    public String[] getContentTypes() {
        return CONTENT_TYPES;
    }

    @Init
    public void init() {
        registry.register(this);
    }

    public void process(Contribution contribution, ClassLoader loader) throws ContributionException {
        URI contributionUri = contribution.getUri();
        for (Resource resource : contribution.getResources()) {
            registry.processResource(contributionUri, resource, loader);
        }
    }

    public void processManifest(Contribution contribution) throws ContributionException {
        XMLStreamReader reader = null;
        try {
            URL manifestURL = info.getServletContext().getResource("/WEB-INF/sca-contribution.xml");
            if (manifestURL == null) {
                ContributionManifest manifest = new ContributionManifest();
                contribution.setManifest(manifest);
                return;
            }
            InputStream stream = manifestURL.openStream();
            reader = xmlFactory.newInputFactoryInstance().createXMLStreamReader(stream);
            reader.nextTag();
            ClassLoader cl = getClass().getClassLoader();
            URI uri = contribution.getUri();
            LoaderContext context = new LoaderContextImpl(cl, uri, null);
            ContributionManifest manifest = loaderRegistry.load(reader, ContributionManifest.class, context);
            contribution.setManifest(manifest);
            iterateArtifacts(contribution, new Action() {
                public void process(Contribution contribution, String contentType, URL url)
                        throws ContributionException {
                    InputStream stream = null;
                    try {
                        stream = url.openStream();
                        registry.processManifestArtifact(contribution.getManifest(), contentType, stream);
                    } catch (IOException e) {
                        throw new ContributionException(e);
                    } finally {
                        try {
                            if (stream != null) {
                                stream.close();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        } catch (XMLStreamException e) {
            throw new ContributionException(e);
        } catch (LoaderException e) {
            throw new ContributionException(e);
        } catch (IOException e) {
            throw new ContributionException(e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (XMLStreamException e) {
                    // TODO log exception
                    e.printStackTrace();
                }
            }
        }
    }

    public void index(Contribution contribution) throws ContributionException {
        iterateArtifacts(contribution, new Action() {
            public void process(Contribution contribution, String contentType, URL url)
                    throws ContributionException {
                registry.indexResource(contribution, contentType, url);
            }
        });
    }

    public void updateContributionDescription(Contribution contribution, ContributionResourceDescription description)
            throws ContributionException {
        // do nothing since classes are local and on the host classloader's parent classpath
    }

    @SuppressWarnings({"unchecked"})
    private void iterateArtifacts(Contribution contribution, Action action) throws ContributionException {
        ServletContext context = info.getServletContext();
        Set<String> metaInfpaths = context.getResourcePaths("/META-INF/");
        Set<String> webInfpaths = context.getResourcePaths("/WEB-INF/");
        try {
            for (String path : metaInfpaths) {
                URL entryUrl = context.getResource(path);
                String contentType = contentTypeResolver.getContentType(entryUrl);
                action.process(contribution, contentType, entryUrl);
            }
            for (String path : webInfpaths) {
                URL entryUrl = context.getResource(path);
                String contentType = contentTypeResolver.getContentType(entryUrl);
                action.process(contribution, contentType, entryUrl);
            }
        } catch (ContentTypeResolutionException e) {
            throw new ContributionException(e);
        } catch (MalformedURLException e) {
            throw new ContributionException(e);
        }
    }

}