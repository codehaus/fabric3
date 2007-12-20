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
package org.fabric3.idea.contribution;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.fabric.services.contribution.processor.Action;
import org.fabric3.fabric.util.FileHelper;
import org.fabric3.host.contribution.ContributionException;
import static org.fabric3.idea.IntelliJConstants.TEST_COMPOSITE;
import org.fabric3.idea.run.IntelliJHostInfo;
import org.fabric3.junit.ImplementationJUnit;
import org.fabric3.junit.JUnitComponentTypeLoader;
import org.fabric3.loader.common.LoaderContextImpl;
import org.fabric3.maven.runtime.MavenHostInfo;
import org.fabric3.scdl.Autowire;
import org.fabric3.scdl.ComponentDefinition;
import org.fabric3.scdl.Composite;
import org.fabric3.scdl.CompositeImplementation;
import org.fabric3.scdl.Include;
import org.fabric3.spi.loader.LoaderContext;
import org.fabric3.spi.loader.LoaderException;
import org.fabric3.spi.loader.LoaderRegistry;
import org.fabric3.spi.model.type.ContributionResourceDescription;
import org.fabric3.spi.services.contenttype.ContentTypeResolutionException;
import org.fabric3.spi.services.contenttype.ContentTypeResolver;
import org.fabric3.spi.services.contribution.ArtifactLocationEncoder;
import org.fabric3.spi.services.contribution.Contribution;
import org.fabric3.spi.services.contribution.ContributionManifest;
import org.fabric3.spi.services.contribution.ContributionProcessor;
import org.fabric3.spi.services.contribution.MetaDataStore;
import org.fabric3.spi.services.contribution.ProcessorRegistry;
import org.fabric3.spi.services.contribution.QNameSymbol;
import org.fabric3.spi.services.contribution.Resource;
import org.fabric3.spi.services.contribution.ResourceElement;
import org.fabric3.spi.services.factories.xml.XMLFactory;

