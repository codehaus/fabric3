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

import java.net.URL;
import java.net.URI;
import javax.xml.namespace.QName;

import org.osoa.sca.annotations.Reference;

import org.fabric3.maven.CompositeQNameService;
import org.fabric3.maven.InvalidResourceException;
import org.fabric3.spi.services.contribution.MetaDataStore;
import org.fabric3.spi.services.contribution.Contribution;
import org.fabric3.spi.services.contribution.Resource;
import org.fabric3.spi.services.contribution.ResourceElement;
import org.fabric3.spi.services.contribution.Symbol;
import org.fabric3.spi.services.contribution.QNameSymbol;
import org.fabric3.host.contribution.ContributionNotFoundException;

/**
 * @version $Revision$ $Date$
 */
public class CompositeQNameServiceImpl implements CompositeQNameService {
    private MetaDataStore store;

    public CompositeQNameServiceImpl(@Reference MetaDataStore store) {
        this.store = store;
    }

    public QName getQName(URI uri, URL url) throws ContributionNotFoundException, InvalidResourceException {
        Contribution contribution = store.find(uri);
        if (contribution == null) {
            throw new ContributionNotFoundException("Contribution not found: " + uri);
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
