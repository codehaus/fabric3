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
package org.fabric3.fabric.assembly.resolver;

import java.net.URI;
import java.util.Collections;
import java.util.List;

import org.fabric3.fabric.assembly.ResolutionException;

/**
 * Thrown when an autowire cannot be resolved to a specific target, i.e. when more than one potential target exists.
 *
 * @version $Rev$ $Date$
 */
public class AmbiguousAutowireTargetException extends ResolutionException {
    private List<URI> targets;
    private static final long serialVersionUID = -8902348708584039561L;

    public AmbiguousAutowireTargetException(String message, URI source, List<URI> targets) {
        super(message, (String) null, source, null);
        this.targets = targets;
    }

    public List<URI> getTargets() {
        return Collections.unmodifiableList(targets);
    }


}
