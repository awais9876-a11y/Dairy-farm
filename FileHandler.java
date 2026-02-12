package com.example.DairyFarm;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileHandler {

    // Read file
    public static List<String> readFile(String path) {
        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                lines.add(line);

            }
        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + path + ". Creating new file.");
            try {
                boolean fileCreated = new File(path).createNewFile();
                if (fileCreated) {
                    System.out.println("File created successfully");
                }
            } catch (IOException ioException) {
                ioException.printStackTrace(System.err);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lines;
    }

    // Write file
    public static void writeFile(String path, List<String> data) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(path))) {
            for (String line : data) {
                bw.write(line);
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
