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
package org.fabric3.fabric.marshaller;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.fabric3.fabric.marshaller.model.Department;
import org.fabric3.fabric.marshaller.model.Employee;
import org.fabric3.fabric.marshaller.model.Skill;
import org.fabric3.spi.marshaller.MarshallerRegistry;

import junit.framework.TestCase;

/**
 * @version $Revision$ $Date$
 */
public class DefaultMarshallerRegistryTest extends TestCase {
    
    private MarshallerRegistry registry;
    
    private Department department;

    public DefaultMarshallerRegistryTest(String arg0) {
        super(arg0);
    }

    protected void setUp() throws Exception {
        
        registry = new DefaultMarshallerRegistry();
        
        Object[][] xmlToJava = new Object[3][2];
        
        xmlToJava[0][0] = new QName("http://www.example.com/department", "department");
        xmlToJava[0][1] = Department.class;
        
        xmlToJava[1][0] = new QName("http://www.example.com/employee", "employee");
        xmlToJava[1][1] = Employee.class;
        
        xmlToJava[2][0] = new QName("http://www.example.com/skill", "skill");
        xmlToJava[2][1] = Skill.class;
        
        for(int i = 0;i < 3;i++) {
            ReflectionMarshaller marshaller = new ReflectionMarshaller();
            marshaller.setXmlName(xmlToJava[i][0].toString());
            marshaller.setModelClass(((Class) xmlToJava[i][1]).getName());
            marshaller.setMarshallerRegistry(registry);
            marshaller.init();
        }
        
        department = new Department();
        department.setName("IT");
        
        Employee employee = new Employee();
        employee.setName("Fred Flintstone");
        Skill skill = new Skill();
        skill.setName("Eating");
        skill.setLevel(100);
        employee.getSkills().add(skill);
        skill = new Skill();
        skill.setName("Drinking");
        skill.setLevel(1000);
        employee.getSkills().add(skill);
        department.getEmployees().add(employee);
        
        employee = new Employee();
        employee.setName("Barney Rubble");
        skill = new Skill();
        skill.setName("Moaning");
        skill.setLevel(100);
        employee.getSkills().add(skill);
        department.getEmployees().add(employee);
        
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testMarshallAndUnmarshall() throws Exception {
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        
        XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(out);
        registry.marshall(department, writer);
        
        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(in);
        
        Department department = (Department) registry.unmarshall(reader);
        assertNotNull(department);
        assertEquals("IT", department.getName());
        
        List<Employee> employees = department.getEmployees();
        assertEquals(2, employees.size());
        
        Employee employee = employees.get(0);
        assertEquals("Fred Flintstone", employee.getName());
        
        List<Skill> skills = employee.getSkills();
        assertEquals(2, skills.size());
        
        Skill skill = skills.get(0);
        assertEquals("Eating", skill.getName());
        assertEquals(100, skill.getLevel());
        
        skill = skills.get(1);
        assertEquals("Drinking", skill.getName());
        assertEquals(1000, skill.getLevel());
        
        employee = employees.get(1);
        assertEquals("Barney Rubble", employee.getName());
        
        skills = employee.getSkills();
        assertEquals(1, skills.size());
        
        skill = skills.get(0);
        assertEquals("Moaning", skill.getName());
        assertEquals(100, skill.getLevel());
        
    }

}
