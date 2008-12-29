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
package org.fabric3.contribution.processor;

import java.util.HashMap;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;

import org.fabric3.host.contribution.InstallException;
import org.fabric3.spi.introspection.ValidationContext;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.xml.XmlProcessor;
import org.fabric3.spi.contribution.xml.XmlProcessorRegistry;

/**
 * Default impelmentation of an XmlProcessorRegistry.
 *
 * @version $Rev$ $Date$
 */
public class XmlProcessorRegistryImpl implements XmlProcessorRegistry {
    private Map<QName, XmlProcessor> cache = new HashMap<QName, XmlProcessor>();

    public void register(XmlProcessor processor) {
        cache.put(processor.getType(), processor);
    }

    public void unregister(QName name) {
        cache.remove(name);
    }

    public void process(Contribution contribution, XMLStreamReader reader, ValidationContext context, ClassLoader loader)
            throws InstallException {
        QName name = reader.getName();
        XmlProcessor processor = cache.get(name);
        if (processor == null) {
            String id = name.toString();
            throw new XmlProcessorTypeNotFoundException("XML processor not found for: " + id, id);
        }
        processor.processContent(contribution, context, reader, loader);
    }
}