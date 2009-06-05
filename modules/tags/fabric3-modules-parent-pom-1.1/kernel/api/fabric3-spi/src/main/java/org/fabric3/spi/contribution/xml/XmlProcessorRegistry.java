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
package org.fabric3.spi.contribution.xml;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;

import org.fabric3.host.contribution.InstallException;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.introspection.IntrospectionContext;

/**
 * A registry of XmlProcessors
 *
 * @version $Rev$ $Date$
 */
public interface XmlProcessorRegistry {
    /**
     * Register a XmlProcessor using the processor's QName type as the key
     *
     * @param processor the processor to register
     */
    void register(XmlProcessor processor);

    /**
     * Unregister an XmlProcessor for a QName
     *
     * @param name the QName
     */
    void unregister(QName name);

    /**
     * Dispatches to an XmlProcessor
     *
     * @param contribution the contribution metadata to update
     * @param reader       the reader positioned at the first element of the document
     * @param context      the context to which validation errors and warnings are reported
     * @param loader       the classloader to perform resolution in
     * @throws InstallException if an error occurs processing
     */
    void process(Contribution contribution, XMLStreamReader reader, IntrospectionContext context, ClassLoader loader) throws InstallException;

}
