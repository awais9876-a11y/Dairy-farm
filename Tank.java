package com.example.DairyFarm;
public class Tank {

    private String tankId;
    private double capacity;
    private double currentVolume;

//    contructor
    public Tank(String tankId, double capacity, double currentVolume) {
        this.tankId = tankId;
        this.capacity = capacity;
        this.currentVolume = currentVolume;
    }

//    methods
    boolean addMilk(double volume) throws InsufficientCapacityException {
        if (currentVolume + volume <= capacity) {
            currentVolume += volume;
            return true;
        }
        throw new InsufficientCapacityException("Tank full: " + tankId);
    }

    boolean removeMilk(double volume) {
        if (currentVolume >= volume) {
            currentVolume -= volume;
            return true;
        }
        return false;
    }

//    file work
    String toFileString() {
        return tankId + "," + capacity + "," + currentVolume;
    }

    static Tank fromFileString(String line) throws DataParseException {
        try {
            String[] p = line.split(",");
            return new Tank(p[0], Double.parseDouble(p[1]), Double.parseDouble(p[2]));
        } catch (Exception ex) {
            throw new DataParseException("FAILED AT: " + line, ex);
        }
    }

//    getter & setter
    public String getTankId() { return tankId; }
    public double getCapacity() { return capacity; }
    public double getCurrentVolume() { return currentVolume; }

    public void setCurrentVolume(double currentVolume) { this.currentVolume = currentVolume; }
}
