/*
* Fabric3
* Copyright (c) 2009 Metaform Systems
*
* Fabric3 is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as
* published by the Free Software Foundation, either version 3 of
* the License, or (at your option) any later version, with the
* following exception:
*
* Linking this software statically or dynamically with other
* modules is making a combined work based on this software.
* Thus, the terms and conditions of the GNU General Public
* License cover the whole combination.
*
* As a special exception, the copyright holders of this software
* give you permission to link this software with independent
* modules to produce an executable, regardless of the license
* terms of these independent modules, and to copy and distribute
* the resulting executable under terms of your choice, provided
* that you also meet, for each linked independent module, the
* terms and conditions of the license of that module. An
* independent module is a module which is not derived from or
* based on this software. If you modify this software, you may
* extend this exception to your version of the software, but
* you are not obligated to do so. If you do not wish to do so,
* delete this exception statement from your version.
*
* Fabric3 is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty
* of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
* See the GNU General Public License for more details.
*
* You should have received a copy of the
* GNU General Public License along with Fabric3.
* If not, see <http://www.gnu.org/licenses/>.
*/
package org.fabric3.spi.transform;

import org.fabric3.model.type.contract.DataType;

/**
 * Creates a transformer capable of converting a set of classes from a source to target type.
 *
 * @version $Rev: 7606 $ $Date: 2009-09-09 16:00:11 +0200 (Wed, 09 Sep 2009) $
 */
public interface TransformerFactory<SOURCE, TARGET> {

    /**
     * Returns true if the factory creates transformers that can transform from the source to target data types.
     *
     * @param source the source datatype
     * @param target the target datatype
     * @return true if the factory creates transformers that can transform from the source to target data types
     */
    boolean canTransform(DataType<?> source, DataType<?> target);

    /**
     * Creates a transformer capable of transforming to or from the given set of classes.
     *
     * @param classes the classes to transform
     * @return the transformer
     * @throws TransformationException if there was an error creating the transformer
     */
    Transformer<SOURCE, TARGET> create(Class<?>... classes) throws TransformationException;
}