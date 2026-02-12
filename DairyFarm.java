package com.example.DairyFarm;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public class DairyFarm {
    // Observable lists fx
    public final ObservableList<Animals> animals = FXCollections.observableArrayList();
    public final ObservableList<Customer> customers = FXCollections.observableArrayList();
    public final ObservableList<Task> tasks = FXCollections.observableArrayList();
    public final ObservableList<Tank> tanks = FXCollections.observableArrayList();
    public final ObservableList<MilkBatch> milkbatches = FXCollections.observableArrayList();
    public final ObservableList<FeedItem> feedItems = FXCollections.observableArrayList();
    public final ObservableList<Order> orders = FXCollections.observableArrayList();
    public final ObservableList<Employee> employees = FXCollections.observableArrayList();

    private FinancialManager fileManager;

//    constructor
    public DairyFarm() {
        this.fileManager = new FinancialManager();
    }

    // Loader
    public void loadAllData() {
        try {
            animals.clear();
            FileHandler.readFile("Animal.txt").stream()
                    .map(line -> {
                        try { return Animals.fromFileString(line);
                        }
                        catch (DataParseException e) {
                            System.out.println("Skipping bad line: " + line);
                            return null; }
                    })
                    .filter(Objects::nonNull).forEach(animals::add);

            milkbatches.clear();
            FileHandler.readFile("milk.txt").stream()
                    .map(line -> {
                        try { return MilkBatch.fromFileString(line); }
                        catch (DataParseException e) { System.out.println("Skipping bad line: " + line); return null; }
                    })
                    .filter(Objects::nonNull).forEach(milkbatches::add);

            tanks.clear();
            FileHandler.readFile("tanks.txt").stream()
                    .map(line -> {
                        try { return Tank.fromFileString(line); } catch (DataParseException e) { System.out.println("Skipping bad line: " + line); return null;}
                    }).filter(Objects::nonNull).forEach(tanks::add);

            feedItems.clear();
            FileHandler.readFile("feed.txt").stream()
                    .map(line -> {
                        try { return FeedItem.fromFileString(line); } catch (DataParseException e) { System.out.println("Skipping bad line: " + line); return null;}
                    }).filter(Objects::nonNull).forEach(feedItems::add);

            customers.clear();
            FileHandler.readFile("customer.txt").stream()
                    .map(line -> {
                        try { return Customer.fromFileString(line); } catch (DataParseException e) { System.out.println("Skipping bad line: " + line); return null;}
                    }).filter(Objects::nonNull).forEach(customers::add);

            orders.clear();
            FileHandler.readFile("order.txt").stream()
                    .map(line -> {
                        try { return Order.fromFileString(line); } catch (DataParseException e) { System.out.println("Skipping bad line: " + line); return null;}
                    }).filter(Objects::nonNull).forEach(orders::add);

            employees.clear();
            FileHandler.readFile("employee.txt").stream()
                    .map(line -> {
                        try { return Employee.fromFileString(line); } catch (DataParseException e) { System.out.println("Skipping bad line: " + line); return null;}
                    }).filter(Objects::nonNull).forEach(employees::add);

            tasks.clear();
            FileHandler.readFile("task.txt").stream()
                    .map(line -> {
                        try { return Task.fromFileString(line); } catch (DataParseException e) { System.out.println("Skipping bad line: " + line); return null;}
                    }).filter(Objects::nonNull).forEach(tasks::add);

            // Load financial manager
//
                FileHandler.readFile("financial.txt").stream().findFirst().ifPresent(line -> {
                    try {
                        fileManager = FinancialManager.fromFileString(line);
                    } catch (DataParseException ignored) {}
                });
//
        } catch (Exception e) {
            throw new RuntimeException("Failed to load data", e);
        }
    }

//    save
    public void saveAllData() {
        FileHandler.writeFile("Animal.txt", animals.stream().map(Animals::toFileString).toList());
        FileHandler.writeFile("milk.txt", milkbatches.stream().map(MilkBatch::toFileString).toList());
        FileHandler.writeFile("tanks.txt", tanks.stream().map(Tank::toFileString).toList());
        FileHandler.writeFile("feed.txt", feedItems.stream().map(FeedItem::toFileString).toList());
        FileHandler.writeFile("customer.txt", customers.stream().map(Customer::toFileString).toList());
        FileHandler.writeFile("order.txt", orders.stream().map(Order::toFileString).toList());
        FileHandler.writeFile("employee.txt", employees.stream().map(Employee::toFileString).toList());
        FileHandler.writeFile("task.txt", tasks.stream().map(Task::toFileString).toList());
        // save financial manager
        FileHandler.writeFile("financial.txt", List.of(fileManager.toFileString()));
    }

    // methods
    public void addAnimal(Animals a) {
        animals.add(a);
    }

    public void removeAnimal(String id) {
        animals.removeIf(x -> x.getAnimalId().equals(id));
    }

    public void addMilkBatch(MilkBatch b) {
        milkbatches.add(b);
    }

    public void addCustomer(Customer c) {
        customers.add(c);
        }

    public void addOrder(Order o) {
        orders.add(o);
        if (fileManager == null) fileManager = new FinancialManager();
        fileManager.addSale(o.getTotalPrice());
    }

    public void addEmployee(Employee e) {
        employees.add(e);
        }

    public void assignTask(Task t) {
        tasks.add(t);
         }

    public void addTank(Tank t){
        tanks.add(t);
         }
    public void addFeed(FeedItem f){
        feedItems.add(f);
    }

    public double getTotalMilkToday() {
        LocalDate today = LocalDate.now();
        return milkbatches.stream().filter(b -> b.getDate().equals(today)).mapToDouble(MilkBatch::getVolume).sum();
    }

    public boolean hasAnimalId(String id) {
        return animals.stream().anyMatch(a -> a.getAnimalId().equalsIgnoreCase(id));
    }

//    getter and setter
    public ObservableList<Animals> getAnimals() { return animals; }
    public ObservableList<Tank> getTanks() { return tanks; }
    public ObservableList<FeedItem> getFeedItems() { return feedItems; }
    public ObservableList<Order> getOrders() { return orders; }
    public ObservableList<Employee> getEmployees() { return employees; }
    public ObservableList<Task> getTasks() { return tasks; }
    public ObservableList<Customer> getCustomers() { return customers; }
    public ObservableList<MilkBatch> getMilkbatches() { return milkbatches; }

    public FinancialManager getFinancialManager() {
        if (fileManager == null) fileManager = new FinancialManager();
        return fileManager;
    }


}
