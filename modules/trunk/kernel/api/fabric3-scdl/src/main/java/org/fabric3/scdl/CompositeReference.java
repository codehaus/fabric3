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

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.fabric3.scdl.validation.MissingPromotionException;

/**
 * @version $Rev$ $Date$
 */
public class CompositeReference extends ReferenceDefinition {

    private final List<URI> promotedUris;

    /**
     * Construct a composite reference.
     *
     * @param name the name of the composite reference
     * @param promotedUris the list of component references it promotes
     */
    public CompositeReference(String name, List<URI> promotedUris) {
        super(name, null);
        this.promotedUris = promotedUris;
    }

    /**
     * Returns the list of references this composite reference promotes.
     *
     * @return the list of references this composite reference promotes
     */
    public List<URI> getPromotedUris() {
        return promotedUris;
    }

    /**
     * Adds the URI of a reference this composite reference promotes.
     *
     * @param uri the promoted reference URI
     */
    public void addPromotedUri(URI uri) {
        promotedUris.add(uri);
    }

    @Override
    public void validate() throws ValidationException {
        super.validate();
        if (promotedUris == null || promotedUris.isEmpty()) {
            throw new MissingPromotionException(this);
        }
    }
}
