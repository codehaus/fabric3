/*
 * Fabric3
 * Copyright (C) 2009 Metaform Systems
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
 *
 * --- Original Apache License ---
 *
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
package org.fabric3.binding.jms.model;

import java.net.URI;
import javax.xml.namespace.QName;

import org.oasisopen.sca.Constants;
import org.w3c.dom.Document;

import org.fabric3.binding.jms.common.JmsBindingMetadata;
import org.fabric3.model.type.component.BindingDefinition;

/**
 * Logical model object for JMS binding definition. TODO Support for overriding request connection, response connection and operation properties from
 * a definition document as well as activation spec and resource adaptor.
 *
 * @version $Revision$ $Date$
 */
public class JmsBindingDefinition extends BindingDefinition {
    private static final long serialVersionUID = -1888120511695824132L;

    /**
     * Qualified name for the binding element.
     */
    public static final QName BINDING_QNAME = new QName(Constants.SCA_NS, "binding.jms");

    /**
     * A generated URI overriding TargetUri in base class.
     */
    private URI generatedTargetUri;

    /**
     * JMS binding metadata shared between logical and physical.
     */
    private JmsBindingMetadata metadata;


    /**
     * Constructor.
     *
     * @param metadata Metadata to be initialized.
     * @param key      the binding key
     */
    public JmsBindingDefinition(JmsBindingMetadata metadata, Document key) {
        super(null, BINDING_QNAME, key);
        this.metadata = metadata;
        addRequiredCapability("jms");
    }

    /**
     * Constructor.
     *
     * @param targetURI URI of binding target
     * @param metadata  Metadata to be initialized.
     * @param key       the binding key
     */
    public JmsBindingDefinition(URI targetURI, JmsBindingMetadata metadata, Document key) {
        super(targetURI, BINDING_QNAME, key);
        this.metadata = metadata;
        addRequiredCapability("jms");
    }

    /**
     * @return the metadata
     */
    public JmsBindingMetadata getMetadata() {
        return metadata;
    }

    /**
     * @param metadata the metadata to set
     */
    public void setMetadata(JmsBindingMetadata metadata) {
        this.metadata = metadata;
    }

    public void setGeneratedTargetUri(URI generatedTargetUri) {
        this.generatedTargetUri = generatedTargetUri;
    }

    @Override
    public URI getTargetUri() {
        return generatedTargetUri;
    }

}
