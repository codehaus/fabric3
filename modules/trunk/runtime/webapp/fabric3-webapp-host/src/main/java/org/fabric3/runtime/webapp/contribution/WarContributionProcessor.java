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
package org.fabric3.runtime.webapp.contribution;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.servlet.ServletContext;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.host.contribution.InstallException;
import org.fabric3.introspection.DefaultIntrospectionContext;
import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.xml.Loader;
import org.fabric3.introspection.xml.LoaderException;
import org.fabric3.runtime.webapp.WebappHostInfo;
import org.fabric3.scdl.ValidationContext;
import org.fabric3.spi.services.contenttype.ContentTypeResolutionException;
import org.fabric3.spi.services.contenttype.ContentTypeResolver;
import org.fabric3.spi.services.contribution.Action;
import org.fabric3.spi.services.contribution.Contribution;
import org.fabric3.spi.services.contribution.ContributionManifest;
import org.fabric3.spi.services.contribution.ContributionProcessor;
import org.fabric3.spi.services.contribution.ProcessorRegistry;
import org.fabric3.spi.services.contribution.Resource;

/**
 * Processes a WAR contribution in an embedded runtime.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class WarContributionProcessor implements ContributionProcessor {

    public static final List<String> CONTENT_TYPES = initializeContentTypes();

    private WebappHostInfo info;
    private ProcessorRegistry registry;
    private ContentTypeResolver contentTypeResolver;
    private Loader loader;

    public WarContributionProcessor(@Reference WebappHostInfo info,
                                    @Reference ProcessorRegistry registry,
                                    @Reference ContentTypeResolver contentTypeResolver,
                                    @Reference Loader loader) {
        this.info = info;
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
        URI contributionUri = contribution.getUri();
        for (Resource resource : contribution.getResources()) {
            if (!resource.isProcessed()) {
                registry.processResource(contributionUri, resource, context, loader);
            }
        }
    }

    public void processManifest(Contribution contribution, final ValidationContext context) throws InstallException {
        URL manifestURL;
        try {
            manifestURL = info.getServletContext().getResource("/WEB-INF/sca-contribution.xml");
            if (manifestURL == null) {
                return;
            }
        } catch (MalformedURLException e) {
            // ignore
            return;
        }

        try {

            ClassLoader cl = getClass().getClassLoader();
            URI uri = contribution.getUri();
            IntrospectionContext childContext = new DefaultIntrospectionContext(cl, uri, null);
            ContributionManifest manifest = loader.load(manifestURL, ContributionManifest.class, childContext);
            if (childContext.hasErrors()) {
                context.addErrors(childContext.getErrors());
            }
            if (childContext.hasWarnings()) {
                context.addWarnings(childContext.getWarnings());
            }
            contribution.setManifest(manifest);
        } catch (LoaderException e) {
            throw new InstallException(e);
        }

        iterateArtifacts(contribution, new Action() {
            public void process(Contribution contribution, String contentType, URL url)
                    throws InstallException {
                InputStream stream = null;
                try {
                    stream = url.openStream();
                    registry.processManifestArtifact(contribution.getManifest(), contentType, stream, context);
                } catch (FileNotFoundException e) {
                    // Tomcat hack: swallow the exception as directories under META-INF are reported as resources from the servlet context but
                    // Tomcat's underlying URLConnection returns a file not found exception when URL.openStream() is called. This is safe as
                    // interateArtifacts only iterates entries found in a contribution archive and FileNotFoundException should generally not happen.
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
        iterateArtifacts(contribution, new Action() {
            public void process(Contribution contribution, String contentType, URL url)
                    throws InstallException {
                registry.indexResource(contribution, contentType, url, context);
            }
        });
    }

    @SuppressWarnings({"unchecked"})
    private void iterateArtifacts(Contribution contribution, Action action) throws InstallException {
        ServletContext context = info.getServletContext();
        Set<String> metaInfpaths = context.getResourcePaths("/META-INF/");
        Set<String> webInfpaths = context.getResourcePaths("/WEB-INF/");
        try {
            processResources(metaInfpaths, action, contribution, context);
            processResources(webInfpaths, action, contribution, context);
        } catch (ContentTypeResolutionException e) {
            throw new InstallException(e);
        } catch (MalformedURLException e) {
            throw new InstallException(e);
        }
    }

    private void processResources(Set<String> paths, Action action, Contribution contribution,
                                  ServletContext context) throws MalformedURLException,
            InstallException, ContentTypeResolutionException {
        if (paths == null || paths.isEmpty()) return;
        for (String path : paths) {
            URL entryUrl = context.getResource(path);
            String contentType = contentTypeResolver.getContentType(entryUrl);
            action.process(contribution, contentType, entryUrl);
        }
    }

    private static List<String> initializeContentTypes() {
        List<String> list = new ArrayList<String>(1);
        list.add("application/vnd.fabric3.war");
        return list;
    }

}