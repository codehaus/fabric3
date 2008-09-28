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
package org.fabric3.spi.services.contribution;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;

import org.fabric3.host.contribution.ContributionException;
import org.fabric3.scdl.ValidationContext;

/**
 * A registry of XmlElementManifestProcessors.
 *
 * @version $Rev$ $Date$
 */
public interface XmlManifestProcessorRegistry {
    /**
     * Register a XmlElementManifestProcessor using the processor's QName type as the key
     *
     * @param processor the processor to register
     */
    void register(XmlElementManifestProcessor processor);

    /**
     * Unregister an XmlElementManifestProcessor for a QName
     *
     * @param name the QName
     */
    void unregisterProcessor(QName name);

    /**
     * Dispatches to an XmlElementManifestProcessor based on the given Qname.
     *
     * @param name     the document element type to dispatch on
     * @param manifest the manifest being processed
     * @param reader   the reader position on the document element start tag
     * @param context  the context to which validation errors and warnings are reported
     * @throws ContributionException if an error occurs during processing
     */
    void process(QName name, ContributionManifest manifest, XMLStreamReader reader, ValidationContext context) throws ContributionException;

}