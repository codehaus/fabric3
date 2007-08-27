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
package org.fabric3.tx.policy;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.fabric3.spi.loader.LoaderContext;
import org.fabric3.spi.loader.LoaderException;
import org.fabric3.spi.loader.LoaderRegistry;
import org.fabric3.spi.loader.LoaderUtil;
import org.fabric3.spi.loader.StAXElementLoader;
import org.fabric3.tx.TxAction;
import org.osoa.sca.Constants;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

/**
 * XML loader for the suspend transaction policy extension.
 * 
 * @version $Revision$ $Date$
 */
@EagerInit
public class TxPolicySetExtensionLoader implements StAXElementLoader<TxPolicyExtension> {
    
    // Qualified name of the handled element
    private static final QName QNAME = new QName(Constants.SCA_NS, "transaction");
    
    /**
     * Registers with the loader registry.
     * 
     * @param registry Loader registry.
     */
    public TxPolicySetExtensionLoader(@Reference LoaderRegistry registry) {
        registry.registerLoader(QNAME, this);
    }

    /**
     * @see org.fabric3.spi.loader.StAXElementLoader#load(javax.xml.stream.XMLStreamReader, org.fabric3.spi.loader.LoaderContext)
     */
    public TxPolicyExtension load(XMLStreamReader reader, LoaderContext context)
            throws XMLStreamException, LoaderException {
        
        TxAction txAction = Enum.valueOf(TxAction.class, reader.getAttributeValue(null, "action"));
        LoaderUtil.skipToEndElement(reader);
        return new TxPolicyExtension(txAction);
        
    }

}
