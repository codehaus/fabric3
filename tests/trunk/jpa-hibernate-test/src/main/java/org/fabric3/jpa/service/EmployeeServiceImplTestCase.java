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

import org.fabric3.jpa.model.Employee;
import org.osoa.sca.annotations.Reference;

import junit.framework.TestCase;

/**
 * @version $Revision$ $Date$
 */
public class EmployeeServiceImplTestCase extends TestCase {
    
    private EmployeeService employeeService;
    
    @Reference(required = true)
    public void setEmployeeService(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    public void testCreateEmployee() {
    	
    	Employee employee = employeeService.findEmployee(123L);
    	if (employee != null) {
    		employeeService.removeEmployee(123L);
    	}
        
        employee = employeeService.createEmployee(123l, "Barney Rubble");
        employee = employeeService.findEmployee(123L);
        
        assertNotNull(employee);
        assertEquals("Barney Rubble", employee.getName());
        
    }

}
