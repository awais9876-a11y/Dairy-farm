package com.example.DairyFarm;
import java.util.ArrayList;

public class Animals {

    private String animalId;
    private String breed;
    private String health;
    private int age;
    private double dailyMilkLimit;
    public static ArrayList<Double> milkHistoryPerDay = new ArrayList<>();

//    constructor
    public Animals(String animalId, String breed, String health, int age, double dailyMilkLimit) {
        this.animalId = animalId;
        this.breed = breed;
        this.health = health;
        this.age = age;
        this.dailyMilkLimit = dailyMilkLimit;
    }

//    methods
    void milkRecord(double amount) {
        milkHistoryPerDay.add(amount);
    }

    double getTodayMilk() {
        if (milkHistoryPerDay.isEmpty()) return 0;
        return milkHistoryPerDay.get(milkHistoryPerDay.size() - 1);
    }


//    file work
    String toFileString() {
        return animalId + "," + breed + "," + health + "," + age + "," + dailyMilkLimit;
    }

    static Animals fromFileString(String line) throws DataParseException {
        try {
            String[] p = line.split(",");
            String id = p[0];
            String breed = p[1];
            String health = p[2];
            int age = Integer.parseInt(p[3]);
            double limit = Double.parseDouble(p[4]);

            return new Animals(id, breed, health, age, limit);

        } catch (Exception ex) {
            throw new DataParseException("FAILED TO PARSE: " + line, ex);
        }
    }

    public String getAnimalId() {
        return animalId;
    }

//    getter and setter
    public String getBreed() { return breed; }
    public String getHealth() { return health; }
    public int getAge() { return age; }
    public double getDailyMilkLimit() { return dailyMilkLimit; }

    public void setBreed(String breed) { this.breed = breed; }
    public void setHealth(String health) { this.health = health; }
    public void setAge(int age) { this.age = age; }
    public void setDailyMilkLimit(double dailyMilkLimit) { this.dailyMilkLimit = dailyMilkLimit; }
}
