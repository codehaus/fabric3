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
package org.fabric3.scdl4j.spi;

/**
 * Exception indicating that there was a error during introspection.
 *
 * @version $Rev$ $Date$
 */
public abstract class IntrospectionException extends Exception {
    protected IntrospectionException() {
    }

    protected IntrospectionException(String string) {
        super(string);
    }

    protected IntrospectionException(String string, Throwable throwable) {
        super(string, throwable);
    }

    protected IntrospectionException(Throwable throwable) {
        super(throwable);
    }
}
