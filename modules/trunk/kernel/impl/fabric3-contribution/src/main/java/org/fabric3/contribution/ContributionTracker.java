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
package org.fabric3.contribution;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.api.annotation.Monitor;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.spi.xml.XMLFactory;
import org.fabric3.spi.Namespaces;
import org.fabric3.spi.services.contribution.Contribution;
import org.fabric3.spi.services.contribution.ContributionServiceListener;

/**
 * Records changes to the state of persistent contributions.
 *
 * @version $Revision$ $Date$
 */
@EagerInit
public class ContributionTracker implements ContributionServiceListener {
    private Map<URI, Contribution> contributions = new ConcurrentHashMap<URI, Contribution>();
    private XMLOutputFactory outputFactory;
    private File repository;
    private File repositoryIndex;
    private ContributionTrackerMonitor monitor;

    public ContributionTracker(@Reference XMLFactory factory, @Reference HostInfo hostInfo, @Monitor ContributionTrackerMonitor monitor) {
        this.outputFactory = factory.newOutputFactoryInstance();
        this.monitor = monitor;
        repository = new File(hostInfo.getBaseDir(), "repository");
    }

    @Init
    public void init() throws IOException {
        if (!repository.exists() || !repository.isDirectory() || !repository.canRead()) {
            throw new IOException("The repository location is not a directory: " + repository);
        }
        repositoryIndex = new File(repository, "f3.xml");
    }

    public void onStore(Contribution contribution) {
        if (!contribution.isPersistent()) {
            return;
        }
        contributions.put(contribution.getUri(), contribution);
        persist();
    }

    public void onInstall(Contribution contribution) {
        update(contribution);

    }

    public void onContribute(Contribution contribution) {
        update(contribution);
    }

    public void onUpdate(Contribution contribution) {
        update(contribution);
    }

    public void onUninstall(Contribution contribution) {
        update(contribution);
    }

    public void onRemove(Contribution contribution) {
        if (!contribution.isPersistent()) {
            return;
        }
        contributions.remove(contribution.getUri());
        persist();
    }

    /**
     * Updates the repository index based on the changed contribution.
     *
     * @param contribution the changed contribution
     */
    private void update(Contribution contribution) {
        if (!contribution.isPersistent()) {
            return;
        }
        contributions.put(contribution.getUri(), contribution);
        persist();
    }

    /**
     * Writes the contribution metadata to an XML-based index file (f3.xml) in the repository.
     */
    private void persist() {
        BufferedOutputStream stream = null;
        try {
            FileOutputStream fos = new FileOutputStream(repositoryIndex);
            stream = new BufferedOutputStream(fos);
            XMLStreamWriter writer = outputFactory.createXMLStreamWriter(stream);
            writer.writeStartDocument();
            writer.writeStartElement("repository");
            writer.writeDefaultNamespace(Namespaces.CORE);
            for (Contribution contribution : contributions.values()) {
                writer.writeStartElement("contribution");
                writer.writeAttribute("uri", contribution.getUri().toString());
                writer.writeAttribute("location", contribution.getLocation().toString());
                writer.writeAttribute("timestamp", String.valueOf(contribution.getTimestamp()));
                writer.writeAttribute("checksum", new String(contribution.getChecksum()));
                writer.writeAttribute("contentType", contribution.getContentType());
                writer.writeAttribute("state", contribution.getState().toString());
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
