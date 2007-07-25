package org.fabric3.runtime.development.host;

import java.net.URL;
import javax.xml.namespace.QName;

import org.fabric3.host.runtime.Fabric3Runtime;

/**
 * The contract between the Domain API and the development runtime implementation, which is loaded in a child
 * classloader of the application and Domain API classloader. This isolates runtime implementation classes from the
 * application classpath.
 *
 * @version $Rev$ $Date$
 */
public interface DevelopmentRuntime extends Fabric3Runtime<DevelopmentHostInfo> {

    /**
     * Activates a composite at the given URL.
     *
     * @param file the URL to the composite file
     */
    void activate(URL file);

    <T> T connectTo(Class<T> interfaze, String serviceUri);

    public void includeExtension(URL compositeFile);

    public void activateExtension(QName qName);

    <T> void registerMockReference(String name, Class<T> interfaze, T mock);

}
