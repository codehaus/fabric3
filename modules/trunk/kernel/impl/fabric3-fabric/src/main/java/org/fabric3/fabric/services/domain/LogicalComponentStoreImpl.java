/*
 * See the NOTICE file distributed with this work for information
 * regarding copyright ownership.  This file is licensed
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.fabric3.fabric.services.domain;

import static java.io.File.separator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.fabric3.fabric.util.FileHelper;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.scdl.ComponentDefinition;
import org.fabric3.scdl.Composite;
import org.fabric3.scdl.CompositeImplementation;
import org.fabric3.spi.services.marshaller.MarshalException;
import org.fabric3.spi.services.marshaller.MarshalService;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.runtime.assembly.LogicalComponentStore;
import org.fabric3.spi.runtime.assembly.RecordException;
import org.fabric3.spi.runtime.assembly.RecoveryException;
import org.fabric3.spi.services.factories.xml.XMLFactory;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;
import org.osoa.sca.annotations.Service;

/**
 * Default implementation of the LogicalComponentStore that persists the logical domain model to disk. The
 * implementation serializes the domain model using XStream.
 *
 * @version $Rev$ $Date$
 */
@Service(LogicalComponentStore.class)
@EagerInit
public class LogicalComponentStoreImpl implements LogicalComponentStore {
    private File serializedFile;
    private URI domainUri;
    private MarshalService marshalService;
    private XMLInputFactory inputFactory;
    private XMLOutputFactory outputFactory;

    public LogicalComponentStoreImpl(@Reference HostInfo hostInfo,
                                     @Reference MarshalService marshalService,
                                     @Reference XMLFactory factory) throws IOException {
        this.marshalService = marshalService;
        outputFactory = factory.newOutputFactoryInstance();
        inputFactory = factory.newInputFactoryInstance();
        domainUri = hostInfo.getDomain();
        URL url = hostInfo.getBaseURL();
        if (url == null) {
            throw new FileNotFoundException("No base directory found");
        }
        String pathname = url.getFile();
        File baseDir = new File(pathname);

        File root = new File(baseDir, "stores" + separator + "assembly");
        FileHelper.forceMkdir(root);
        if (!root.exists() || !root.isDirectory() || !root.canRead()) {
            throw new IOException("The location is not a directory: " + root.getCanonicalPath());
        }
        serializedFile = new File(root, "assembly.ser");
    }

    public void store(LogicalCompositeComponent domain) throws RecordException {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(serializedFile);
            XMLStreamWriter writer = outputFactory.createXMLStreamWriter(fos);
            marshalService.marshall(domain, writer);
        } catch (FileNotFoundException e) {
            throw new RecordException("Error serializing assembly", e);
        } catch (MarshalException e) {
            throw new RecordException("Error serializing assembly", e);
        } catch (XMLStreamException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    // TODO log exception
                    e.printStackTrace();
                }
            }
        }
    }

    @SuppressWarnings({"unchecked"})
    public LogicalCompositeComponent read() throws RecoveryException {
        if (!serializedFile.exists()) {
            // no serialized file, create a new domain
            Composite type = new Composite(null);
            CompositeImplementation impl = new CompositeImplementation();
            impl.setComponentType(type);
            ComponentDefinition<CompositeImplementation> definition =
                    new ComponentDefinition<CompositeImplementation>(domainUri.toString());
            definition.setImplementation(impl);
            return new LogicalCompositeComponent(domainUri, domainUri, definition, null);
        }
        FileInputStream fin = null;
        try {
            fin = new FileInputStream(serializedFile);
            return marshalService.unmarshall(LogicalCompositeComponent.class, inputFactory.createXMLStreamReader(fin));
        } catch (FileNotFoundException e) {
            // should not happen
            throw new AssertionError();
        } catch (MarshalException e) {
            throw new RecoveryException("Error recovering", e);
        } catch (XMLStreamException e) {
            throw new RecoveryException("Error recovering", e);
        } finally {
            if (fin != null) {
                try {
                    fin.close();
                } catch (IOException e) {
                    // TODO log exception
                    e.printStackTrace();
                }
            }
        }

    }

}
