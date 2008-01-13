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
package org.fabric3.loader.common;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.fabric3.scdl.OperationDefinition;
import org.fabric3.spi.loader.InvalidValueException;
import org.fabric3.spi.loader.LoaderContext;
import org.fabric3.spi.loader.LoaderException;
import org.fabric3.spi.loader.LoaderRegistry;
import org.fabric3.spi.loader.LoaderUtil;
import org.fabric3.spi.loader.PolicyHelper;
import org.fabric3.spi.loader.StAXElementLoader;
import org.osoa.sca.Constants;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

/**
 * Loads an operation definition from the SCDL.
 *
 * @version $Rev: 1980 $ $Date: 2007-11-13 17:31:55 +0000 (Tue, 13 Nov 2007) $
 */
@EagerInit
public class OperationLoader implements StAXElementLoader<OperationDefinition> {

    private final PolicyHelper policyHelper;
    private final LoaderRegistry loaderRegistry;

    public OperationLoader(@Reference PolicyHelper policyHelper, @Reference LoaderRegistry loaderRegistry) {
        this.policyHelper = policyHelper;
        this.loaderRegistry = loaderRegistry;
    }
    
    @Init
    public void start() {
        loaderRegistry.registerLoader(new QName(Constants.SCA_NS, "operation"), this);
    }

    public OperationDefinition load(XMLStreamReader reader, LoaderContext context) throws  LoaderException, XMLStreamException {
        
        String name = reader.getAttributeValue(null, "name");
        if (name == null) {
            InvalidValueException e = new InvalidValueException("operation name not supplied");
            e.setResourceURI(context.getSourceBase().toString());
            throw e;
        }
        
        OperationDefinition operationDefinition = new OperationDefinition();
        operationDefinition.setName(name);
        
        policyHelper.loadPolicySetsAndIntents(operationDefinition, reader);

        LoaderUtil.skipToEndElement(reader);
        
        return operationDefinition;
        
    }
    
}
