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
package org.fabric3.spi.contribution.archive;

import java.io.IOException;
import java.net.URL;
import java.util.List;

/**
 * Constructs a classpath based on the contents of an archive. Implementations introspect archives and place any
 * required artifacts on the classpath definition. For example, a jar processor may place libraries found in
 * /META-INF/lib on the classpath.
 *
 * @version $Rev$ $Date$
 */
public interface ClasspathProcessor {

    /**
     * Returns true if the processor can introspect the given archive
     *
     * @param url the location of the archive
     * @return true if the processor can introspect the archive
     */
    public boolean canProcess(URL url);

    /**
     * Constructs the classpath by introspecting the archive
     *
     * @param url the location of the archive
     * @return the classpath
     * @throws IOException if an error occurs during introspection
     */
    public List<URL> process(URL url) throws IOException;

}
