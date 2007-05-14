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
package org.fabric3.fabric.services.contribution.manifest;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import static org.osoa.sca.Constants.SCA_NS;
import org.osoa.sca.annotations.Reference;

import org.fabric3.extension.loader.LoaderExtension;
import org.fabric3.spi.loader.LoaderContext;
import org.fabric3.spi.loader.LoaderException;
import org.fabric3.spi.loader.LoaderRegistry;
import org.fabric3.spi.services.contribution.Deployable;
import org.fabric3.spi.util.stax.StaxUtil;

/**
 * Processes a <code>deployable</code> element in a contribution manifest
 *
 * @version $Rev$ $Date$
 */
public class DeployableLoader extends LoaderExtension<Object, Deployable> {
    private static final QName DEPLOYABLE = new QName(SCA_NS, "deployable");

    /**
     * Constructor specifies the registry to register with.
     *
     * @param registry the LoaderRegistry this loader should register with
     */
    public DeployableLoader(@Reference LoaderRegistry registry) {
        super(registry);
    }

    public QName getXMLType() {
        return DEPLOYABLE;
    }

    public Deployable load(Object contribution, XMLStreamReader reader, LoaderContext context)
            throws LoaderException, XMLStreamException {
        String name = reader.getAttributeValue(null, "composite");
        if (name == null) {
            throw new MissingAttributeException("composite");
        }
        QName compositeName = StaxUtil.createQName(name, reader);
        return new Deployable(compositeName);
    }

}
