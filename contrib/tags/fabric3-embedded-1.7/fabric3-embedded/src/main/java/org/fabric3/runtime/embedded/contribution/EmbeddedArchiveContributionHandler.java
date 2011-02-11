package org.fabric3.runtime.embedded.contribution;

import org.fabric3.host.contribution.InstallException;
import org.fabric3.host.stream.Source;
import org.fabric3.host.stream.UrlSource;
import org.fabric3.host.util.FileHelper;
import org.fabric3.runtime.embedded.api.EmbeddedComposite;
import org.fabric3.spi.contribution.ContentTypeResolutionException;
import org.fabric3.spi.contribution.ContentTypeResolver;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.ContributionManifest;
import org.fabric3.spi.contribution.archive.Action;
import org.fabric3.spi.contribution.archive.ArchiveContributionHandler;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.Loader;
import org.fabric3.spi.introspection.xml.LoaderException;
import org.osoa.sca.annotations.Reference;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.regex.Pattern;

/**
 * @author Michal Capo
 */
public class EmbeddedArchiveContributionHandler implements ArchiveContributionHandler {
    private Loader loader;
    private final ContentTypeResolver contentTypeResolver;

    public EmbeddedArchiveContributionHandler(@Reference Loader loader, @Reference ContentTypeResolver contentTypeResolver) {
        this.loader = loader;
        this.contentTypeResolver = contentTypeResolver;
    }

    public boolean canProcess(Contribution contribution) {
        URL location = contribution.getLocation();
        if (location == null || !"file".equals(location.getProtocol())) {
            return false;
        }
        File file = new File(location.getFile());
        String contentType = contribution.getContentType();
        return EmbeddedComposite.CONTENT_TYPE.equals(contentType);
    }

    public void processManifest(Contribution contribution, IntrospectionContext context) throws InstallException {
        ContributionManifest manifest;
        try {
            String sourceUrl = contribution.getLocation().toString();

            URL manifestUrl = new URL(sourceUrl + "/META-INF/sca-contribution.xml");
            File file = new File(manifestUrl.toURI());
            if (!file.exists()) {
                manifestUrl = new URL(sourceUrl + "/WEB-INF/sca-contribution.xml");
            }
            ClassLoader cl = getClass().getClassLoader();
            URI uri = contribution.getUri();
            IntrospectionContext childContext = new DefaultIntrospectionContext(uri, cl);
            Source source = new UrlSource(manifestUrl);
            manifest = loader.load(source, ContributionManifest.class, childContext);
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
        } catch (URISyntaxException e) {
            throw new InstallException(e);
        }
    }

    public void iterateArtifacts(Contribution contribution, Action action) throws InstallException {
        File root = FileHelper.toFile(contribution.getLocation());
        iterateArtifactsRecursive(contribution, action, root, root);
    }

    protected void iterateArtifactsRecursive(Contribution contribution, Action action, File dir, File root) throws InstallException {
        File[] files = dir.listFiles();
        ContributionManifest manifest = contribution.getManifest();
        if (null != files) {
            for (File file : files) {
                if (file.isDirectory()) {
                    iterateArtifactsRecursive(contribution, action, file, root);
                } else {
                    try {
                        if (file.getName().equals("sca-contribution.xml")) {
                            // don't index the manifest
                            continue;
                        }
                        URL entryUrl = file.toURI().toURL();
                        String contentType = contentTypeResolver.getContentType(entryUrl);
                        // skip entry if we don't recognize the content type
                        if (contentType == null) {
                            continue;
                        }
                        if (exclude(manifest, file, root)) {
                            continue;
                        }
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

    private boolean exclude(ContributionManifest manifest, File file, File root) {
        for (Pattern pattern : manifest.getScanExcludes()) {
            // construct a file name relative to the root directory as excludes are relative to the archive root
            String relativePathName = file.toURI().toString().substring(root.toURI().toString().length());
            if (pattern.matcher(relativePathName).matches()) {
                return true;
            }
        }
        return false;
    }

}
