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
import java.util.Random;

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
        if (!compositePath.startsWith(PREFIX)) {
            throw new MalformedURLException(String.format("Composite path have to start with keyword '%1$s' followed by absolute package and composite name. E.g.: %1$s:/org.example/database.composite", PREFIX));
        }

        embeddedComposite(compositePath.replaceFirst(PREFIX, ""));
    }

    private void embeddedComposite(final String classPathLocation) throws MalformedURLException, URISyntaxException {
        File compositeFile = new File(classPathLocation);

        if (!compositeFile.exists()) {
            // if file doesn't exists try to find it on classpath
            compositeFile = new File(FileSystem.fileAtClassPath(classPathLocation).toURI());
        }

        if (!FileSystem.isAbsolute(classPathLocation) || !FileSystem.exists(compositeFile)) {
            throw new EmbeddedFabric3SetupException(String.format("Composite file path '%1$s' have to be absolute or doesn't exists.", classPathLocation));
        }

        try {
            this.url = compositeFile.toURI().toURL();
        } catch (MalformedURLException e) {
            throw new EmbeddedFabric3SetupException(String.format("Cannot get parent folder for '%1$s'.", classPathLocation));
        }

        String[] name = classPathLocation.split("/");
        // TODO search for existing composite name
        this.uri = URI.create(name[name.length - 1] + new Random().nextInt());
        this.timestamp = System.currentTimeMillis();
        this.source = new UrlSource(url);
        this.contentType = CONTENT_TYPE;
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
