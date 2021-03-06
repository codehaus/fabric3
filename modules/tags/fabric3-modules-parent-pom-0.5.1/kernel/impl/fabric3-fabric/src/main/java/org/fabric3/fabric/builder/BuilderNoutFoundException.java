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
package org.fabric3.fabric.builder;

import org.fabric3.spi.builder.BuilderException;

/**
 * Represents an error processing an assembly model
 *
 * @version $Rev$ $Date$
 */
public class BuilderNoutFoundException extends BuilderException {
    private static final long serialVersionUID = 2604579115547966762L;

    public BuilderNoutFoundException() {
        super();
    }

    public BuilderNoutFoundException(String message) {
        super(message);
    }

    public BuilderNoutFoundException(String message, String identifier) {
        super(message, identifier);
    }

    public BuilderNoutFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public BuilderNoutFoundException(Throwable cause) {
        super(cause);
    }

}
