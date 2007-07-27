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
package org.fabric3.spi.services.contribution;

import java.net.URI;
import javax.xml.namespace.QName;

import org.fabric3.scdl.ModelObject;

/**
 * Implementations store contribution metadata
 *
 * @version $Rev$ $Date$
 */
public interface MetaDataStore {
    String DEFAULT_STORE = "DefaultStore";

    /**
     * Returns the store id.
     *
     * @return the store id
     */
    String getId();

    /**
     * Stores the contribution metadata
     *
     * @param contribution the contribution metadata
     * @throws MetaDataStoreException if an error storing the metadata occurs
     */
    void store(Contribution contribution) throws MetaDataStoreException;

    /**
     * Returns the contribution for the given URI
     *
     * @param contributionUri the contribution URI
     * @return the contribution for the given URI or null if not found
     */
    Contribution find(URI contributionUri);

    /**
     * Looks up a model object qname.
     * 
     * @param deployable Qualified name of the model object.
     * @return Model object.
     */
    ModelObject resolve(QName deployable);

    Contribution resolve(Import imprt);

}
