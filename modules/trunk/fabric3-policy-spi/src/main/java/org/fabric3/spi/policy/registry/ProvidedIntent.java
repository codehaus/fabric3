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
package org.fabric3.spi.policy.registry;

import org.fabric3.scdl.definitions.Intent;

/**
 * Represents an intent provided by the implementation/binding type.
 * 
 * @version $Revision$ $Date$
 */
public final class ProvidedIntent {
    
    // Intent that is provided by the binding/implementation type
    private final Intent intent;
    
    // Whether the intent is always provided by the binding/implementation type
    private final boolean alwaysProvided;

    /**
     * @param intent Intent that is provided by the binding/implementation type.
     * @param alwaysProvided True if the intent is always provided by the binding/implementation type.
     */
    public ProvidedIntent(Intent intent, boolean alwaysProvided) {
        this.intent = intent;
        this.alwaysProvided = alwaysProvided;
    }

    /**
     * @return Intent that is provided by the binding/implementation type.
     */
    public final Intent getIntent() {
        return intent;
    }

    /**
     * @return True if the intent is always provided by the binding/implementation type.
     */
    public final boolean isAlwaysProvided() {
        return alwaysProvided;
    }

}
