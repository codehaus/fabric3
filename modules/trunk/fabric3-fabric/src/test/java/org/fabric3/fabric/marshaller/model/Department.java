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
package org.fabric3.fabric.marshaller.model;

import java.util.LinkedList;
import java.util.List;

import org.fabric3.spi.model.type.ModelObject;

/**
 * @version $Revision$ $Date$
 */
public class Department extends ModelObject {
    
    private String name;
    private List<Employee> employees = new LinkedList<Employee>();
    /**
     * @return the employees
     */
    public List<Employee> getEmployees() {
        return employees;
    }
    /**
     * @param employees the employees to set
     */
    public void setEmployees(List<Employee> employees) {
        this.employees = employees;
    }
    /**
     * @return the name
     */
    public String getName() {
        return name;
    }
    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

}
