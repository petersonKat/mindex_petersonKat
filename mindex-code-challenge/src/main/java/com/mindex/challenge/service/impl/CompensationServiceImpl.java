package com.mindex.challenge.service.impl;

import com.mindex.challenge.dao.CompensationRepository;
import com.mindex.challenge.data.Compensation;
import com.mindex.challenge.service.CompensationService;
import com.mindex.challenge.dao.EmployeeRepository;
import com.mindex.challenge.data.Employee;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public class CompensationServiceImpl implements CompensationService {

    private static final Logger LOG = LoggerFactory.getLogger(CompensationServiceImpl.class);

    @Autowired
    private CompensationRepository compensationRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Override
    public Compensation create(Compensation compensation) {
        LOG.debug("Checking existence of specified employeeId [{}]", compensation.getEmployeeId());
        Employee employee = employeeRepository.findByEmployeeId(compensation.getEmployeeId());
        
        //If employeeId does not go to existing employee, do not want to allow creation
        if(employee == null){
            throw new RuntimeException("Invalid employeeId: " + compensation.getEmployeeId());
        }
        LOG.debug("Creating Compensation [{}]", compensation);

        //Randomize id for primary key purposes
        compensation.setCompensationId(UUID.randomUUID().toString());
        compensationRepository.insert(compensation);

        return compensation;
    }

    @Override
    public Compensation read(String compensationId) {
        LOG.debug("Finding Compensation with id [{}]", compensationId);

        Compensation compensation = compensationRepository.findByCompensationId(compensationId);

        if (compensation == null) {
            throw new RuntimeException("Invalid compensationId: " + compensationId);
        }

        return compensation;
    }
}
