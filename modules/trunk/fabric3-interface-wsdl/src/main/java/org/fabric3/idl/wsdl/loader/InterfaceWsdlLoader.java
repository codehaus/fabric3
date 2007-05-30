/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
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

package org.fabric3.idl.wsdl.loader;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.fabric3.extension.loader.LoaderExtension;
import org.fabric3.idl.wsdl.WsdlContract;
import org.fabric3.idl.wsdl.processor.WsdlProcessor;
import org.fabric3.spi.loader.LoaderContext;
import org.fabric3.spi.loader.LoaderException;
import org.fabric3.spi.loader.LoaderRegistry;
import org.osoa.sca.Constants;

/**
 * Loader for interface.wsdl.
 * 
 * @version $Revision$ $Date$
 */
public class InterfaceWsdlLoader extends LoaderExtension<Object, WsdlContract> implements Constants {
    
    /**
     * Interface element QName.
     */
    private static final QName QNAME = new QName(SCA_NS, "interface.wsdl");
    
    /**
     * WSDL processor.
     */
    private WsdlProcessor processor;

    /**
     * @param loaderRegistry Loader registry.
     * @param wsdlProcessor WSDL processor.
     */
    protected InterfaceWsdlLoader(LoaderRegistry loaderRegistry, WsdlProcessor processor) {
        super(loaderRegistry);
        this.processor = processor;
    }

    /**
     * @see org.fabric3.extension.loader.LoaderExtension#getXMLType()
     */
    @Override
    public QName getXMLType() {
        return QNAME;
    }

    /**
     * @see org.fabric3.spi.loader.StAXElementLoader#load(java.lang.Object, javax.xml.stream.XMLStreamReader, org.fabric3.spi.loader.LoaderContext)
     */
    @SuppressWarnings("unchecked")
    public WsdlContract load(Object input, XMLStreamReader reader, LoaderContext context) throws XMLStreamException, LoaderException {
        
        WsdlContract wsdlContract = new WsdlContract();
        
        String wsdlLocation = reader.getAttributeValue(null, "wsdlLocation");
        if(wsdlLocation == null) {
            // We don't support auto dereferecing of namespace URI
            throw new LoaderException("WSDL Location is required");
        }
        URL wsdlUrl = getWsdlUrl(wsdlLocation);
        if(wsdlUrl == null) {
            throw new LoaderException("Unable to locate WSDL " + wsdlLocation);
            
        }
        
        String interfaze = reader.getAttributeValue(null, "interface");
        if(interfaze == null) {
            throw new LoaderException("Interface is required");
        }
        QName interfaceQName = getQName(interfaze);
        wsdlContract.setQname(interfaceQName);
        wsdlContract.setOperations(processor.getOperations(interfaceQName, wsdlUrl));
        
        String callbackInterfaze = reader.getAttributeValue(null, "callbackInterface");
        if(callbackInterfaze != null) {
            QName callbackInterfaceQName = getQName(callbackInterfaze);
            wsdlContract.setCallbackQname(callbackInterfaceQName);
            wsdlContract.setCallbackOperations(processor.getOperations(callbackInterfaceQName, wsdlUrl));
        }        
        
        return wsdlContract;
        
    }

    /*
     * Returns the interface.portType qname.
     */
    private QName getQName(String interfaze) {
        throw new UnsupportedOperationException("Not supported yet");
    }
    
    /*
     * Gets the WSDL URL.
     */
    private URL getWsdlUrl(String wsdlPath) {
        
        try {
            return new URL(wsdlPath);
        } catch(MalformedURLException ex) {
            return getClass().getClassLoader().getResource(wsdlPath);
        }
    }

}
