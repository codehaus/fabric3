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
