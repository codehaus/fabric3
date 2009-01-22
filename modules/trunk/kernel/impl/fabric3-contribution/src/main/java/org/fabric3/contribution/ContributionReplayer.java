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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.api.annotation.Monitor;
import org.fabric3.host.Namespaces;
import org.fabric3.host.contribution.ContributionException;
import org.fabric3.host.contribution.ContributionService;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.spi.xml.XMLFactory;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.ContributionState;
import org.fabric3.spi.contribution.MetaDataStore;
import org.fabric3.spi.services.event.EventService;
import org.fabric3.spi.services.event.Fabric3EventListener;
import org.fabric3.spi.services.event.RuntimeRecover;

/**
 * Used when a runtime is initialized to restore the state of contributions recorded by the ContributionTracker.
 *
 * @version $Revision$ $Date$
 */
@EagerInit
public class ContributionReplayer implements Fabric3EventListener<RuntimeRecover> {
    private static final QName CONTRIBUTION = new QName(Namespaces.CORE, "contribution");
    private ContributionService contributionService;
    private MetaDataStore store;
    private EventService eventService;
    private ContributionReplayMonitor monitor;
    private XMLInputFactory inputFactory;
    private File repositoryIndex;

    public ContributionReplayer(@Reference ContributionService contributionService,
                                @Reference MetaDataStore store,
                                @Reference XMLFactory xmlFactory,
                                @Reference HostInfo hostInfo,
                                @Reference EventService eventService,
                                @Monitor ContributionReplayMonitor monitor) {
        this.contributionService = contributionService;
        this.store = store;
        this.eventService = eventService;
        this.monitor = monitor;
        this.inputFactory = xmlFactory.newInputFactoryInstance();
        File repository = new File(hostInfo.getBaseDir(), "repository");
        repositoryIndex = new File(repository, "f3.xml");
    }

    @Init
    public void init() {
        eventService.subscribe(RuntimeRecover.class, this);
    }

    public void onEvent(RuntimeRecover event) {
        if (!repositoryIndex.exists()) {
            return;
        }

        try {
            // read the repository metadata file
            Map<ContributionState, List<Contribution>> contributions = parse();
            List<Contribution> stored = contributions.get(ContributionState.STORED);
            List<Contribution> installed = contributions.get(ContributionState.INSTALLED);

            // store contributions in the metadata store, bypassing the ContributionService since the archives are already in the repository
            for (Contribution contribution : stored) {
                store.store(contribution);
            }
            for (Contribution contribution : installed) {
                store.store(contribution);
            }

            // install contributions that were previously in the INSTALLED state
            List<URI> installedURIs = new ArrayList<URI>(installed.size());
            for (Contribution contribution : installed) {
                installedURIs.add(contribution.getUri());
            }
            contributionService.install(installedURIs);
        } catch (ContributionException e) {
            monitor.error(e);
        } catch (FileNotFoundException e) {
            monitor.error(e);
        } catch (XMLStreamException e) {
            monitor.error(e);
        }
    }

    private Map<ContributionState, List<Contribution>> parse() throws FileNotFoundException, XMLStreamException, InvalidRepositoryIndexException {
        FileInputStream fis = new FileInputStream(repositoryIndex);
        BufferedInputStream stream = new BufferedInputStream(fis);
        XMLStreamReader reader = inputFactory.createXMLStreamReader(stream);
        List<Contribution> stored = new ArrayList<Contribution>();
        List<Contribution> installed = new ArrayList<Contribution>();
        Map<ContributionState, List<Contribution>> contributions = new HashMap<ContributionState, List<Contribution>>();
        contributions.put(ContributionState.STORED, stored);
        contributions.put(ContributionState.INSTALLED, installed);
        try {
            while (true) {
                switch (reader.next()) {
                case XMLStreamConstants.START_ELEMENT:
                    if (CONTRIBUTION.equals(reader.getName())) {
                        String uriStr = reader.getAttributeValue(null, "uri");
                        if (uriStr == null) {
                            throw createException("URI attribute missing ", reader, null);
                        }
                        URI uri = URI.create(uriStr);
                        String locationStr = reader.getAttributeValue(null, "location");
                        if (locationStr == null) {
                            throw createException("Location attribute missing ", reader, null);
                        }
                        URL location;
                        try {
                            location = new URL(locationStr);
                        } catch (MalformedURLException e) {
                            throw createException("Invalid location attribute", reader, e);
                        }
                        String timeStampStr = reader.getAttributeValue(null, "timestamp");
                        if (timeStampStr == null) {
                            throw createException("Timestamp attribute missing ", reader, null);
                        }
                        long timestamp;
                        try {
                            timestamp = Long.parseLong(timeStampStr);
                        } catch (NumberFormatException e) {
                            throw createException("Invalid timestamp", reader, e);
                        }
                        String checksumStr = reader.getAttributeValue(null, "checksum");
                        if (checksumStr == null) {
                            throw createException("Checksum attribute missing ", reader, null);
                        }
                        byte[] checksum = checksumStr.getBytes();
                        String contentType = reader.getAttributeValue(null, "contentType");
                        if (contentType == null) {
                            throw createException("ContentType attribute missing ", reader, null);
                        }
                        String stateStr = reader.getAttributeValue(null, "state");
                        if (stateStr == null) {
                            throw createException("State attribute missing ", reader, null);
                        }
                        ContributionState state;
                        try {
                            state = ContributionState.valueOf(stateStr.toUpperCase());
                        } catch (IllegalArgumentException e) {
                            throw createException("Invalid state", reader, e);
                        }

                        Contribution contribution = new Contribution(uri, location, checksum, timestamp, contentType, true);
                        contribution.setState(state);
                        if (ContributionState.STORED == state) {
                            stored.add(contribution);
                        } else {
                            installed.add(contribution);
                        }
                    }
                    break;
                case XMLStreamConstants.END_DOCUMENT:
                    return contributions;
                }

            }
        } finally {
            try {
                stream.close();
            } catch (IOException e) {
                // ignore
            }
        }

    }

    private InvalidRepositoryIndexException createException(String message, XMLStreamReader reader, Exception e)
            throws InvalidRepositoryIndexException {
        Location location = reader.getLocation();
        String msg = message + "[" + location.getLineNumber() + "," + location.getColumnNumber() + "]";
        if (e == null) {
            return new InvalidRepositoryIndexException(msg);
        } else {
            return new InvalidRepositoryIndexException(msg, e);
        }
    }

}