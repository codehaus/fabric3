/*
 * Fabric3
 * Copyright � 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the �License�), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an �as is� basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 */
package org.fabric3.fabric.services.lcm;

import java.io.File;
import static java.io.File.separator;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;
import org.osoa.sca.annotations.Service;

import org.fabric3.fabric.util.FileHelper;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.scdl.ComponentDefinition;
import org.fabric3.scdl.Composite;
import org.fabric3.scdl.CompositeImplementation;
import org.fabric3.services.xmlfactory.XMLFactory;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.services.lcm.LogicalComponentStore;
import org.fabric3.spi.services.lcm.StoreException;
import org.fabric3.spi.services.lcm.RecoveryException;
import org.fabric3.spi.services.marshaller.MarshalException;
import org.fabric3.spi.services.marshaller.MarshalService;

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
        File baseDir  = hostInfo.getBaseDir();
        if (baseDir == null) {
            throw new FileNotFoundException("No base directory found");
        }
        File root = new File(baseDir, "stores" + separator + "assembly");
        FileHelper.forceMkdir(root);
        if (!root.exists() || !root.isDirectory() || !root.canRead()) {
            throw new IOException("The location is not a directory: " + root.getCanonicalPath());
        }
        serializedFile = new File(root, "assembly.ser");
    }

    public void store(LogicalCompositeComponent domain) throws StoreException {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(serializedFile);
            XMLStreamWriter writer = outputFactory.createXMLStreamWriter(fos);
            marshalService.marshall(domain, writer);
        } catch (FileNotFoundException e) {
            throw new StoreException("Error serializing assembly", e);
        } catch (MarshalException e) {
            throw new StoreException("Error serializing assembly", e);
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
            return new LogicalCompositeComponent(domainUri, definition, null);
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
