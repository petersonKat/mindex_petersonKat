package com.mindex.challenge.service.impl;

import com.mindex.challenge.data.Compensation;
import com.mindex.challenge.data.Employee;
import com.mindex.challenge.service.CompensationService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CompensationServiceImplTest {
    private static String SALARY = "$100";
    private static String EFFECTIVE_DATE = "05/1/1968";

    private String compensationUrl;
    private String compensationIdUrl;
    private String employeeUrl;

    @Autowired
    private CompensationService compensationService;

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Before
    public void setup() {
        compensationUrl = "http://localhost:" + port + "/compensation";
        compensationIdUrl = "http://localhost:" + port + "/compensation/{id}";
        employeeUrl = "http://localhost:" + port + "/employee";
    }

    @Test
    public void testCreateNonExistingEmployee() {
        //create compensation with no employeeId
        Compensation testCompensation = new Compensation();
        testCompensation.setSalary(SALARY);
        testCompensation.setEffectiveDate(EFFECTIVE_DATE);

        //Try to create compensation with no employeeId, no id comes back because an error is thrown
        Compensation createdCompensation = restTemplate.postForEntity(compensationUrl, testCompensation, Compensation.class).getBody();

        assertNull(createdCompensation.getCompensationId());

        //Proving above statement by running service and receiving runtime error
        Exception exception = assertThrows(RuntimeException.class, () -> {
            compensationService.create(testCompensation);
        });

        String expectedMessage = "Invalid employeeId: null";
        String actualMessage = exception.getMessage();

        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    public void testReadNonExistingCompensation() {
        //create compensation with no compensationId and try to read
        Compensation testCompensation = new Compensation();
        Compensation readCompensation = restTemplate.getForEntity(compensationIdUrl, Compensation.class, testCompensation.getCompensationId()).getBody();

        //Nothing comes back because an error is thrown
        assertNotNull(readCompensation);

        //Proving above statement by running service and receiving runtime error
        Exception exception = assertThrows(RuntimeException.class, () -> {
            compensationService.read(null);
        });

        String expectedMessage = "Invalid compensationId: null";
        String actualMessage = exception.getMessage();

        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    public void testCreateReadExistingEmployee() {
        //create test employee for compensation
        Employee testEmployee = new Employee();
        testEmployee.setFirstName("John");
        testEmployee.setLastName("Doe");
        testEmployee.setDepartment("Engineering");
        testEmployee.setPosition("Developer");

        //Already checking equality in employee tests, just need to verify id created
        Employee createdEmployee = restTemplate.postForEntity(employeeUrl, testEmployee, Employee.class).getBody();

        assertNotNull(createdEmployee.getEmployeeId());

        Compensation testCompensation = new Compensation();
        testCompensation.setEmployeeId(createdEmployee.getEmployeeId());
        testCompensation.setSalary(SALARY);
        testCompensation.setEffectiveDate(EFFECTIVE_DATE);

        //Expect compensation properly created with real employeeId
        Compensation createdCompensation = restTemplate.postForEntity(compensationUrl, testCompensation, Compensation.class).getBody();

        assertNotNull(createdCompensation.getCompensationId());
        assertCompensationEquivalence(testCompensation, createdCompensation);


        //Expect compensation properly fetched with real compensationId
        Compensation readCompensation = restTemplate.getForEntity(compensationIdUrl, Compensation.class, createdCompensation.getCompensationId()).getBody();
        assertEquals(createdCompensation.getCompensationId(), readCompensation.getCompensationId());
        assertCompensationEquivalence(createdCompensation, readCompensation);
    }

    private static void assertCompensationEquivalence(Compensation expected, Compensation actual) {
        assertEquals(expected.getEmployeeId(), actual.getEmployeeId());
        assertEquals(expected.getSalary(), actual.getSalary());
        assertEquals(expected.getEffectiveDate(), actual.getEffectiveDate());
    }
}
