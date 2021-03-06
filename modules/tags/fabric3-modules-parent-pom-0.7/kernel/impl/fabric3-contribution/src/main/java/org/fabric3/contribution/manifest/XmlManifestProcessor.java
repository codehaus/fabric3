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
package org.fabric3.contribution.manifest;

import java.io.InputStream;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.host.contribution.InstallException;
import org.fabric3.spi.xml.XMLFactory;
import org.fabric3.model.type.ValidationContext;
import org.fabric3.spi.contribution.ContributionManifest;
import org.fabric3.spi.contribution.ManifestProcessor;
import org.fabric3.spi.contribution.ProcessorRegistry;
import org.fabric3.spi.contribution.manifest.XmlManifestProcessorRegistry;
import org.fabric3.spi.contribution.manifest.XmlElementManifestProcessor;

/**
 * Processes XML artifacts in a contribution that contain manifest information. Dispatches to {@link XmlElementManifestProcessor}
 * based on the document element type for further processing.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class XmlManifestProcessor implements ManifestProcessor {
    private ProcessorRegistry processorRegistry;
    private XmlManifestProcessorRegistry manifestProcessorRegistry;
    private XMLInputFactory xmlFactory;

    public XmlManifestProcessor(@Reference ProcessorRegistry registry,
                                @Reference XmlManifestProcessorRegistry manifestProcessorRegistry,
                                @Reference XMLFactory xmlFactory) {
        this.processorRegistry = registry;
        this.manifestProcessorRegistry = manifestProcessorRegistry;
        this.xmlFactory = xmlFactory.newInputFactoryInstance();
    }

    @Init
    public void init() {
        processorRegistry.register(this);
    }

    public String getContentType() {
        return "application/xml";
    }

    public void process(ContributionManifest manifest, InputStream stream, ValidationContext context) throws InstallException {
        XMLStreamReader reader = null;
        try {
            reader = xmlFactory.createXMLStreamReader(stream);
            while (reader.hasNext() && XMLStreamConstants.START_ELEMENT != reader.getEventType()) {
                reader.next();
            }
            if (XMLStreamConstants.END_DOCUMENT == reader.getEventType()) {
                return;
            }
            manifestProcessorRegistry.process(reader.getName(), manifest, reader, context);
        } catch (XMLStreamException e) {
            throw new InstallException(e);
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (XMLStreamException e) {
                e.printStackTrace();
            }
        }
    }

}
