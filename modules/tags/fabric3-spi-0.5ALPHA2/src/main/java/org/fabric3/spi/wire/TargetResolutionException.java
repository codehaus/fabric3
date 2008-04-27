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
package org.fabric3.spi.wire;

import org.fabric3.spi.assembly.ActivateException;

/**
 * Any exception raised during resolving reference targets.
 *
 * @version $Rev: 1567 $ $Date: 2007-10-20 11:34:49 +0100 (Sat, 20 Oct 2007) $
 */
public class TargetResolutionException extends ActivateException {

    private static final long serialVersionUID = -5262124031513496306L;

    /**
     * Initializes the exception message.
     *
     * @param message Exception message.
     */
    public TargetResolutionException(String message) {
        super(message);
    }


}
