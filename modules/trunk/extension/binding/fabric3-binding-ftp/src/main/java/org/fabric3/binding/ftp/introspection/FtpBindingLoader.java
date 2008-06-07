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
package org.fabric3.binding.ftp.introspection;

import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.fabric3.binding.ftp.scdl.FtpBindingDefinition;
import org.fabric3.binding.ftp.scdl.TransferMode;
import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.xml.InvalidValue;
import org.fabric3.introspection.xml.LoaderHelper;
import org.fabric3.introspection.xml.LoaderUtil;
import org.fabric3.introspection.xml.MissingAttribute;
import org.fabric3.introspection.xml.TypeLoader;
import org.osoa.sca.annotations.Reference;

/**
 *
 * @version $Revision$ $Date$
 */
public class FtpBindingLoader implements TypeLoader<FtpBindingDefinition> {

    private final LoaderHelper loaderHelper;

    /**
     * Constructor.
     *
     * @param loaderHelper the policy helper
     */
    public FtpBindingLoader(@Reference LoaderHelper loaderHelper) {
        this.loaderHelper = loaderHelper;
    }

    public FtpBindingDefinition load(XMLStreamReader reader, IntrospectionContext introspectionContext) throws XMLStreamException {

        FtpBindingDefinition bd = null;
        String uri = null;
        
        try {

            uri = reader.getAttributeValue(null, "uri");
            String transferMode = reader.getAttributeValue(null, "mode");
            if (uri == null) {
                MissingAttribute failure = new MissingAttribute("A binding URI must be specified ", "uri", reader);
                introspectionContext.addError(failure);
                return null;
            }
            bd = new FtpBindingDefinition(new URI(uri), TransferMode.valueOf(transferMode));

            loaderHelper.loadPolicySetsAndIntents(bd, reader, introspectionContext);

        } catch (URISyntaxException ex) {
            InvalidValue failure = new InvalidValue("The FTP binding URI is not valid: " + uri, "uri", reader);
            introspectionContext.addError(failure);
        }

        LoaderUtil.skipToEndElement(reader);
        return bd;

    }
    
}
