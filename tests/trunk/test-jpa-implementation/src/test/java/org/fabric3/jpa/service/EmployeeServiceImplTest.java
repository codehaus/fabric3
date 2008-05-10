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
package org.fabric3.jpa.service;

import java.util.ArrayList;
import java.util.List;

import org.fabric3.jpa.model.Employee;

import org.osoa.sca.annotations.Reference;

import junit.framework.TestCase;

/**
 * @version $Revision$ $Date$
 */
public class EmployeeServiceImplTest extends TestCase {
    
    @Reference protected EmployeeService employeeService;
    
    public void testCreation() {
        
        List<Employee> employees = new ArrayList<Employee>();
        employees.add(new Employee(1L, "Meeraj Kunnumpurath"));
        employees.add(new Employee(2L, "Jeremy Boynes"));
        employees.add(new Employee(3L, "Jim Marino"));
        
        employeeService.createEmployees(employees);
        
    }
    
    
}
