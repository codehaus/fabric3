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
package org.fabric3.scdl;

/**
 * Denotes if autowire is on, off or inherited.
 *
 * @version $Rev$ $Date$
 */
public enum Autowire {
    ON,
    OFF,
    INHERITED;

    /**
     * Parse an autowire value.
     *
     * @param text the text to parse
     * @return INHERITED if the text is null or empty, ON if text is "true", otherwise OFF
     */
    public static Autowire fromString(String text) {
        if (text == null || text.length() == 0) {
            return INHERITED;
        } else if ("true".equalsIgnoreCase(text)) {
            return ON;
        } else {
            return OFF;
        }
    }
}
