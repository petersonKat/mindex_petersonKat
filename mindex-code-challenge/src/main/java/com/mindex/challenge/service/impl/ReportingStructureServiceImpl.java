package com.mindex.challenge.service.impl;

import com.mindex.challenge.data.ReportingStructure;
import com.mindex.challenge.service.ReportingStructureService;
import com.mindex.challenge.dao.EmployeeRepository;
import com.mindex.challenge.data.Employee;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ReportingStructureServiceImpl implements ReportingStructureService {

    private static final Logger LOG = LoggerFactory.getLogger(ReportingStructureServiceImpl.class);

    @Autowired
    private EmployeeRepository employeeRepository;

    @Override
    public ReportingStructure read(String id) {
        LOG.debug("Finding Employee with id [{}]", id);

        Employee employee = employeeRepository.findByEmployeeId(id);

        //If employee does not exist, cannot have a reporting structure
        if (employee == null) {
            throw new RuntimeException("Invalid employeeId: " + id);
        }

        LOG.debug("Generating reportingStructure with id [{}]", id);

        ReportingStructure reportingStructure = new ReportingStructure(employee, countDirectReport(employee, 0));


        return reportingStructure;
    }

    //Recursive helper function to count all direct reports for an employee
    public int countDirectReport(Employee employee, int numDirectReports){
        if(employee.getDirectReports() != null){
            Employee directEmployee = null;
            //If direct reports exist, start by adding in all reporting employees
            numDirectReports += employee.getDirectReports().size();
            //Check reporting employees to see if they also have reporting employees
            for(Employee e: employee.getDirectReports()){
                directEmployee = employeeRepository.findByEmployeeId(e.getEmployeeId());
                //if any employee under the top employee has underlings, repeat process to count them as well
                if(employeeRepository.findByEmployeeId(directEmployee.getEmployeeId()).getDirectReports() != null){
                    numDirectReports = countDirectReport(directEmployee, numDirectReports);
                }
            }
            return numDirectReports;
        }
        else{
            return 0;//if no direct reports, return 0
        }
    }
}
