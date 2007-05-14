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
package org.fabric3.fabric.component.scope;

import java.io.PrintWriter;
import java.util.List;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Destroy;

import org.fabric3.host.monitor.ExceptionFormatter;
import org.fabric3.host.monitor.FormatterRegistry;
import org.fabric3.host.Fabric3Exception;
import org.fabric3.api.Fabric3RuntimeException;
import org.fabric3.spi.component.GroupInitializationException;

/**
 * @version $Rev$ $Date$
 */
@EagerInit
public class GroupInitializationExceptionFormatter implements ExceptionFormatter<GroupInitializationException> {

    private final FormatterRegistry registry;


    public GroupInitializationExceptionFormatter(@Reference FormatterRegistry registry) {
        this.registry = registry;
    }

    @Init
    void init() {
        registry.register(this);
    }

    @Destroy
    void destroy() {
        registry.unregister(this);
    }

    public boolean canFormat(Class<?> type) {
        return GroupInitializationException.class.isAssignableFrom(type);
    }

    public PrintWriter write(PrintWriter writer, GroupInitializationException exception) {
        exception.appendBaseMessage(writer);
        List<Exception> causes = exception.getCauses();
        for (Exception cause : causes) {
            if (cause instanceof Fabric3Exception) {
                Fabric3Exception f3ex = (Fabric3Exception) cause;
                writer.println(f3ex.getIdentifier() + " caused :");
            } else if (cause instanceof Fabric3RuntimeException) {
                Fabric3RuntimeException f3ex = (Fabric3RuntimeException) cause;
                writer.println(f3ex.getIdentifier() + " caused :");
            } else {
                writer.println("Caused by:");
            }
            exception.printStackTrace(writer);
        }
        return writer;
    }
}
