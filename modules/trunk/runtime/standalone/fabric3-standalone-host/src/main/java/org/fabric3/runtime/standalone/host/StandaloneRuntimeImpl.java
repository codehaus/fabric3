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
package org.fabric3.runtime.standalone.host;

import org.fabric3.api.annotation.LogLevel;
import org.fabric3.fabric.runtime.AbstractRuntime;
import org.fabric3.monitor.JavaLoggingMonitorFactory;
import org.fabric3.runtime.standalone.StandaloneHostInfo;
import org.fabric3.runtime.standalone.StandaloneRuntime;

/**
 * @version $Rev$ $Date$
 */
public class StandaloneRuntimeImpl extends AbstractRuntime<StandaloneHostInfo> implements StandaloneRuntime {
    StandaloneMonitor monitor;

    public StandaloneRuntimeImpl() {
        // FIXME
        super(StandaloneHostInfo.class, new JavaLoggingMonitorFactory(null, null, "f3"));
        monitor = getMonitorFactory().getMonitor(StandaloneMonitor.class);
    }


    public interface StandaloneMonitor {
        @LogLevel("SEVERE")
        void runError(Exception e);
    }
}
