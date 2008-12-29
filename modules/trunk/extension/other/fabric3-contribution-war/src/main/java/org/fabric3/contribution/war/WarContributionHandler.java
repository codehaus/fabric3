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
package org.fabric3.contribution.war;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.api.annotation.Monitor;
import org.fabric3.host.contribution.InstallException;
import org.fabric3.spi.contribution.Constants;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.ContributionManifest;
import org.fabric3.spi.contribution.ProcessorRegistry;
import org.fabric3.spi.contribution.archive.Action;
import org.fabric3.spi.contribution.archive.ArchiveContributionHandler;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.Loader;
import org.fabric3.spi.introspection.xml.LoaderException;
import org.fabric3.spi.services.contenttype.ContentTypeResolutionException;
import org.fabric3.spi.services.contenttype.ContentTypeResolver;

/**
 * Introspects a WAR contribution, delegating to ResourceProcessors for handling leaf-level children.
 */
@EagerInit
public class WarContributionHandler implements ArchiveContributionHandler {
    private final Loader loader;
    private final ContentTypeResolver contentTypeResolver;
    private WarContributionMonitor monitor;
    private ProcessorRegistry registry;

    public WarContributionHandler(@Reference ProcessorRegistry processorRegistry,
                                  @Reference Loader loader,
                                  @Reference ContentTypeResolver contentTypeResolver,
                                  @Monitor WarContributionMonitor monitor) {

        this.registry = processorRegistry;
        this.loader = loader;
        this.contentTypeResolver = contentTypeResolver;
        this.monitor = monitor;
    }

    @Init
    public void init() {
        monitor.extensionStarted();
    }

    @Destroy
    public void destroy() {
        monitor.extensionStopped();
    }

    public String getContentType() {
        return Constants.ZIP_CONTENT_TYPE;
    }

    public boolean canProcess(Contribution contribution) {
        String sourceUrl = contribution.getLocation().toString();
        return sourceUrl.endsWith(".war");
    }

    public void processManifest(Contribution contribution, final IntrospectionContext context) throws InstallException {
        ContributionManifest manifest;
        try {
            URL sourceUrl = contribution.getLocation();
            URL manifestURL = new URL("jar:" + sourceUrl.toExternalForm() + "!/WEB-INF/sca-contribution.xml");
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

    }

    public void iterateArtifacts(Contribution contribution, Action action) throws InstallException {
        URL location = contribution.getLocation();
        ZipInputStream zipStream = null;
        try {
            zipStream = new ZipInputStream(location.openStream());
            while (true) {
                ZipEntry entry = zipStream.getNextEntry();
                if (entry == null) {
                    // EOF
                    break;
                }
                if (entry.isDirectory()) {
                    continue;
                }

                URL entryUrl = new URL("jar:" + location.toExternalForm() + "!/" + entry.getName());
                // hack to return the correct content type
                String contentType = contentTypeResolver.getContentType(new URL(location, entry.getName()));

                // String contentType = contentTypeResolver.getContentType(entryUrl);
                // skip entry if we don't recognize the content type
                if (contentType == null) {
                    continue;
                }
                action.process(contribution, contentType, entryUrl);
            }
        } catch (ContentTypeResolutionException e) {
            throw new InstallException(e);
        } catch (MalformedURLException e) {
            throw new InstallException(e);
        } catch (IOException e) {
            throw new InstallException(e);
        } finally {
            try {
                if (zipStream != null) {
                    zipStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}