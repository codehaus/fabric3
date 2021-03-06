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
package org.fabric3.contribution.archive;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import org.osoa.sca.annotations.Reference;

import org.fabric3.host.Constants;
import org.fabric3.host.contribution.InstallException;
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
import org.fabric3.util.io.FileHelper;

/**
 * Handles exploded archives on a filesystem.
 */
public class ExplodedArchiveContributionHandler implements ArchiveContributionHandler {
    private Loader loader;
    private final ContentTypeResolver contentTypeResolver;
    private ProcessorRegistry registry;


    public ExplodedArchiveContributionHandler(@Reference Loader loader,
                                              @Reference ContentTypeResolver contentTypeResolver,
                                              @Reference ProcessorRegistry registry) {
        this.loader = loader;
        this.contentTypeResolver = contentTypeResolver;
        this.registry = registry;
    }

    public String getContentType() {
        return Constants.FOLDER_CONTENT_TYPE;
    }

    public boolean canProcess(Contribution contribution) {
        return Constants.FOLDER_CONTENT_TYPE.equals(contribution.getContentType());
    }

    public void processManifest(Contribution contribution, final IntrospectionContext context) throws InstallException {
        ContributionManifest manifest;
        try {
            URL sourceUrl = contribution.getLocation();
            URL manifestUrl = new URL(sourceUrl.toString() + "/META-INF/sca-contribution.xml");
            ClassLoader cl = getClass().getClassLoader();
            URI uri = contribution.getUri();
            IntrospectionContext childContext = new DefaultIntrospectionContext(cl, uri, null);
            manifest = loader.load(manifestUrl, ContributionManifest.class, childContext);
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

    public void iterateArtifacts(Contribution contribution, Action action)
            throws InstallException {
        File root = FileHelper.toFile(contribution.getLocation());
        assert root.isDirectory();
        iterateArtifactsResursive(contribution, action, root);
    }

    protected void iterateArtifactsResursive(Contribution contribution, Action action, File dir)
            throws InstallException {
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
                    throw new InstallException(e);
                } catch (IOException e) {
                    throw new InstallException(e);
                } catch (ContentTypeResolutionException e) {
                    throw new InstallException(e);
                }
            }
        }

    }

}
