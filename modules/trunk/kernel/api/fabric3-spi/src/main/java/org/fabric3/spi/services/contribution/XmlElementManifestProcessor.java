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

import org.fabric3.host.contribution.InstallException;
import org.fabric3.model.type.ValidationContext;

/**
 * Processes an XML-based entry that contains manifest information. Invoked when a contribution is introspected and an XML content type is encountered
 * with a document element type corresponding to the one handled by the processor implementation.
 *
 * @version $Rev$ $Date$
 */
public interface XmlElementManifestProcessor {

    /**
     * Returns the QName for the type of XML entry handled by this processor
     *
     * @return the QName
     */
    QName getType();

    /**
     * Processes the XML contribution
     *
     * @param manifest the contribution manifest to update
     * @param reader   the reader positioned at the first element of the document
     * @param context  the context to which validation errors and warnings are reported
     * @throws InstallException if an error occurs processing
     */
    void process(ContributionManifest manifest, XMLStreamReader reader, ValidationContext context) throws InstallException;
}