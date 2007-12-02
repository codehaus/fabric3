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

/**
 * @version $Rev: 1960 $ $Date: 2007-11-12 18:17:03 +0000 (Mon, 12 Nov 2007) $
 */
public class PromotedServiceNotFoundException extends ResolutionException {
    private static final long serialVersionUID = 2303515371429492705L;

    public PromotedServiceNotFoundException(URI source, URI target) {
        super(source, target);
    }

    public String getMessage() {
        return "The composite service or reference " + getSource() + " promotes a component " + getTarget() + " that could not be found.";
    }
}