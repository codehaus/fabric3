package org.fabric3.fabric.generator.classloader;

import org.osoa.sca.annotations.Property;

import org.fabric3.fabric.command.UnprovisionClassloaderCommand;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.generator.RemoveCommandGenerator;
import org.fabric3.spi.model.instance.LogicalComponent;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
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
public class UnprovisionClassloaderCommandGenerator implements RemoveCommandGenerator {

    private final int order;

    public UnprovisionClassloaderCommandGenerator(@Property(name = "order")int order) {
        this.order = order;
    }

    public int getOrder() {
        return order;
    }

    @SuppressWarnings("unchecked")
    public UnprovisionClassloaderCommand generate(LogicalComponent<?> component) throws GenerationException {
        return new UnprovisionClassloaderCommand(order, component.getUri());
    }

}
