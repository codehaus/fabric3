package org.fabric3.spi;

/**
 * @version $Rev$ $Date$
 */
public interface Constants {
    /**
     * Fabric3 base namespace.
     */
    String FABRIC3_NS = "http://fabric3.org/xmlns/sca/2.0-alpha";

    /**
     * Fabric3 base maven namespace.
     */
    String FABRIC3_MAVEN_NS = "http://fabric3.org/xmlns/sca/2.0-alpha/maven";

    /**
     * Fabric3 system namespace.
     */
    String FABRIC3_SYSTEM_NS = "http://fabric3.org/xmlns/sca/system/2.0-alpha";

    /**
     * Prefix form of the namespace that can be prepended to declarations.
     */
    String FABRIC3_PREFIX = '{' + FABRIC3_NS + '}';
}
