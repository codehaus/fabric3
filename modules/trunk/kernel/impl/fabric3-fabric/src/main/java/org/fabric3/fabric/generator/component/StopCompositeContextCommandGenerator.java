package org.fabric3.fabric.generator.component;

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
import org.fabric3.fabric.command.StopCompositeContextCommand;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.generator.RemoveCommandGenerator;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;

/**
 * Generates a command to stop the composite context on a runtime.
 *
 * @version $Rev: 4150 $ $Date: 2008-05-09 12:33:01 -0700 (Fri, 09 May 2008) $
 */

public class StopCompositeContextCommandGenerator implements RemoveCommandGenerator {


    private final int order;

    public StopCompositeContextCommandGenerator(int order) {
        this.order = order;
    }

    public int getOrder() {
        return order;
    }

     @SuppressWarnings("unchecked")
    public StopCompositeContextCommand generate(LogicalComponent<?> component) throws GenerationException {
        if (!component.isProvisioned() && component instanceof LogicalCompositeComponent) {
            return new StopCompositeContextCommand(order, component.getUri());
        }
        return null;

    }
}
