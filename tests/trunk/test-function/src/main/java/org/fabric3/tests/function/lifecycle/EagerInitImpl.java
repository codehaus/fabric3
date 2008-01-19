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
package org.fabric3.tests.function.lifecycle;

import org.osoa.sca.annotations.Scope;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;

/**
 * @version $Rev$ $Date$
 */
@Scope("COMPOSITE")
@EagerInit
public class EagerInitImpl {
    private static boolean initialized;

    public static boolean isInitialized() {
        return initialized;
    }

    public static void setInitialized(boolean initialized) {
        EagerInitImpl.initialized = initialized;
    }

    @Init
    protected void init() {
        setInitialized(true);
    }
}
