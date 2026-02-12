package com.example.DairyFarm;
import java.time.LocalDate;

public class MilkBatch {
    private String batchId;
    private String animalId;
    private double volume;
    private LocalDate date;

//    constructor
    public MilkBatch(String batchId, String animalId, double volume, LocalDate date) {
        this.batchId = batchId;
        this.animalId = animalId;
        this.volume = volume;
        this.date = date;
    }

//    file work
    String toFileString() {
        return batchId + "," + animalId + "," + volume + "," + date;
    }

    static MilkBatch fromFileString(String line) throws DataParseException {

        try {
            String[] p = line.split(",");
            return new MilkBatch(p[0], p[1], Double.parseDouble(p[2]), LocalDate.parse(p[3]));
        } catch (Exception e) {
            throw new DataParseException("FAILED AT: " + line, e);
        }
    }

//    getter & setter
    public String getBatchId() { return batchId; }
    public String getAnimalId() { return animalId; }
    public double getVolume() { return volume; }
    public LocalDate getDate() { return date; }
}
