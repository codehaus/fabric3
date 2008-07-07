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
package org.fabric3.fabric.services.contribution;

import java.net.URI;
import javax.xml.namespace.QName;

import org.fabric3.host.contribution.ValidationFailure;

/**
 * Thrown when a deployable composite specified in the sca-contribution is invalid.
 *
 * @version $Rev$ $Date$
 */
public class InvalidDeployable extends ValidationFailure<URI> {
    private String message;
    private QName deployable;

    /**
     * Constructor.
     *
     * @param message    the error message
     * @param uri        the contribution URI
     * @param deployable the deployable qualified name
     */
    public InvalidDeployable(String message, URI uri, QName deployable) {
        super(uri);
        this.message = message;
        this.deployable = deployable;
    }

    public QName getDeployable() {
        return deployable;
    }

    public void setDeployable(QName deployable) {
        this.deployable = deployable;
    }

    public String getMessage() {
        return message;
    }
}