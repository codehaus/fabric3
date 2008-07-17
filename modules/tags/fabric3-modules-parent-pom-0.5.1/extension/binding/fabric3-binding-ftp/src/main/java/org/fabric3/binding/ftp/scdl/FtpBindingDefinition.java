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
package org.fabric3.binding.ftp.scdl;

import java.net.URI;

import org.fabric3.binding.ftp.common.Constants;
import org.fabric3.binding.ftp.introspection.FtpBindingLoader;
import org.fabric3.scdl.BindingDefinition;

/**
 * Binding definition loaded from the SCDL.
 * 
 * @version $Revision$ $Date$
 */
public class FtpBindingDefinition extends BindingDefinition {

    private static final long serialVersionUID = -889044951554792780L;
    
    private final TransferMode transferMode;

    /**
     * Initializes the binding type.
     * 
     * @param URI Target URI.
     */
    public FtpBindingDefinition(URI uri, TransferMode transferMode) {
        super(uri, Constants.BINDING_QNAME);
        this.transferMode = transferMode;
    }

    /**
     * Gets the transfer mode.
     * 
     * @return File transfer mode.
     */
    public TransferMode getTransferMode() {
        return transferMode;
    }

}
