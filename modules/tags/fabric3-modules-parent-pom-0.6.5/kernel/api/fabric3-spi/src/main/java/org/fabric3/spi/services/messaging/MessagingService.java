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
package org.fabric3.spi.services.messaging;

import java.net.URI;
import javax.xml.stream.XMLStreamReader;

/**
 * Defines the abstraction allowing runtimes to exchange arbitrary messages with each other.
 *
 * @version $Revision$ $Date$
 */
public interface MessagingService {

    /**
     * Returns the messaging scheme handled by this service.
     *
     * @return the messaging scheme handled by this service
     */
    String getScheme();

    /**
     * Sends a message to the specified runtime. The method returns a unique message id for the sent message. The
     * consumers can use the message id for correlating responses to sent messages.
     *
     * @param runtimeId Runtime id of recipient.
     * @param content   Message content.
     * @throws MessagingException In case of discovery errors.
     */
    void sendMessage(URI runtimeId, XMLStreamReader content) throws MessagingException;

}
