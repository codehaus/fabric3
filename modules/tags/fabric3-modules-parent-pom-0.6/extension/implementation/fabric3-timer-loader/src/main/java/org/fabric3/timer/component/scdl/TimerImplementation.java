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
package org.fabric3.timer.component.scdl;

import javax.xml.namespace.QName;

import org.fabric3.java.scdl.JavaImplementation;
import org.fabric3.spi.Constants;
import org.fabric3.timer.component.provision.TriggerData;

/**
 * Represents a timer component implementation type.
 *
 * @version $$Rev: 3079 $$ $$Date: 2008-03-13 03:30:59 -0700 (Thu, 13 Mar 2008) $$
 */
public class TimerImplementation extends JavaImplementation {
    public static final QName IMPLEMENTATION_TIMER = new QName(Constants.FABRIC3_NS, "implementation.timer");
    private static final long serialVersionUID = -911919528396189874L;
    private TriggerData triggerData;

    public QName getType() {
        return IMPLEMENTATION_TIMER;
    }

    public TriggerData getTriggerData() {
        return triggerData;
    }

    public void setTriggerData(TriggerData triggerData) {
        this.triggerData = triggerData;
    }
}