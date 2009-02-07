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
package org.fabric3.fabric.domain;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.api.annotation.Monitor;
import org.fabric3.host.Namespaces;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.MetaDataStore;
import org.fabric3.spi.contribution.manifest.QNameSymbol;
import org.fabric3.spi.domain.DomainListener;
import org.fabric3.spi.xml.XMLFactory;

/**
 * Records the current domain state to a journal so it may be replayed when a controller comes back online and resyncs with the domain.
 *
 * @version $Revision$ $Date$
 */
@EagerInit
public class FSDomainTracker implements DomainListener {
    private static final String NO_PLAN = "";
    private File domainLog;
    private XMLOutputFactory outputFactory;
    private MetaDataStore store;
    private FSDomainTrackerMonitor monitor;
    private Map<QName, String> deployables;

    public FSDomainTracker(@Reference XMLFactory factory,
                           @Reference MetaDataStore store,
                           @Reference HostInfo info,
                           @Monitor FSDomainTrackerMonitor monitor) {
        this.store = store;
        this.monitor = monitor;
        this.outputFactory = factory.newOutputFactoryInstance();
        this.deployables = new HashMap<QName, String>();
        domainLog = new File(info.getDataDir(), "domain.xml");
    }

    public void onInclude(QName included, String plan) {
        // not the most efficient but it avoids poluting the DomainListener interface with a boolean for the persistent nature of a contribution
        Contribution contribution = store.resolveContainingContribution(new QNameSymbol(included));
        if (!contribution.isPersistent()) {
            // the contribution is not persistent, avoid recording it
            return;
        }
        if (plan == null) {
            plan = NO_PLAN;
        }
        deployables.put(included, plan);
        persist();
    }

    public void onUndeploy(QName undeployed) {
        deployables.remove(undeployed);
        persist();
    }

    private void persist() {
        BufferedOutputStream stream = null;
        try {
            FileOutputStream fos = new FileOutputStream(domainLog);
            stream = new BufferedOutputStream(fos);
            XMLStreamWriter writer = outputFactory.createXMLStreamWriter(stream);
            writer.writeStartDocument();
            writer.writeStartElement("domain");
            writer.writeDefaultNamespace(Namespaces.CORE);
            for (Map.Entry<QName, String> entry : deployables.entrySet()) {
                QName deployable = entry.getKey();
                String plan = entry.getValue();
                writer.writeStartElement("deployable");
                writer.writeAttribute("namespace", deployable.getNamespaceURI());
                writer.writeAttribute("name", deployable.getLocalPart());
                if (plan != NO_PLAN) {
                    writer.writeAttribute("plan", plan);
                }
                writer.writeEndElement();
            }
            writer.writeEndElement();
            writer.writeEndDocument();
        } catch (FileNotFoundException e) {
            monitor.error(e);
        } catch (XMLStreamException e) {
            monitor.error(e);
        } finally {
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (IOException e) {
                // ignore
            }
        }
    }

}
