/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the “License”), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an “as is” basis,
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
package org.fabric3.scdl;

import java.net.URI;
import java.util.List;

import org.fabric3.scdl.validation.MissingPromotion;

/**
 * @version $Rev$ $Date$
 */
public class CompositeReference extends ReferenceDefinition {
    private static final long serialVersionUID = 5387987439912912994L;

    private final List<URI> promotedUris;

    /**
     * Construct a composite reference.
     *
     * @param name         the name of the composite reference
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
    public void validate(ValidationContext context) {
        super.validate(context);
        if (promotedUris == null || promotedUris.isEmpty()) {
            context.addError(new MissingPromotion(this));
        }
    }
}
