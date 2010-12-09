package org.fabric3.runtime.embedded;

import org.fabric3.host.stream.Source;
import org.fabric3.host.stream.UrlSource;
import org.fabric3.runtime.embedded.api.EmbeddedComposite;
import org.fabric3.runtime.embedded.exception.EmbeddedFabric3SetupException;
import org.fabric3.runtime.embedded.util.FileSystem;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Michal Capo
 */
public class EmbeddedCompositeImpl implements EmbeddedComposite {

    private URI uri;
    private URL url;
    private long timestamp;
    private Source source;
    private String contentType;

    public EmbeddedCompositeImpl(final String compositePath) throws MalformedURLException, URISyntaxException {
        if (!compositePath.startsWith(EMBEDDED)) {
            throw new MalformedURLException(String.format("Composite path have to start with keyword '%1$s' followed by absolute package and composite name. E.g.: %1$s:/org.example/database.composite", EMBEDDED));
        }

        if (compositePath.startsWith(EMBEDDED_COMPOSITE)) {
            embeddedComposite(compositePath.replaceFirst(EMBEDDED_COMPOSITE, ""));
        } else if (compositePath.startsWith(EMBEDDED_WAR)) {
            embeddedWar(compositePath.replaceFirst(EMBEDDED_WAR, ""));
        } else {
            throw new MalformedURLException(String.format("Unknown embedded composite type %1$s.", compositePath));
        }
    }

    private void embeddedComposite(final String classPathLocation) throws MalformedURLException, URISyntaxException {
        File compositeFile = new File(FileSystem.fileAtClassPath(classPathLocation).toURI());

        if (!FileSystem.isAbsolute(classPathLocation) || !FileSystem.exists(compositeFile)) {
            throw new EmbeddedFabric3SetupException(String.format("Composite file path '%1$s' have to be absolute or doesn't exists.", classPathLocation));
        }

        try {
            this.url = compositeFile.getParentFile().toURI().toURL();
        } catch (MalformedURLException e) {
            throw new EmbeddedFabric3SetupException(String.format("Cannot get parent folder for '%1$s'.", classPathLocation));
        }

        Matcher m = Pattern.compile("(?:.*)/(.*)").matcher(classPathLocation);
        m.find();
        this.uri = URI.create(m.group(1));
        this.timestamp = System.currentTimeMillis();
        this.source = new UrlSource(url);
        this.contentType = CONTENT_TYPE_CLASSPATH;
    }

    private void embeddedWar(final String fileSystemLocation) {
        if (!FileSystem.isAbsolute(fileSystemLocation) || !FileSystem.exists(fileSystemLocation)) {
            throw new EmbeddedFabric3SetupException(String.format("Composite file path '%1$s' have to be absolute or doesn't exists.", fileSystemLocation));
        }

        File compositeFile = new File(fileSystemLocation);

        try {
            this.url = compositeFile.getParentFile().getParentFile().toURI().toURL();
        } catch (MalformedURLException e) {
            throw new EmbeddedFabric3SetupException(String.format("Cannot get parent folder for '%1$s'.", fileSystemLocation));
        }

        Matcher m = Pattern.compile("(?:.*)/(.*)").matcher(fileSystemLocation);
        m.find();
        this.uri = URI.create(m.group(1));
        this.timestamp = System.currentTimeMillis();
        this.source = new UrlSource(url);
        this.contentType = CONTENT_TYPE_FILE;
    }

    public URI getUri() {
        return uri;
    }

    public boolean persist() {
        return false;
    }

    public boolean isExtension() {
        return false;
    }

    public Source getSource() {
        return source;
    }

    public URL getLocation() {
        return url;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getContentType() {
        return contentType;
    }

}
