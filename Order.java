package com.example.DairyFarm;
import java.time.LocalDate;

public class Order {
    private String orderId;
    private String customerId;
    private double volume;
    private double totalPrice;
    private LocalDate date;

//    contructor
    public Order(String orderId, String customerId, double volume, double totalPrice, LocalDate date) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.volume = volume;
        this.totalPrice = totalPrice;
        this.date = date;
    }

//    methods
    public double calculateBill(double pricePerLiter) {
        totalPrice = volume * pricePerLiter;
        return totalPrice;
    }

//    file work
    public String toFileString() {
        return orderId + "," + customerId + "," + volume + "," + totalPrice + "," + date;
    }

    public static Order fromFileString(String line) throws DataParseException {
        try {
            String[] p = line.split(",");
            return new Order(p[0], p[1], Double.parseDouble(p[2]), Double.parseDouble(p[3]), LocalDate.parse(p[4]));
        } catch (Exception e) {
            throw new DataParseException("Failed to parse Order: " + line, e);
        }
    }

    // Getters and setters
    public String getOrderId() { return orderId; }
    public String getCustomerId() { return customerId; }
    public double getVolume() { return volume; }
    public double getTotalPrice() { return totalPrice; }
    public LocalDate getDate() { return date; }

    public void setVolume(double volume) { this.volume = volume; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }
    public void setDate(LocalDate date) { this.date = date; }
}