/**
 * Introspects and processes IntelliJ module contribution.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class IntelliJModuleContributionProcessor implements ContributionProcessor {
    public static final String[] CONTENT_TYPES = new String[]{"application/vnd.fabric3.intellij-module"};

    private ProcessorRegistry registry;
    private ContentTypeResolver contentTypeResolver;
    private ArtifactLocationEncoder encoder;
    private IntelliJHostInfo hostInfo;
    private XMLFactory xmlFactory;
    private LoaderRegistry loaderRegistry;
    private JUnitComponentTypeLoader componentTypeLoader;
    private MetaDataStore store;

    public IntelliJModuleContributionProcessor(@Reference ProcessorRegistry registry,
                                               @Reference ContentTypeResolver contentTypeResolver,
                                               @Reference ArtifactLocationEncoder encoder,
                                               @Reference MavenHostInfo hostInfo,
                                               @Reference XMLFactory xmlFactory,
                                               @Reference LoaderRegistry loaderRegistry,
                                               @Reference JUnitComponentTypeLoader componentTypeLoader,
                                               @Reference MetaDataStore store) {
        this.registry = registry;
        this.contentTypeResolver = contentTypeResolver;
        this.encoder = encoder;
        // FIXME need to cast - introspect host info type in AbstractRuntime
        this.hostInfo = (IntelliJHostInfo) hostInfo;
        this.xmlFactory = xmlFactory;
        this.loaderRegistry = loaderRegistry;
        this.componentTypeLoader = componentTypeLoader;
        this.store = store;
    }

    public String[] getContentTypes() {
        return CONTENT_TYPES;
    }

    @Init
    public void init() {
        registry.register(this);
    }

    public void processManifest(Contribution contribution) throws ContributionException {
        XMLStreamReader reader = null;
        try {

            URL manifestURL = new URL(hostInfo.getOutputDirectory(), "/classes/META-INF/sca-contribution.xml");
            InputStream stream;
            try {
                stream = manifestURL.openStream();
            } catch (FileNotFoundException e) {
                ContributionManifest manifest = new ContributionManifest();
                contribution.setManifest(manifest);
                return;
            }
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

    public void process(Contribution contribution, ClassLoader loader) throws ContributionException {
        ClassLoader oldClassloader = Thread.currentThread().getContextClassLoader();
        URI contributionUri = contribution.getUri();
        try {
            Thread.currentThread().setContextClassLoader(loader);
            for (Resource resource : contribution.getResources()) {
                registry.processResource(contributionUri, resource, loader);
            }
            createTestComposite(contribution, contributionUri, loader);
        } finally {
            Thread.currentThread().setContextClassLoader(oldClassloader);
        }
    }

    public void updateContributionDescription(Contribution contribution, ContributionResourceDescription description)
        throws ContributionException {
        URL encodedClasses = encoder.encode(hostInfo.getOutputDirectory());
        description.addArtifactUrl(encodedClasses);
        URL encodedTestClasses = encoder.encode(hostInfo.getTestOutputDirectory());
        description.addArtifactUrl(encodedTestClasses);
        for (URL url : hostInfo.getDependencyUrls()) {
            description.addArtifactUrl(encoder.encode(url));
        }
    }

    private void createTestComposite(Contribution contribution, URI contributionUri, ClassLoader loader)
        throws ContributionException {
        // fabricate the test composite by including test components
        Composite composite = new Composite(TEST_COMPOSITE);
        for (String type : hostInfo.getJUnitComponentImplementations()) {
            LoaderContext context = new LoaderContextImpl(contributionUri, loader, null);
            ImplementationJUnit implJunit = new ImplementationJUnit(type);
            try {
                componentTypeLoader.load(implJunit, context);
            } catch (LoaderException e) {
                throw new ContributionException("Error creating test composite", e);
            }
            ComponentDefinition<ImplementationJUnit> definition =
                new ComponentDefinition<ImplementationJUnit>(type, implJunit);
            // autowire references
            definition.setAutowire(Autowire.ON);
            composite.add(definition);
        }
        for (QName includedName : hostInfo.getIncludedComposites()) {
            QNameSymbol symbol = new QNameSymbol(includedName);
            ResourceElement<QNameSymbol, Composite> element =
                store.resolve(contributionUri, Composite.class, symbol);
            if (element == null) {
                // xcv throw better exception
                String identifier = includedName.toString();
                throw new ContributionException("Composite not found [" + identifier + "]", identifier);
            }
            Include include = new Include();
            include.setIncluded(element.getValue());
            include.setName(includedName);
            composite.add(include);
        }
        CompositeImplementation impl = new CompositeImplementation();
        impl.setName(TEST_COMPOSITE);
        impl.setComponentType(composite);
        QNameSymbol symbol = new QNameSymbol(TEST_COMPOSITE);
        ResourceElement<QNameSymbol, Composite> element = new ResourceElement<QNameSymbol, Composite>(symbol);
        element.setValue(composite);
        Resource resource;
        try {
            resource = new Resource(new URL("file://generated"), "generated");
        } catch (MalformedURLException e) {
            // should not happen
            throw new AssertionError(e);
        }
        resource.addResourceElement(element);
        contribution.addResource(resource);
    }

    private void iterateArtifacts(Contribution contribution, Action action) throws ContributionException {
        File root = FileHelper.toFile(hostInfo.getOutputDirectory());
        iterateArtifactsResursive(contribution, action, root);
        root = FileHelper.toFile(hostInfo.getTestOutputDirectory());
        iterateArtifactsResursive(contribution, action, root);
    }

    private void iterateArtifactsResursive(Contribution contribution, Action action, File dir)
        throws ContributionException {
        File[] files = dir.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                iterateArtifactsResursive(contribution, action, file);
            } else {
                try {
                    URL entryUrl = file.toURI().toURL();
                    String contentType = contentTypeResolver.getContentType(entryUrl);
                    action.process(contribution, contentType, entryUrl);
                } catch (MalformedURLException e) {
                    throw new ContributionException(e);
                } catch (IOException e) {
                    throw new ContributionException(e);
                } catch (ContentTypeResolutionException e) {
                    throw new ContributionException(e);
                }
            }
        }

    }


}
