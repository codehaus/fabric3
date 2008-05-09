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

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.osoa.sca.annotations.Scope;

import org.fabric3.jpa.model.Employee;

/**
 * Exercises multi-threaded EntityManager operation
 *
 * @version $Revision$ $Date$
 */
@Scope("COMPOSITE")
public class MultiThreadedEmployeeServiceImpl implements EmployeeService {

    private EntityManager em;

    @PersistenceContext(name = "employeeEmf", unitName = "employee")
    public void setEntityManager(EntityManager em) {
        this.em = em;
    }

    public Employee createEmployee(Long id, String name) {
        Employee employee = new Employee(id, name);
        em.persist(employee);
        return employee;
    }

    public Employee findEmployee(Long id) {
        return em.find(Employee.class, id);
    }

    public void removeEmployee(Long id) {
        Employee employee = em.find(Employee.class, id);
        em.remove(employee);
    }

}