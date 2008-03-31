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

/**
 * @version $Rev$ $Date$
 */
public interface PushTransformer<SOURCE, TARGET> extends Transformer {
    /**
     * Transforms the source by writing it to the target.
     *
     * @param source the source instance
     * @param target the target to be written to
     * @param context the context for this transformation
     * @throws TransformationException if there was a problem during the transformation
     */
    void transform(SOURCE source, TARGET target, TransformContext context) throws TransformationException;
}
