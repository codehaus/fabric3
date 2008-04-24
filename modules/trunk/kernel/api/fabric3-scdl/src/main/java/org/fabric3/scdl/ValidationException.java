/*
 * See the NOTICE file distributed with this work for information
 * regarding copyright ownership.  This file is licensed
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

import java.util.List;

/**
 * @version $Rev$ $Date$
 */
public abstract class ValidationException extends Exception {

    private final List<ValidationException> causes;

    /**
     * Default constructor that initializes a null list of causes.
     */
    protected ValidationException() {
        causes = null;
    }

    /**
     * Constructor that initializes the initial list of causes.
     *
     * @param causes the underlying causes of this exception
     */
    protected ValidationException(List<ValidationException> causes) {
        this.causes = causes;
    }

    /**
     * Returns a collection of underlying causes of this exception.
     *
     * @return a collection of underlying causes; may be null
     */
    public List<ValidationException> getCauses() {
        return causes;
    }
}
