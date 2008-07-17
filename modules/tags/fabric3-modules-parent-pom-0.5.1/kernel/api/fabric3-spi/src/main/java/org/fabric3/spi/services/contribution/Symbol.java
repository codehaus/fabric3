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

/**
 * Encapsulates an addressable key for a symbol space.
 *
 * @version $Rev$ $Date$
 */
public abstract class Symbol<KEY> {
    protected KEY key;

    public Symbol(KEY key) {
        this.key = key;
    }

    public KEY getKey() {
        return key;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Symbol symbol = (Symbol) o;

        return !(key != null ? !key.equals(symbol.key) : symbol.key != null);

    }

    public int hashCode() {
        return (key != null ? key.hashCode() : 0);
    }


}
