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
package org.fabric3.rs.scdl;

import java.net.URI;
import javax.xml.namespace.QName;
import org.fabric3.scdl.BindingDefinition;

/**
 * @version $Rev$ $Date$
 */
public class RsBindingDefinition extends BindingDefinition {

    public static final QName BINDING_RS = new QName("http://www.fabric3.org/xmlns/rs/1.0", "binding.rs");
    private boolean isResource;
    private boolean isProvider;

    public RsBindingDefinition(URI targetUri) {
        super(targetUri, BINDING_RS);
    }

    public boolean isProvider() {
        return isProvider;
    }

    public void setIsProvider(boolean value) {
        this.isProvider = value;
    }

    public boolean isResource() {
        return isResource;
    }

    public void setIsResource(boolean value) {
        this.isResource = value;
    }
}
