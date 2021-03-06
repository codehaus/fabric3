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
package org.fabric3.spi.services.contribution;

import org.fabric3.host.contribution.ContributionException;

/**
 * @version $Rev$ $Date$
 */
public class MetaDataStoreException extends ContributionException {
    private static final long serialVersionUID = 5509464593295262830L;

    public MetaDataStoreException(String message) {
        super(message);
    }

    public MetaDataStoreException(String message, Throwable cause) {
        super(message, cause);
    }

    public MetaDataStoreException(String message, String identifier) {
        super(message, identifier);
    }

    public MetaDataStoreException(String message, String identifier, Throwable cause) {
        super(message, identifier, cause);
    }
}
