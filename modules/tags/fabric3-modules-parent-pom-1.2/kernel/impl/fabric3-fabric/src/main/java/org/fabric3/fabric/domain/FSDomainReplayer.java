/*
* Fabric3
* Copyright (c) 2009 Metaform Systems
*
* Fabric3 is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as
* published by the Free Software Foundation, either version 3 of
* the License, or (at your option) any later version, with the
* following exception:
*
* Linking this software statically or dynamically with other
* modules is making a combined work based on this software.
* Thus, the terms and conditions of the GNU General Public
* License cover the whole combination.
*
* As a special exception, the copyright holders of this software
* give you permission to link this software with independent
* modules to produce an executable, regardless of the license
* terms of these independent modules, and to copy and distribute
* the resulting executable under terms of your choice, provided
* that you also meet, for each linked independent module, the
* terms and conditions of the license of that module. An
* independent module is a module which is not derived from or
* based on this software. If you modify this software, you may
* extend this exception to your version of the software, but
* you are not obligated to do so. If you do not wish to do so,
* delete this exception statement from your version.
*
* Fabric3 is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty
* of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
* See the GNU General Public License for more details.
*
* You should have received a copy of the
* GNU General Public License along with Fabric3.
* If not, see <http://www.gnu.org/licenses/>.
*/
package org.fabric3.fabric.domain;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
import org.fabric3.host.RuntimeMode;
import org.fabric3.host.domain.DeploymentException;
import org.fabric3.host.domain.Domain;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.spi.allocator.NoZonesAvailableException;
import org.fabric3.spi.event.DomainRecover;
import org.fabric3.spi.event.EventService;
import org.fabric3.spi.event.Fabric3EventListener;
import org.fabric3.spi.xml.XMLFactory;

/**
 * Replays the domain journal when the controller synchronizes with the domain. The domain journal records the state of the domain as composites are
 * included and undeployed. Replaying the journal has the affect of reinstating the logical assembly to its prior state before the controller went
 * offline (either as a result of a normal shutdown or system failure). Replaying the journal will also send physical change sets to all zones in the
 * domain. If a zone is up-to-date, it will discard the change set. Otherwise it will execute its commands, resulting in component reprovisioning.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class FSDomainReplayer implements Fabric3EventListener<DomainRecover> {
    private static final QName DEPLOYABLE = new QName(Namespaces.CORE, "deployable");
    private EventService eventService;
    private FSDomainReplayMonitor monitor;
    private XMLInputFactory inputFactory;
    private File domainLog;
    private Domain domain;
    private boolean federated;

    public FSDomainReplayer(@Reference(name = "domain") Domain domain,
                            @Reference XMLFactory xmlFactory,
                            @Reference HostInfo hostInfo,
                            @Reference EventService eventService,
                            @Monitor FSDomainReplayMonitor monitor) {
        this.domain = domain;
        this.eventService = eventService;
        this.monitor = monitor;
        this.inputFactory = xmlFactory.newInputFactoryInstance();
        this.federated = hostInfo.getRuntimeMode() == RuntimeMode.CONTROLLER;
        domainLog = new File(hostInfo.getDataDir(), "domain.xml");
    }

    @Init
    public void init() {
        eventService.subscribe(DomainRecover.class, this);
    }

    public void onEvent(DomainRecover event) {
        if (!domainLog.exists()) {
            return;
        }

        try {
            List<Entry> entries = parse();
            // replay the deployments
            List<QName> deployables = new ArrayList<QName>();
            List<String> plans;
            if (federated) {
                // the runtime is running in federated mode, use deployment plans
                plans = new ArrayList<String>();
                for (Entry entry : entries) {
                    deployables.add(entry.getDeployable());
                    plans.add(entry.getPlanName());
                }
            } else {
                // the runtime is running in single-VM mode, skip deployment plans
                plans = Collections.emptyList();
                for (Entry entry : entries) {
                    deployables.add(entry.getDeployable());
                }
            }
            int i = 0;
            while (i < 11) {
                try {
                    i++;
                    domain.recover(deployables, plans);
                    return;
                } catch (DeploymentException e) {
                    if (e.getCause() instanceof NoZonesAvailableException) {
                        // the zones may not have been discovered, retry
                        try {
                            monitor.status("No zones found in domain. Waiting...");
                            Thread.sleep(2000);
                            continue;
                        } catch (InterruptedException ex) {
                            // ignore, assuming this thread is not reused
                        }
                    }
                    throw e;
                }
            }
        } catch (FileNotFoundException e) {
            monitor.error(e);
        } catch (XMLStreamException e) {
            monitor.error(e);
        } catch (DeploymentException e) {
            monitor.error(e);
        }
    }

    /**
     * Reads the domain journal.
     *
     * @return th list of journal entries
     * @throws FileNotFoundException if the journal file does not exist
     * @throws XMLStreamException    if there is an error reading the journal
     */
    private List<Entry> parse() throws FileNotFoundException, XMLStreamException {
        FileInputStream fis = new FileInputStream(domainLog);
        BufferedInputStream stream = new BufferedInputStream(fis);
        XMLStreamReader reader = inputFactory.createXMLStreamReader(stream);
        List<Entry> deployables = new ArrayList<Entry>();
        try {
            while (true) {
                switch (reader.next()) {
                case XMLStreamConstants.START_ELEMENT:
                    if (DEPLOYABLE.equals(reader.getName())) {
                        String namespace = reader.getAttributeValue(null, "namespace");
                        if (namespace == null) {
                            Location location = reader.getLocation();
                            int line = location.getLineNumber();
                            int col = location.getColumnNumber();
                            monitor.errorMessage("Namespace attribute missing in domain journal [" + line + "," + col + "]");
                            continue;
                        }
                        String name = reader.getAttributeValue(null, "name");
                        if (name == null) {
                            Location location = reader.getLocation();
                            int line = location.getLineNumber();
                            int col = location.getColumnNumber();
                            monitor.errorMessage("Name attribute missing in domain journal [" + line + "," + col + "]");
                            continue;
                        }
                        String plan = reader.getAttributeValue(null, "plan");
                        QName qName = new QName(namespace, name);
                        Entry entry = new Entry(qName, plan);
                        deployables.add(entry);
                    }
                    break;
                case XMLStreamConstants.END_DOCUMENT:
                    return deployables;
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

    private class Entry {
        private QName deployable;
        private String planName;

        private Entry(QName deployable, String planName) {
            this.deployable = deployable;
            this.planName = planName;
        }

        public QName getDeployable() {
            return deployable;
        }

        public String getPlanName() {
            return planName;
        }
    }
}