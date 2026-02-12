package com.example.DairyFarm;
public class Customer {
    private String customerId;
    private String name;
    private String type; // regular/shop/wholesale
    private double pricePerLiter;

//    contructor
    public Customer(String customerId, String name, String type, double pricePerLiter) {
        this.customerId = customerId;
        this.name = name;
        this.type = type;
        this.pricePerLiter = pricePerLiter;
    }

//    file
    public String toFileString() {
        return customerId + "," + name + "," + type + "," + pricePerLiter;
    }

    public static Customer fromFileString(String line) throws DataParseException {
        try {
            String[] p = line.split(",");
            return new Customer(p[0], p[1], p[2], Double.parseDouble(p[3]));
        } catch (Exception e) {
            throw new DataParseException("Failed to parse Customer: " + line, e);
        }
    }

    // Getters and setters
    public String getCustomerId() { return customerId; }
    public String getName() { return name; }
    public String getType() { return type; }
    public double getPricePerLiter() { return pricePerLiter; }

    public void setName(String name) { this.name = name; }
    public void setType(String type) { this.type = type; }
    public void setPricePerLiter(double pricePerLiter) { this.pricePerLiter = pricePerLiter; }
}
