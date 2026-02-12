package com.example.DairyFarm;
public class FeedItem {
    private String feedName;
    private double quantityKg;
    private double dailyConsumptionRate;

//    constructor
    public FeedItem(String feedName, double quantityKg, double dailyConsumptionRate) {
        this.feedName = feedName;
        this.quantityKg = quantityKg;
        this.dailyConsumptionRate = dailyConsumptionRate;
    }

//    methods
    public void useFeed(double kg) {
        if (kg <= quantityKg) {
            quantityKg -= kg;
        } else {
            System.out.println("Not enough feed: " + feedName);
        }
    }

    public void addFeed(double kg) {
        quantityKg += kg;
    }

//    file work
    public String toFileString() {
        return feedName + "," + quantityKg + "," + dailyConsumptionRate;
    }

    public static FeedItem fromFileString(String line) throws DataParseException {
        try {
            String[] p = line.split(",");
            return new FeedItem(p[0], Double.parseDouble(p[1]), Double.parseDouble(p[2]));
        } catch (Exception e) {
            throw new DataParseException("Failed to parse FeedItem: " + line, e);
        }
    }

    // Getters and setters
    public String getFeedName() { return feedName; }
    public double getQuantityKg() { return quantityKg; }
    public double getDailyConsumptionRate() { return dailyConsumptionRate; }

    public void setFeedName(String feedName) { this.feedName = feedName; }
    public void setQuantityKg(double quantityKg) { this.quantityKg = quantityKg; }
    public void setDailyConsumptionRate(double dailyConsumptionRate) { this.dailyConsumptionRate = dailyConsumptionRate; }
}
