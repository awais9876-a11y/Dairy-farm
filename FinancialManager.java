package com.example.DairyFarm;
public class FinancialManager {
    private double totalSales;
    private double totalExpenses;

//    constructor
    public FinancialManager() {
        this.totalSales = 0.0;
        this.totalExpenses = 0.0;
    }

//    methods
    public void addSale(double amount) {
        totalSales += amount;
    }

    public void addExpense(double amount) {
        totalExpenses += amount;
    }

    public double getProfit() {
        return totalSales - totalExpenses;
    }

    public String toFileString() {
        return totalSales + "," + totalExpenses;
    }

//    file work
    public static FinancialManager fromFileString(String line) throws DataParseException {
        try {
            String[] p = line.split(",");
            FinancialManager fm = new FinancialManager();
            fm.totalSales = Double.parseDouble(p[0]);
            fm.totalExpenses = Double.parseDouble(p[1]);
            return fm;
        } catch (Exception e) {
            throw new DataParseException("Failed to parse FinancialManager: " + line, e);
        }
    }

    // Getters and setters
    public double getTotalSales() { return totalSales; }
    public double getTotalExpenses() { return totalExpenses; }
}
