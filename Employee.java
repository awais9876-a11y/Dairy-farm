package com.example.DairyFarm;


public class Employee {
    private String employeeId;
    private String name;
    private String role;

//    constructor
    public Employee(String employeeId, String name, String role) {
        this.employeeId = employeeId;
        this.name = name;
        this.role = role;
    }

//    file work
    public String toFileString() {
        return employeeId + "," + name + "," + role;
    }

    public static Employee fromFileString(String line) throws DataParseException {
        try {
            String[] p = line.split(",");
            return new Employee(p[0], p[1], p[2]);
        } catch (Exception e) {
            throw new DataParseException("Failed to parse Employee: " + line, e);
        }
    }

    // Getters and setters
    public String getEmployeeId() { return employeeId; }
    public String getName() { return name; }
    public String getRole() { return role; }

    public void setName(String name) { this.name = name; }
    public void setRole(String role) { this.role = role; }
}
