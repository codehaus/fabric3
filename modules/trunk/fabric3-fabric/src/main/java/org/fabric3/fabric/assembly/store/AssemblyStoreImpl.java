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
package org.fabric3.fabric.assembly.store;

import java.io.File;
import static java.io.File.separator;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;

import com.thoughtworks.xstream.XStream;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;
import org.osoa.sca.annotations.Service;

import org.fabric3.fabric.services.xstream.XStreamFactory;
import org.fabric3.fabric.util.FileHelper;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.scdl.ComponentDefinition;
import org.fabric3.scdl.CompositeComponentType;
import org.fabric3.scdl.CompositeImplementation;

/**
 * Default implementation of the AssemblyStore that persists the logical domain model to disk. The implementation
 * serializes the domain model using XStream.
 *
 * @version $Rev$ $Date$
 */
@Service(AssemblyStore.class)
@EagerInit
public class AssemblyStoreImpl implements AssemblyStore {
    private XStream xstream;
    private File serializedFile;
    private URI domainUri;

    public AssemblyStoreImpl(@Reference HostInfo hostInfo,
                             @Reference XStreamFactory factory) throws IOException {
        domainUri = hostInfo.getDomain();
        xstream = factory.createInstance();
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

    public void store(LogicalComponent<CompositeImplementation> domain) throws RecordException {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(serializedFile);
            xstream.toXML(domain, fos);
        } catch (FileNotFoundException e) {
            throw new RecordException("Error serializing assembly", e);
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    //noinspection ThrowFromFinallyBlock
                    throw new RecordException("Error serializing assembly", e);
                }
            }
        }
    }

    @SuppressWarnings({"unchecked"})
    public LogicalComponent<CompositeImplementation> read() throws RecoveryException {
        if (!serializedFile.exists()) {
            // no serialized file, create a new domain
            CompositeComponentType type = new CompositeComponentType();
            CompositeImplementation impl = new CompositeImplementation();
            impl.setComponentType(type);
            ComponentDefinition<CompositeImplementation> definition =
                    new ComponentDefinition<CompositeImplementation>(domainUri.toString(), impl);
            return new LogicalComponent<CompositeImplementation>(domainUri, domainUri, definition, null);
        }
        FileInputStream fin = null;
        try {
            fin = new FileInputStream(serializedFile);
            return (LogicalComponent<CompositeImplementation>) xstream.fromXML(fin);
        } catch (FileNotFoundException e) {
            // should not happen
            throw new AssertionError();
        } finally {
            if (fin != null) {
                try {
                    fin.close();
                } catch (IOException e) {
                    //noinspection ThrowFromFinallyBlock
                    throw new RecoveryException("Error recovering assembly", e);
                }
            }
        }

    }

}
