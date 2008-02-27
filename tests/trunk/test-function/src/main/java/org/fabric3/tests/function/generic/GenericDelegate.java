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
package org.fabric3.tests.function.generic;

import org.osoa.sca.annotations.Reference;

/**
 * @version $Rev$ $Date$
 */
public class GenericDelegate implements GenericInterface<String> {
    @Reference
    protected GenericInterface<String> delegate;

    public String echo(String t) {
        return delegate.echo(t);
    }
}
