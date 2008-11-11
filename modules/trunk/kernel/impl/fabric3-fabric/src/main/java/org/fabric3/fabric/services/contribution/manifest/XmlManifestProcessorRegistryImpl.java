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
package org.fabric3.fabric.services.contribution.manifest;

import java.util.HashMap;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;

import org.fabric3.host.contribution.InstallException;
import org.fabric3.scdl.ValidationContext;
import org.fabric3.spi.services.contribution.ContributionManifest;
import org.fabric3.spi.services.contribution.XmlElementManifestProcessor;
import org.fabric3.spi.services.contribution.XmlManifestProcessorRegistry;

/**
 * Default implementation of XmlManifestProcessorRegistry.
 *
 * @version $Rev$ $Date$
 */
public class XmlManifestProcessorRegistryImpl implements XmlManifestProcessorRegistry {

    private Map<QName, XmlElementManifestProcessor> cache = new HashMap<QName, XmlElementManifestProcessor>();

    public void register(XmlElementManifestProcessor processor) {
        cache.put(processor.getType(), processor);
    }

    public void unregisterProcessor(QName name) {
        cache.remove(name);
    }

    public void process(QName name, ContributionManifest manifest, XMLStreamReader reader, ValidationContext context) throws InstallException {
        XmlElementManifestProcessor processor = cache.get(name);
        if (processor == null) {
            return;
        }
        processor.process(manifest, reader, context);
    }
}
