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
package org.fabric3.transform;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.fabric3.spi.model.type.DataType;
import org.fabric3.spi.transform.Transformer;
import org.fabric3.spi.transform.TransformerRegistry;

/**
 * @version $Rev$ $Date$
 */
public class DefaultTransformerRegistry<T extends Transformer> implements TransformerRegistry<T> {
    private final Map<TransformerPair, T> transformers = new ConcurrentHashMap<TransformerPair, T>();

    public void register(T transformer) {
        TransformerPair pair = new TransformerPair(transformer.getSourceType(), transformer.getTargetType());
        transformers.put(pair, transformer);
    }

    public void unregister(T transformer) {
        TransformerPair pair = new TransformerPair(transformer.getSourceType(), transformer.getTargetType());
        transformers.remove(pair);
    }

    public T getTransformer(DataType<?> source, DataType<?> target) {
        TransformerPair pair = new TransformerPair(source, target);
        return transformers.get(pair);
    }

    private static class TransformerPair {
        private final DataType<?> source;
        private final DataType<?> target;


        public TransformerPair(DataType<?> source, DataType<?> target) {
            this.source = source;
            this.target = target;
        }


        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            TransformerPair that = (TransformerPair) o;

            return source.equals(that.source) && target.equals(that.target);

        }

        public int hashCode() {
            int result;
            result = source.hashCode();
            result = 31 * result + target.hashCode();
            return result;
        }
    }
}
