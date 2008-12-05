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
package org.fabric3.maven.contribution;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.annotations.Reference;

import org.fabric3.spi.services.contribution.Constants;
import org.fabric3.host.contribution.ContributionNotFoundException;
import org.fabric3.maven.CompositeQNameService;
import org.fabric3.maven.InvalidResourceException;
import org.fabric3.model.type.component.Composite;
import org.fabric3.spi.services.contribution.Contribution;
import org.fabric3.spi.services.contribution.MetaDataStore;
import org.fabric3.spi.services.contribution.manifest.QNameSymbol;
import org.fabric3.spi.services.contribution.Resource;
import org.fabric3.spi.services.contribution.ResourceElement;
import org.fabric3.spi.services.contribution.Symbol;
import org.fabric3.spi.xml.XMLFactory;

/**
 * @version $Revision$ $Date$
 */
public class CompositeQNameServiceImpl implements CompositeQNameService {
    private MetaDataStore store;
    private XMLInputFactory xmlFactory;

    public CompositeQNameServiceImpl(@Reference MetaDataStore store, @Reference XMLFactory factory) {
        this.store = store;
        this.xmlFactory = factory.newInputFactoryInstance();
    }

    public QName getQName(URI uri, URL url) throws ContributionNotFoundException, InvalidResourceException {
        Contribution contribution = store.find(uri);
        if (contribution == null) {
            throw new ContributionNotFoundException("Contribution not found: " + uri);
        }
        XMLStreamReader reader = null;
        InputStream stream = null;
        try {
            stream = url.openStream();
            reader = xmlFactory.createXMLStreamReader(stream);
            reader.nextTag();
            String name = reader.getAttributeValue(null, "name");
            if (name == null) {
                throw new InvalidResourceException("Composite name not specified in : " + url);
            }
            Resource resource = new Resource(url, Constants.COMPOSITE_CONTENT_TYPE);
            String targetNamespace = reader.getAttributeValue(null, "targetNamespace");
            QName compositeName = new QName(targetNamespace, name);
            QNameSymbol symbol = new QNameSymbol(compositeName);
            ResourceElement<QNameSymbol, Composite> element = new ResourceElement<QNameSymbol, Composite>(symbol);
            resource.addResourceElement(element);
            contribution.addResource(resource);
        } catch (XMLStreamException e) {
            throw new InvalidResourceException("Error reading " + url, e);
        } catch (IOException e) {
            throw new InvalidResourceException("Error reading " + url, e);
        } finally {
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (XMLStreamException e) {
                e.printStackTrace();
            }
        }


        for (Resource resource : contribution.getResources()) {
            if (url.equals(resource.getUrl())) {
                if (resource.getResourceElements().size() != 1) {
                    throw new InvalidResourceException("Resource must contain one resource element");
                }
                ResourceElement<?, ?> element = resource.getResourceElements().get(0);
                Symbol symbol = element.getSymbol();
                if (symbol instanceof QNameSymbol) {
                    return ((QNameSymbol) symbol).getKey();
                } else {
                    throw new InvalidResourceException("Resource symbol is not of expected type:" + symbol);
                }
            }
        }
        return null;
    }
}
