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
package org.fabric3.test.wiring;

import org.osoa.sca.annotations.Reference;

/**
 * @version $Rev$ $Date$
 */
public class TestComponent implements TestService {
    private Target constructorTarget;
    private Target target;
    private Target promotedReference;
    private Target nonConfiguredPromotedReference;
    private Target optionalNonSetReference;

    public TestComponent(@Reference(name = "targetConstructor")Target constructorTarget) {
        this.constructorTarget = constructorTarget;
    }

    @Reference
    public void setTarget(Target target) {
        this.target = target;
    }

    @Reference
    public void setPromotedReference(Target promotedReference) {
        this.promotedReference = promotedReference;
    }

    @Reference
    public void setNonConfiguredPromotedReference(Target target) {
        this.nonConfiguredPromotedReference = target;
    }

    @Reference(required = false)
    public void setOptionalNonSetReference(Target optionalNonSetReference) {
        this.optionalNonSetReference = optionalNonSetReference;
    }

    public Target getTarget() {
        return target;
    }

    public Target getPromotedReference() {
        return promotedReference;
    }

    public Target getNonConfiguredPromotedReference() {
        return nonConfiguredPromotedReference;
    }

    public Target getConstructorTarget() {
        return constructorTarget;
    }

    public Target getOptionalNonSetReference() {
        return optionalNonSetReference;
    }
}
