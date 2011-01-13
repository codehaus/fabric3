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
        if (compositePath.endsWith(".composite")) {
            throw new MalformedURLException(String.format("Don't specify composite file in embedded composite. Give parent folder instead."));
        }
        if(compositePath.endsWith("WEB-INF/") || compositePath.endsWith("WEB-INF")) {
            throw new MalformedURLException("IF you want add a war folder, you have to specify WEB-INF parent folder.");
        }

        File compositeFile = new File(compositePath);

        if (!compositeFile.exists()) {
            // if file doesn't exists try to find it on classpath
            compositeFile = new File(FileSystem.fileAtClassPath(compositePath).toURI());
        }

        if (!FileSystem.isAbsolute(compositePath) || !FileSystem.exists(compositeFile)) {
            throw new EmbeddedFabric3SetupException(String.format("Composite file path '%1$s' have to be absolute or doesn't exists.", compositePath));
        }

        try {
            this.url = compositeFile.toURI().toURL();
        } catch (MalformedURLException e) {
            throw new EmbeddedFabric3SetupException(String.format("Cannot get parent folder for '%1$s'.", compositePath));
        }

        String[] name = compositePath.split("/");
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
