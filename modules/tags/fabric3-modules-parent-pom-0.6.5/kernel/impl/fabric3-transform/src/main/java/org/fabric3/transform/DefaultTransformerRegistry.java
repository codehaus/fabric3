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
 */
package org.fabric3.transform;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.fabric3.scdl.DataType;
import org.fabric3.transform.Transformer;
import org.fabric3.transform.TransformerRegistry;

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
