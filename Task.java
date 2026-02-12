package com.example.DairyFarm;
import java.time.LocalDate;

public class Task {
    private String taskId;
    private String employeeId;
    private String description;
    private LocalDate dueDate;
    private String status; // pending/completed

//    contructor
    public Task(String taskId, String employeeId, String description, LocalDate dueDate, String status) {
        this.taskId = taskId;
        this.employeeId = employeeId;
        this.description = description;
        this.dueDate = dueDate;
        this.status = status;
    }

//    method
    public void markCompleted() {
        status = "completed";
    }

//    file work
    public String toFileString() {
        return taskId + "," + employeeId + "," + description + "," + dueDate + "," + status;
    }

    public static Task fromFileString(String line) throws DataParseException {
        try {
            String[] p = line.split(",");
            return new Task(p[0], p[1], p[2], LocalDate.parse(p[3]), p[4]);
        } catch (Exception e) {
            throw new DataParseException("Failed to parse Task: " + line, e);
        }
    }

    // Getters and setters
    public String getTaskId() { return taskId; }
    public String getEmployeeId() { return employeeId; }
    public String getDescription() { return description; }
    public LocalDate getDueDate() { return dueDate; }
    public String getStatus() { return status; }

    public void setDescription(String description) { this.description = description; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public void setStatus(String status) { this.status = status; }
}
