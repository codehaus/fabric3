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
package org.fabric3.maven.contribution;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.util.io.FileHelper;
import org.fabric3.host.contribution.InstallException;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.Loader;
import org.fabric3.spi.introspection.xml.LoaderException;
import org.fabric3.model.type.ValidationContext;
import org.fabric3.spi.services.contenttype.ContentTypeResolutionException;
import org.fabric3.spi.services.contenttype.ContentTypeResolver;
import org.fabric3.spi.contribution.archive.Action;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.ContributionManifest;
import org.fabric3.spi.contribution.ContributionProcessor;
import org.fabric3.spi.contribution.ProcessorRegistry;
import org.fabric3.spi.contribution.Resource;

/**
 * Processes a Maven module directory.
 *
 * @version $Rev: 5954 $ $Date: 2008-11-15 12:52:25 -0800 (Sat, 15 Nov 2008) $
 */
@EagerInit
public class ModuleContributionProcessor implements ContributionProcessor {
    public static final List<String> CONTENT_TYPES = initializeContentTypes();

    private ProcessorRegistry registry;
    private ContentTypeResolver contentTypeResolver;
    private Loader loader;

    public ModuleContributionProcessor(@Reference ProcessorRegistry registry,
                                       @Reference ContentTypeResolver contentTypeResolver,
                                       @Reference Loader loader) {
        this.registry = registry;
        this.contentTypeResolver = contentTypeResolver;
        this.loader = loader;
    }

    public List<String> getContentTypes() {
        return CONTENT_TYPES;
    }

    @Init
    public void init() {
        registry.register(this);
    }

    public void process(Contribution contribution, ValidationContext context, ClassLoader loader) throws InstallException {
        ClassLoader oldClassloader = Thread.currentThread().getContextClassLoader();
        URI contributionUri = contribution.getUri();
        try {
            Thread.currentThread().setContextClassLoader(loader);
            for (Resource resource : contribution.getResources()) {
                if (!resource.isProcessed()) {
                    registry.processResource(contributionUri, resource, context, loader);
                }
            }
        } finally {
            Thread.currentThread().setContextClassLoader(oldClassloader);
        }
    }

    public void processManifest(Contribution contribution, final ValidationContext context) throws InstallException {
        ContributionManifest manifest;
        try {
            URL sourceUrl = contribution.getLocation();
            URL manifestURL = new URL(sourceUrl.toExternalForm() + "/classes/META-INF/sca-contribution.xml");
            ClassLoader cl = getClass().getClassLoader();
            URI uri = contribution.getUri();
            IntrospectionContext childContext = new DefaultIntrospectionContext(cl, uri, null);
            manifest = loader.load(manifestURL, ContributionManifest.class, childContext);
            if (childContext.hasErrors()) {
                context.addErrors(childContext.getErrors());
            }
            if (childContext.hasWarnings()) {
                context.addWarnings(childContext.getWarnings());
            }
            contribution.setManifest(manifest);
        } catch (LoaderException e) {
            if (e.getCause() instanceof FileNotFoundException) {
                // ignore no manifest found
            } else {
                throw new InstallException(e);
            }
        } catch (MalformedURLException e) {
            // ignore no manifest found
        }

        iterateArtifacts(contribution, context, new Action() {
            public void process(Contribution contribution, String contentType, URL url)
                    throws InstallException {
                InputStream stream = null;
                try {
                    stream = url.openStream();
                    registry.processManifestArtifact(contribution.getManifest(), contentType, stream, context);
                } catch (IOException e) {
                    throw new InstallException(e);
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
    }

    public void index(Contribution contribution, final ValidationContext context) throws InstallException {
        iterateArtifacts(contribution, context, new Action() {
            public void process(Contribution contribution, String contentType, URL url)
                    throws InstallException {
                registry.indexResource(contribution, contentType, url, context);
            }
        });
    }

    private void iterateArtifacts(Contribution contribution, final ValidationContext context, Action action) throws InstallException {
        File root = FileHelper.toFile(contribution.getLocation());
        assert root.isDirectory();
        iterateArtifactsResursive(contribution, context, action, root);
    }

    private void iterateArtifactsResursive(Contribution contribution, final ValidationContext context, Action action, File dir)
            throws InstallException {
        File[] files = dir.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                iterateArtifactsResursive(contribution, context, action, file);
            } else {
                try {
                    URL entryUrl = file.toURI().toURL();
                    String contentType = contentTypeResolver.getContentType(entryUrl);
                    action.process(contribution, contentType, entryUrl);
                } catch (MalformedURLException e) {
                    context.addWarning(new ContributionIndexingFailure(file, e));
                } catch (IOException e) {
                    context.addWarning(new ContributionIndexingFailure(file, e));
                } catch (ContentTypeResolutionException e) {
                    context.addWarning(new ContributionIndexingFailure(file, e));
                }
            }
        }

    }

    private static List<String> initializeContentTypes() {
        List<String> list = new ArrayList<String>(1);
        list.add("application/vnd.fabric3.maven-project");
        return list;
    }

}