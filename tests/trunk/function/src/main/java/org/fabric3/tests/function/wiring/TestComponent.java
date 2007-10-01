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
package org.fabric3.tests.function.wiring;

import org.osoa.sca.annotations.Reference;

import org.fabric3.tests.function.common.HelloService;

/**
 * @version $Rev$ $Date$
 */
public class TestComponent implements TestService {
    private HelloService constructorService;
    private HelloService service;
    private HelloService promotedReference;
    private HelloService nonConfiguredPromotedReference;
    private HelloService optionalNonSetReference;

    public TestComponent(@Reference(name = "targetConstructor")HelloService constructorHelloService) {
        this.constructorService = constructorHelloService;
    }

    @Reference
    public void setService(HelloService service) {
        this.service = service;
    }

    @Reference
    public void setPromotedReference(HelloService promotedReference) {
        this.promotedReference = promotedReference;
    }

    @Reference
    public void setNonConfiguredPromotedReference(HelloService target) {
        this.nonConfiguredPromotedReference = target;
    }

    @Reference(required = false)
    public void setOptionalNonSetReference(HelloService optionalNonSetReference) {
        this.optionalNonSetReference = optionalNonSetReference;
    }

    public HelloService getService() {
        return service;
    }

    public HelloService getPromotedReference() {
        return promotedReference;
    }

    public HelloService getNonConfiguredPromotedReference() {
        return nonConfiguredPromotedReference;
    }

    public HelloService getConstructorService() {
        return constructorService;
    }

    public HelloService getOptionalNonSetReference() {
        return optionalNonSetReference;
    }
}
