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
package org.fabric3.spi.services.contribution.archive;

import java.io.IOException;
import java.net.URL;
import java.util.List;

/**
 * A registry of classpath processors
 *
 * @version $Rev$ $Date$
 */
public interface ClasspathProcessorRegistry {

    /**
     * Registers the processor
     *
     * @param processor the processor
     */
    void register(ClasspathProcessor processor);

    /**
     * De-registers the processor
     *
     * @param processor the processor
     */
    void unregister(ClasspathProcessor processor);

    /**
     * Processes the given url
     *
     * @param url the url to process
     * @return the classpath
     * @throws IOException if an error occurs processing the url
     */
    List<URL> process(URL url) throws IOException;
}
