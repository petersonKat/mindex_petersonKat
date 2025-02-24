package com.mindex.challenge.data;


public class ReportingStructure {
    private Employee employee;
    private int numberOfReports;

    public ReportingStructure() {
        this.employee = new Employee();
    }

    public ReportingStructure(Employee employee, int numberOfReports) {
        this.employee = employee;
        this.numberOfReports = numberOfReports;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee.setEmployeeId(employee.getEmployeeId());
        this.employee.setFirstName(employee.getFirstName());
        this.employee.setLastName(employee.getLastName());
        this.employee.setPosition(employee.getPosition());
        this.employee.setDepartment(employee.getDepartment());
        this.employee.setDirectReports(employee.getDirectReports());
    }

    public int getNumberOfReports() {
        return numberOfReports;
    }

    public void setNumberOfReports(int numberOfReports) {
        this.numberOfReports = numberOfReports;
    }

}
