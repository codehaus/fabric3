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
package org.fabric3.loader.definitions;

import java.util.Set;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.xml.InvalidPrefixException;
import org.fabric3.introspection.xml.LoaderHelper;
import org.fabric3.introspection.xml.LoaderUtil;
import org.fabric3.introspection.xml.TypeLoader;
import org.fabric3.loader.impl.InvalidQNamePrefix;
import org.fabric3.scdl.definitions.BindingType;

/**
 * Loader for definitions.
 *
 * @version $Revision$ $Date$
 */
@EagerInit
public class BindingTypeLoader implements TypeLoader<BindingType> {

    private final LoaderHelper helper;

    public BindingTypeLoader(@Reference LoaderHelper helper) {
        this.helper = helper;
    }

    public BindingType load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {


        String name = reader.getAttributeValue(null, "name");
        QName qName = new QName(context.getTargetNamespace(), name);

        Set<QName> alwaysProvides;
        try {
            alwaysProvides = helper.parseListOfQNames(reader, "alwaysProvides");
            Set<QName> mayProvide = helper.parseListOfQNames(reader, "mayProvide");
            LoaderUtil.skipToEndElement(reader);
            return new BindingType(qName, alwaysProvides, mayProvide);
        } catch (InvalidPrefixException e) {
            context.addError(new InvalidQNamePrefix(e.getPrefix(), reader));
        }
        return null;


    }

}
