/*
* Fabric3
* Copyright (c) 2009 Metaform Systems
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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.osoa.sca.annotations.Reference;

import org.fabric3.host.Constants;
import org.fabric3.host.contribution.InstallException;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.ContributionManifest;
import org.fabric3.spi.contribution.ProcessorRegistry;
import org.fabric3.spi.contribution.ContentTypeResolver;
import org.fabric3.spi.contribution.ContentTypeResolutionException;
import org.fabric3.spi.contribution.archive.Action;
import org.fabric3.spi.contribution.archive.ArchiveContributionHandler;
import org.fabric3.spi.contribution.manifest.JarManifestHandler;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.Loader;
import org.fabric3.spi.introspection.xml.LoaderException;

/**
 * Introspects a Zip-based contribution, delegating to ResourceProcessors for handling leaf-level children.
 */
public class ZipContributionHandler implements ArchiveContributionHandler {

    private List<JarManifestHandler> manifestHandlers = Collections.emptyList();
    private final Loader loader;
    private final ContentTypeResolver contentTypeResolver;
    private ProcessorRegistry registry;

    public ZipContributionHandler(@Reference ProcessorRegistry processorRegistry,
                                  @Reference Loader loader,
                                  @Reference ContentTypeResolver contentTypeResolver) {

        this.registry = processorRegistry;
        this.loader = loader;
        this.contentTypeResolver = contentTypeResolver;
    }

    @Reference(required = false)
    public void setManifestHandlers(List<JarManifestHandler> manifestHandlers) {
        this.manifestHandlers = manifestHandlers;
    }

    public String getContentType() {
        return Constants.ZIP_CONTENT_TYPE;
    }

    public boolean canProcess(Contribution contribution) {
        String sourceUrl = contribution.getLocation().toString();
        return sourceUrl.endsWith(".jar") || sourceUrl.endsWith(".zip");
    }

    public void processManifest(Contribution contribution, final IntrospectionContext context) throws InstallException {
        URL sourceUrl = contribution.getLocation();
        try {
            URL manifestURL = new URL("jar:" + sourceUrl.toExternalForm() + "!/META-INF/sca-contribution.xml");
            ClassLoader cl = getClass().getClassLoader();
            URI uri = contribution.getUri();
            IntrospectionContext childContext = new DefaultIntrospectionContext(uri, cl);
            ContributionManifest manifest = loader.load(manifestURL, ContributionManifest.class, childContext);
            if (childContext.hasErrors()) {
                context.addErrors(childContext.getErrors());
            }
            if (childContext.hasWarnings()) {
                context.addWarnings(childContext.getWarnings());
            }
            if (childContext.hasErrors() || childContext.hasWarnings()) {
                return;
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
        InputStream manifestStream = null;
        try {
            URL jarUrl = new URL("jar:" + sourceUrl.toExternalForm() + "!/META-INF/MANIFEST.MF");
            manifestStream = jarUrl.openStream();
            Manifest jarManifest = new Manifest(manifestStream);
            for (JarManifestHandler handler : manifestHandlers) {
                handler.processManifest(contribution.getManifest(), jarManifest, context);
            }
        } catch (MalformedURLException e) {
            // ignore no manifest found
        } catch (IOException e) {
            throw new InstallException(e);
        } finally {
            try {
                if (manifestStream != null) {
                    manifestStream.close();
                }
            } catch (IOException e) {
                // ignore
                e.printStackTrace();
            }
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
