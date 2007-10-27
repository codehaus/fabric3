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
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;

import org.fabric3.jpa.model.Employee;

/**
 * @version $Revision$ $Date$
 */
public class EmployeeServiceImpl implements EmployeeService {
    
    private EntityManagerFactory emf;
    
    @PersistenceUnit(name="employeeEmf", unitName="employee")
    public void setEntityManagerFactory(EntityManagerFactory emf) {
        this.emf = emf;
    }

    /**
     * @see org.fabric3.jpa.service.EmployeeService#createEmployee(java.lang.Long, java.lang.String)
     */
    public Employee createEmployee(Long id, String name) {
        
        EntityManager em = emf.createEntityManager();
            
        Employee employee = new Employee(id, name);
        em.persist(employee);
        em.flush();
    
        return employee;
        
    }

    /**
     * @see org.fabric3.jpa.service.EmployeeService#findEmployee(java.lang.Long)
     */
    public Employee findEmployee(Long id) {
        return emf.createEntityManager().find(Employee.class, id);
    }

    /**
     * @see org.fabric3.jpa.service.EmployeeService#removedEmployee(java.lang.Long)
     */
    public void removeEmployee(Long id)  {

        EntityManager em = emf.createEntityManager();

        Employee employee = em.find(Employee.class, id);
        em.remove(employee);

        em.flush();

    }

}
