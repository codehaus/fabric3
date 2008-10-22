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
package org.fabric3.spi;

/**
 * @version $Rev$ $Date$
 */
@Deprecated
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
