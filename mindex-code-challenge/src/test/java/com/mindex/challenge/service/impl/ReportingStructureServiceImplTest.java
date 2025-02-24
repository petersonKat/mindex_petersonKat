package com.mindex.challenge.service.impl;

import com.mindex.challenge.data.ReportingStructure;
import com.mindex.challenge.service.ReportingStructureService;
import com.mindex.challenge.data.Employee;
import com.mindex.challenge.service.EmployeeService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ReportingStructureServiceImplTest {
    private static String FIRST_NAME_1 = "John";
    private static String FIRST_NAME_2 = "Jane";
    private static String FIRST_NAME_3 = "Joe";
    private static String LAST_NAME = "Doe";
    private static String DEPARTMENT = "Engineering";
    private static String MANAGER = "Manager";
    private static String DEV_MANAGER = "Developer Manager";
    private static String DEV = "Developer";

    private String employeeUrl;
    private String reportingStructureIdUrl;

    @Autowired
    private ReportingStructureService reportingStructureService;

    @Autowired
    private EmployeeService EmployeeService;

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Before
    public void setup() {
        employeeUrl = "http://localhost:" + port + "/employee";
        reportingStructureIdUrl = "http://localhost:" + port + "/reportingstructure/{id}";
    }

    @Test
    public void testNonExistentEmployee(){
        //Set up employee with null id
        Employee testEmployee = createEmployee(FIRST_NAME_1, LAST_NAME, DEPARTMENT, DEV, null);
        //Check request fails due to null employeeId
        Exception exception = assertThrows(RuntimeException.class, () -> {
            reportingStructureService.read(testEmployee.getEmployeeId());
        });

        String expectedMessage = "Invalid employeeId: null";
        String actualMessage = exception.getMessage();
        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    public void testReadNoDirectReports() {
        //Set up employee for reporting structure test
        Employee testEmployee = createEmployee(FIRST_NAME_1, LAST_NAME, DEPARTMENT, DEV, null);

        //Already testing employee is created correctly in appropriate file, just need to verify id existence
        Employee createdEmployee = restTemplate.postForEntity(employeeUrl, testEmployee, Employee.class).getBody();

        assertNotNull(createdEmployee.getEmployeeId());


        //Check employee and direct reports properly returned
        ReportingStructure testReportingStructure = new ReportingStructure(createdEmployee, 0);

        ReportingStructure readReportingStructure = restTemplate.getForEntity(reportingStructureIdUrl, ReportingStructure.class, createdEmployee.getEmployeeId()).getBody();
        assertEquals(testReportingStructure.getEmployee().getEmployeeId(), readReportingStructure.getEmployee().getEmployeeId());
        assertReportingStructureEquivalence(testReportingStructure, readReportingStructure);
    }

    @Test
    public void testReadNonNestedDirectReports() {
        //Set up first employee for reporting structure test
        Employee reportTestEmployee = createEmployee(FIRST_NAME_2, LAST_NAME, DEPARTMENT, DEV, null);
        //Already testing employee is created correctly in appropriate file, just need to verify id existence
        Employee createdEmployee = restTemplate.postForEntity(employeeUrl, reportTestEmployee, Employee.class).getBody();

        assertNotNull(createdEmployee.getEmployeeId());

        //Set up manager
        List<Employee> employeeList = new ArrayList<>();
        employeeList.add(createdEmployee);
        Employee testEmployee = createEmployee(FIRST_NAME_1, LAST_NAME, DEPARTMENT, MANAGER, employeeList);//employee Jane assigned to John (should have 1 directReport)

        Employee createdBossEmployee = restTemplate.postForEntity(employeeUrl, testEmployee, Employee.class).getBody();

        assertNotNull(createdBossEmployee.getEmployeeId());


        //Check employee and direct reports properly returned
        ReportingStructure testReportingStructure = new ReportingStructure(createdBossEmployee, 1);

        ReportingStructure readReportingStructure = restTemplate.getForEntity(reportingStructureIdUrl, ReportingStructure.class, createdBossEmployee.getEmployeeId()).getBody();
        assertEquals(testReportingStructure.getEmployee().getEmployeeId(), readReportingStructure.getEmployee().getEmployeeId());
        assertReportingStructureEquivalence(testReportingStructure, readReportingStructure);
    }

    @Test
    public void testReadNestedDirectReports() {
        //initialize list for directReports
        List<Employee> employeeList = new ArrayList<>();
        
        //Set up first employee
        Employee nestReportTestEmployee = createEmployee(FIRST_NAME_3, LAST_NAME, DEPARTMENT, MANAGER, null);
        //Already testing employee is created correctly in appropriate file, just need to verify id existence
        Employee nestCreatedEmployee = restTemplate.postForEntity(employeeUrl, nestReportTestEmployee, Employee.class).getBody();

        assertNotNull(nestCreatedEmployee.getEmployeeId());

        //set up nested employee
        employeeList.add(nestCreatedEmployee);
        Employee reportTestEmployee = createEmployee(FIRST_NAME_2, LAST_NAME, DEPARTMENT, DEV_MANAGER, employeeList);//employee Joe assigned to Jane

        Employee createdEmployee = restTemplate.postForEntity(employeeUrl, reportTestEmployee, Employee.class).getBody();

        assertNotNull(createdEmployee.getEmployeeId());

        //set up final employee
        employeeList.remove(0);
        employeeList.add(createdEmployee);
        Employee testEmployee = createEmployee(FIRST_NAME_1, LAST_NAME, DEPARTMENT, MANAGER, employeeList);//employee Jane assigned to John (should now have 2 directReports)

        Employee createdBossEmployee = restTemplate.postForEntity(employeeUrl, testEmployee, Employee.class).getBody();

        assertNotNull(createdBossEmployee.getEmployeeId());

        //Check employee and direct reports properly returned
        ReportingStructure testReportingStructure = new ReportingStructure(createdBossEmployee, 2);

        ReportingStructure readReportingStructure = restTemplate.getForEntity(reportingStructureIdUrl, ReportingStructure.class, createdBossEmployee.getEmployeeId()).getBody();
        assertEquals(testReportingStructure.getEmployee().getEmployeeId(), readReportingStructure.getEmployee().getEmployeeId());
        assertReportingStructureEquivalence(testReportingStructure, readReportingStructure);
    }

    private static void assertReportingStructureEquivalence(ReportingStructure expected, ReportingStructure actual) {
        assertEquals(expected.getEmployee().getFirstName(), actual.getEmployee().getFirstName());
        assertEquals(expected.getEmployee().getLastName(), actual.getEmployee().getLastName());
        assertEquals(expected.getEmployee().getDepartment(), actual.getEmployee().getDepartment());
        assertEquals(expected.getEmployee().getPosition(), actual.getEmployee().getPosition());
        assertEquals(expected.getNumberOfReports(), actual.getNumberOfReports());
    }

    private static Employee createEmployee(String firstName, String lastName, String department, String position, List<Employee> employeeList){
        Employee employee = new Employee();
        employee.setFirstName(firstName);
        employee.setLastName(lastName);
        employee.setDepartment(department);
        employee.setPosition(position);
        employee.setDirectReports(employeeList);
        return employee;
    }
}
