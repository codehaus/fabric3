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
public class EmployeeServiceImplTest extends TestCase {
    @Reference
    protected EmployeeService employeeService;

    @Reference
    protected EmployeeService employeeMultiThreadedService;

    @Reference
    protected EmployeeService employeeEMFService;

    @Reference
    protected ConversationEmployeeService conversationEmployeeService;

    public void testCreateEmployee() {
        Employee employee;

        employeeService.createEmployee(123l, "Barney Rubble");
        employee = employeeService.findEmployee(123L);

        assertNotNull(employee);
        assertEquals("Barney Rubble", employee.getName());

    }

    public void testCreateEMFEmployee() throws Exception {
        employeeEMFService.createEmployee(123l, "Barney Rubble");
        Employee employee = employeeEMFService.findEmployee(123L);

        assertNotNull(employee);
        assertEquals("Barney Rubble", employee.getName());

    }

    public void testCreateMultiThreadedEmployee() {
        employeeMultiThreadedService.createEmployee(123l, "Barney Rubble");
        Employee employee = employeeMultiThreadedService.findEmployee(123L);

        assertNotNull(employee);
        assertEquals("Barney Rubble", employee.getName());

    }

    public void testExtendedPersistenceContext() {
        conversationEmployeeService.createEmployee(123l, "Barney Rubble");
        Employee employee = conversationEmployeeService.findEmployee(123L);

        assertNotNull(employee);
        assertEquals("Barney Rubble", employee.getName());
        // verify the object has not be detached
        employee.setName("Fred Flintstone");
        Employee employee2 = conversationEmployeeService.updateEmployee(employee);
        // the merge operation should use the same persistent entity since it is never detached for extended persistence contexts
        assertSame(employee, employee2);
        employee = conversationEmployeeService.findEmployee(123L);
        assertEquals("Fred Flintstone", employee.getName());
        // end the conversation, which should also close the EntityManager/persistence context
        conversationEmployeeService.end();
        employee2 = conversationEmployeeService.findEmployee(123L);
        // employee2 should be loaded in a different persistence context and not the same as the original
        assertNotSame(employee, employee2);
    }

    protected void setUp() throws Exception {
        super.setUp();
        Employee employee = employeeService.findEmployee(123L);
        if (employee != null) {
            employeeService.removeEmployee(123L);
        }
    }


}
