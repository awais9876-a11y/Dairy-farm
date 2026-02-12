package com.example.DairyFarm;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class MainApp extends Application {

    protected static DairyFarm model = new DairyFarm();

    private BarChart<String, Number> milkChart;  // Add this
    private PieChart tankPie;

    // Observable lists 
    private ObservableList<Animals> animalsObs;
    private ObservableList<Tank> tanksObs;
    private ObservableList<FeedItem> feedObs;
    private ObservableList<Order> ordersObs;
    private ObservableList<Employee> employeesObs;
    private ObservableList<Task> tasksObs;
    private ObservableList<Customer> customersObs;

    private Label kpiTotalMilk = new Label("-");
    private Label kpiAnimals = new Label("-");
    private Label kpiProfit = new Label("-");

    Label totalTodayLabel;
    Label totalWeekLabel;
    Label avgDailyLabel ;

    private Label finSales = new Label("Total Sales: Rs. 0.00");
    private Label finExpense = new Label("Total Expenses: Rs. 0.00");
    private Label finProfit = new Label("Profit: Rs. 0.00");

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Dairy Farm Management");

        TabPane tabs = new TabPane();
        tabs.getTabs().addAll(
                createDashboardTab(),
                createAnimalsTab(),
                createTanksTab(),
                createFeedTab(),
                createMilkTab(),
                createOrdersTab(),
                createEmployeesTasksTab(),
                createCustomersTab(),
                createFinancialsTab()
        );

        BorderPane root = new BorderPane(tabs);
        root.setTop(createTopBar());

        Scene scene = new Scene(root, 1000, 700);
        primaryStage.setScene(scene);
        primaryStage.show();


        // initial load
        safeLoad();
    }

    // Top bar 
    private HBox createTopBar() {
        Button loadBtn = new Button("Load");
        Button saveBtn = new Button("Save All");
        Button refreshKPIs = new Button("Refresh Dashboard");

        loadBtn.setOnAction(e -> {
            safeLoad();
            showInfo("Data loaded.");
        });

        saveBtn.setOnAction(e -> {
            safeSave();
            showInfo("Data saved.");
            refreshAll();
        });

        refreshKPIs.setOnAction(
                e -> refreshAll());

        HBox box = new HBox(10, loadBtn, saveBtn, refreshKPIs);
        box.setPadding(new Insets(8));
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    // Dashboard Tab 
    private Tab createDashboardTab() {
        Tab t = new Tab("Dashboard");
        t.setClosable(false);

        // KPI tiles
        VBox kpis = new VBox(8,
                mkKpiTile("Total Milk Today (L)", kpiTotalMilk),
                mkKpiTile("Number of Animals", kpiAnimals),
                mkKpiTile("Profit", kpiProfit)
        );

        kpis.setPadding(new Insets(8));

            // Charts 
            this.milkChart = createMilkBarChart();
            this.tankPie = createTankUsagePie();
            

        HBox charts = new HBox(12, milkChart, tankPie);
        charts.setPadding(new Insets(8));
        charts.setPrefHeight(420);
        VBox content = new VBox(12, kpis, charts);
        content.setPadding(new Insets(12));

        t.setContent(content);
        return t;
    }

    private VBox mkKpiTile(String title, Label value) {
        Label lTitle = new Label(title);
        lTitle.setStyle("-fx-font-weight: bold;");
        value.setStyle("-fx-font-size: 18px;");

        VBox box = new VBox(4, lTitle, value);
        box.setPadding(new Insets(8));
        box.setStyle("-fx-border-color: #ddd; -fx-border-radius: 6; -fx-background-radius: 6; -fx-background-color: #fafafa;");
        box.setPrefWidth(240);
        return box;
    }

    private BarChart<String, Number> createMilkBarChart() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Date");
        yAxis.setLabel("Total Milk (L)");

        BarChart<String, Number> bc = new BarChart<>(xAxis, yAxis);
        bc.setTitle("Last 7 days milk production");
        bc.setLegendVisible(false);
        bc.setAnimated(false);
        bc.setPrefWidth(640);

        refreshMilkChart(bc);
        return bc;
    }

    private void refreshMilkChart(BarChart<String, Number> bc) {
        bc.getData().clear();
        Map<LocalDate, Double> byDate = model.getMilkbatches().stream()
                .collect(Collectors.groupingBy(MilkBatch::getDate, Collectors.summingDouble(MilkBatch::getVolume)));

        LocalDate today = LocalDate.now();
        XYChart.Series<String, Number> s = new XYChart.Series<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            double v = byDate.getOrDefault(d, model.getTotalMilkToday());
            s.getData().add(new XYChart.Data<>(d.toString(), v));
        }
        bc.getData().add(s);
    }

    private PieChart createTankUsagePie() {
        PieChart pie = new PieChart();
        pie.setTitle("Tank usage");
        refreshTankPie(pie);
        pie.setPrefWidth(300);
        return pie;
    }

    private void refreshTankPie(PieChart pie) {
        pie.getData().clear();
        for (Tank t : model.getTanks()) {
            double used = t.getCurrentVolume();
            double cap = t.getCapacity();
            pie.getData().add(new PieChart.Data(t.getTankId() + " (" + String.format("%.0f/%.0f", used, cap) + ")", used));

        }
    }

//    Animals Tab
    private Tab createAnimalsTab() {
        Tab t = new Tab("Animals");
        t.setClosable(false);

        TableView<Animals> table = new TableView<>();
        TableColumn<Animals, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("animalId"));
        TableColumn<Animals, String> breedCol = new TableColumn<>("Breed");
        breedCol.setCellValueFactory(new PropertyValueFactory<>("breed"));
        TableColumn<Animals, String> healthCol = new TableColumn<>("Health");
        healthCol.setCellValueFactory(new PropertyValueFactory<>("health"));
        TableColumn<Animals, Integer> ageCol = new TableColumn<>("Age");
        ageCol.setCellValueFactory(new PropertyValueFactory<>("age"));
        TableColumn<Animals, Double> dailyCol = new TableColumn<>("Daily Limit");
        dailyCol.setCellValueFactory(new PropertyValueFactory<>("dailyMilkLimit"));

        table.getColumns().addAll(idCol, breedCol, healthCol, ageCol, dailyCol);

        // load obs
        table.setItems(model.getAnimals());

        // recheck
        ObservableList<Animals> itemsUsed = model.getAnimals(); // recommended
        table.setItems(itemsUsed);

//        System.out.println("Table bound to list instance: " + itemsUsed);
//        System.out.println("Model animals size at bind: " + itemsUsed.size());

        Button add = new Button("Add");
        Button edit = new Button("Edit");
        Button remove = new Button("Remove");

        add.setOnAction(e -> {
            Optional<Animals> opt = showAnimalDialog(null);
            opt.ifPresent(a -> {
                if (model.hasAnimalId(a.getAnimalId())) {
                    showAlert(Alert.AlertType.ERROR, "Duplicate ID", "Animal ID already exists.");
                    return;
                }

                animalsObs.add(a);
                model.addAnimal(a);
                refreshAll();
            });
        });

        edit.setOnAction(e -> {
            Animals sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { showAlert(Alert.AlertType.INFORMATION, "Select", "Select an animal to edit.");
                return;
            }
            Optional<Animals> opt = showAnimalDialog(sel);
            opt.ifPresent(updated -> {
                int idx = animalsObs.indexOf(sel);
                animalsObs.set(idx, updated);
                model.removeAnimal(sel.getAnimalId());
                model.addAnimal(updated);
                refreshAll();
            });
        });

        remove.setOnAction(e -> {
            Animals sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { showAlert(Alert.AlertType.INFORMATION, "Select", "Select an animal to remove."); return; }
            model.removeAnimal(sel.getAnimalId());
            animalsObs.remove(sel);
            refreshAll();
        });

        HBox controls = new HBox(8, add, edit, remove);
        controls.setPadding(new Insets(8));

        VBox v = new VBox(8, table, controls);
        v.setPadding(new Insets(12));
        t.setContent(v);
        return t;
    }

    private Optional<Animals> showAnimalDialog(Animals existing) {
        Dialog<Animals> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Add Animal" : "Edit Animal");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setVgap(8);
        grid.setHgap(8);
        grid.setPadding(new Insets(12));

        TextField idField = new TextField();
        TextField breedField = new TextField();
        TextField healthField = new TextField();
        TextField ageField = new TextField();
        TextField limitField = new TextField();

        if (existing != null) {
            idField.setText(existing.getAnimalId());
            idField.setDisable(true);
            breedField.setText(existing.getBreed());
            healthField.setText(existing.getHealth());
            ageField.setText(String.valueOf(existing.getAge()));
            limitField.setText(String.valueOf(existing.getDailyMilkLimit()));
        }

        grid.addRow(0, new Label("ID:"), idField);
        grid.addRow(1, new Label("Breed:"), breedField);
        grid.addRow(2, new Label("Health:"), healthField);
        grid.addRow(3, new Label("Age:"), ageField);
        grid.addRow(4, new Label("Daily Milk Limit (L):"), limitField);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    String id = idField.getText().trim();
                    String breed = breedField.getText().trim();
                    String health = healthField.getText().trim();
                    int age = Integer.parseInt(ageField.getText().trim());
                    double limit = Double.parseDouble(limitField.getText().trim());
                    return new Animals(id, breed, health, age, limit);
                } catch (Exception ex) {
                    showAlert(Alert.AlertType.ERROR, "Invalid input", ex.getMessage());
                    return null;
                }
            }
            return null;
        });

        return dialog.showAndWait();
    }

//    tank tab
    private Tab createTanksTab() {
        Tab t = new Tab("Tanks");
        t.setClosable(false);

        TableView<Tank> table = new TableView<>();
        TableColumn<Tank, String> idC = new TableColumn<>("Tank ID");
        idC.setCellValueFactory(new PropertyValueFactory<>("tankId"));
        TableColumn<Tank, Double> capC = new TableColumn<>("Capacity (L)");
        capC.setCellValueFactory(new PropertyValueFactory<>("capacity"));
        TableColumn<Tank, Double> curC = new TableColumn<>("Current Volume (L)");
        curC.setCellValueFactory(new PropertyValueFactory<>("currentVolume"));

        table.getColumns().addAll(idC, capC, curC);

        table.setItems(model.getTanks());

//        recheck
        ObservableList<Tank> itemsUsed = model.getTanks();
        table.setItems(itemsUsed);

        Button add = new Button("Add Tank");
        Button fill = new Button("Add Milk");
        Button removeMilk = new Button("Remove Milk");
        Button delete = new Button("Delete Tank");

        // Add Tank Button
        add.setOnAction(e -> {
            Dialog<Tank> d = new Dialog<>();
            d.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            GridPane gp = new GridPane();
            gp.setVgap(8); gp.setHgap(8); gp.setPadding(new Insets(12));

            TextField id = new TextField(),
            cap = new TextField(),
            cur = new TextField("0");


            gp.addRow(0, new Label("Tank ID:"), id);
            gp.addRow(1, new Label("Capacity (L):"), cap);
            gp.addRow(2, new Label("Current Volume (L):"), cur);

            d.getDialogPane().setContent(gp);
            d.setResultConverter(btn -> {
                if (btn == ButtonType.OK) {
                    try {
                        return new Tank(id.getText().trim(),
                                Double.parseDouble(cap.getText().trim()),
                                Double.parseDouble(cur.getText().trim()));

                    } catch (Exception ex) {
                        showAlert(Alert.AlertType.ERROR, "Invalid input",
                                ex.getMessage());
                        return null;
                    }
                }
                return null;
            });

            d.showAndWait().ifPresent(tank -> {
                tanksObs.add(tank);
                model.getTanks().add(tank);
                showInfo("Suceeded");
                refreshAll();
            });
        });

        // Add Milk Button
        fill.setOnAction(e -> {
            Tank sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) {
                showAlert(Alert.AlertType.INFORMATION, "Select Tank",
                        "Select a tank first");
                return;
            }

            TextInputDialog td = new TextInputDialog("10");
            td.setTitle("Add Milk to Tank");
            td.setHeaderText("Add milk to tank: " + sel.getTankId());
            td.setContentText("Enter liters to add:");

            td.showAndWait().ifPresent(txt -> {
                try {
                    double vol = Double.parseDouble(txt);
                    try {
                        if (sel.addMilk(vol)) {
                            table.refresh();
                            refreshAll();
                            showAlert(Alert.AlertType.INFORMATION, "Success",
                                    vol + "L added to " + sel.getTankId());
                        }
                    } catch (InsufficientCapacityException ex) {
                        showAlert(Alert.AlertType.WARNING, "Capacity Error",
                                ex.getMessage());
                    }
                } catch (NumberFormatException ex) {
                    showAlert(Alert.AlertType.ERROR, "Invalid Input",
                            "Please enter a valid number");
                }
            });
        });


        removeMilk.setOnAction(e -> {
            Tank sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) {
                showAlert(Alert.AlertType.INFORMATION, "Select Tank",
                        "Select a tank first");
                return;
            }

            TextInputDialog td = new TextInputDialog("10");
            td.setTitle("Remove Milk from Tank");
            td.setHeaderText("Remove milk from tank: " + sel.getTankId());
            td.setContentText("Current volume: " + sel.getCurrentVolume() + "L\n" +
                    "Enter liters to remove:");

            td.showAndWait().ifPresent(txt -> {
                try {
                    double vol = Double.parseDouble(txt);

                    if (vol < 0) {
                        showAlert(Alert.AlertType.ERROR, "Invalid Amount",
                                "Cannot remove negative amount");
                        return;
                    }

                    // Try to remove milk
                    if (sel.removeMilk(vol)) {
                        table.refresh();
                        refreshAll();
                        showAlert(Alert.AlertType.INFORMATION, "Success",
                                vol + "L removed from " + sel.getTankId() +
                                        "\nRemaining: " + sel.getCurrentVolume() + "L");
                    } else {
                        showAlert(Alert.AlertType.WARNING, "Insufficient Milk",
                                "Tank only has " + sel.getCurrentVolume() + "L\n" +
                                        "Cannot remove " + vol + "L");
                    }
                } catch (NumberFormatException ex) {
                    showAlert(Alert.AlertType.ERROR, "Invalid Input",
                            "Please enter a valid number");
                }
            });
        });

        // Delete Tank Button
        delete.setOnAction(e -> {
            Tank sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) {
                showAlert(Alert.AlertType.INFORMATION, "Select Tank",
                        "Select a tank to delete");
                return;
            }

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    "Delete tank " + sel.getTankId() + "?");
            confirm.setTitle("Confirm Delete");
            Optional<ButtonType> r = confirm.showAndWait();
            if (r.isPresent() && r.get() == ButtonType.OK) {
                model.getTanks().remove(sel);
                tanksObs.remove(sel);
                refreshAll();
                showAlert(Alert.AlertType.INFORMATION, "Deleted",
                        sel.getTankId() + " has been deleted");
            }
        });

        HBox controls = new HBox(8, add, fill, removeMilk, delete);
        controls.setPadding(new Insets(8));
        controls.setStyle("-fx-border-color: #ddd; -fx-border-radius: 4; " +
                "-fx-background-color: #f9f9f9;");

        VBox v = new VBox(8, table, controls);
        v.setPadding(new Insets(12));
        t.setContent(v);
        return t;
    }

    // Feed Tab 
    private Tab createFeedTab() {
        Tab t = new Tab("Feed");
        t.setClosable(false);

        TableView<FeedItem> table = new TableView<>();
        TableColumn<FeedItem, String> nameC = new TableColumn<>("Feed");
        nameC.setCellValueFactory(new PropertyValueFactory<>("feedName"));
        TableColumn<FeedItem, Double> qtyC = new TableColumn<>("Quantity (Kg)");
        qtyC.setCellValueFactory(new PropertyValueFactory<>("quantityKg"));
        TableColumn<FeedItem, Double> rateC = new TableColumn<>("Daily Rate (Kg)");
        rateC.setCellValueFactory(new PropertyValueFactory<>("dailyConsumptionRate"));

         table.getColumns().addAll(nameC, qtyC, rateC);

        table.setItems(model.getFeedItems());

//        recheck
        ObservableList<FeedItem> itemsUsed = model.getFeedItems(); // recommended
        table.setItems(itemsUsed);

        Button add = new Button("Add");
        Button use = new Button("Use (kg)");
        Button restock = new Button("Restock");

        add.setOnAction(e -> {
            Dialog<FeedItem> d = new Dialog<>();
            d.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            GridPane gp = new GridPane();
            gp.setVgap(8);
            gp.setHgap(8);
            gp.setPadding(new Insets(12));

            TextField name = new TextField(),
                    qty = new TextField("0"),
                    rate = new TextField("0");

            gp.addRow(0, new Label("Name:"), name);
            gp.addRow(1, new Label("Quantity (Kg):"), qty);
            gp.addRow(2, new Label("Daily Rate (Kg):"), rate);

            d.getDialogPane().setContent(gp);
            d.setResultConverter(btn -> {
                if (btn == ButtonType.OK) {
                    try {
                        return new FeedItem(name.getText().trim(), Double.parseDouble(qty.getText().trim()), Double.parseDouble(rate.getText().trim()));
                    } catch (Exception ex) { showAlert(Alert.AlertType.ERROR, "Invalid", ex.getMessage()); return null; }
                }
                return null;
            });

            d.showAndWait().ifPresent(fi -> {
                feedObs.add(fi);
               model.getFeedItems().add(fi); });
        });

        use.setOnAction(e -> {
            FeedItem sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) {
                showAlert(Alert.AlertType.INFORMATION, "Select", "Select feed");
                return;
            }

            TextInputDialog td = new TextInputDialog("0");
            td.setHeaderText("Use kg from " + sel.getFeedName());
            td.showAndWait().ifPresent(txt -> {
                try {
                    double kg = Double.parseDouble(txt);
                    sel.useFeed(kg);
                    table.refresh();
                } catch (Exception ex) {
                    showAlert(Alert.AlertType.ERROR, "Invalid", "Enter numeric value");
                }
            });
        });

        restock.setOnAction(e -> {
            FeedItem sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) {
                showAlert(Alert.AlertType.INFORMATION, "Select", "Select feed");
                return;
            }

            TextInputDialog td = new TextInputDialog("0");
            td.setHeaderText("Add kg to " + sel.getFeedName());
            td.showAndWait().ifPresent(txt -> {
                try {
                    double kg = Double.parseDouble(txt);
                    sel.addFeed(kg);
                    table.refresh();
                } catch (Exception ex) { showAlert(Alert.AlertType.ERROR, "Invalid", "Enter numeric value"); }
            });
        });

        HBox controls = new HBox(8, add, use, restock);
        controls.setPadding(new Insets(8));
        VBox v = new VBox(8, table, controls); v.setPadding(new Insets(12));
        t.setContent(v);
        return t;
    }

    //Milk Tab 
    private Tab createMilkTab() {
        Tab t = new Tab("Milk Production");
        t.setClosable(false);
        
        TableView<MilkBatch> table = new TableView<>();
        TableColumn<MilkBatch, String> batchIdCol = new TableColumn<>("Batch ID");
        batchIdCol.setCellValueFactory(new PropertyValueFactory<>("batchId"));

        TableColumn<MilkBatch, String> animalIdCol = new TableColumn<>("Animal ID");
        animalIdCol.setCellValueFactory(new PropertyValueFactory<>("animalId"));

        TableColumn<MilkBatch, Double> volumeCol = new TableColumn<>("Volume (L)");
        volumeCol.setCellValueFactory(new PropertyValueFactory<>("volume"));

        TableColumn<MilkBatch, LocalDate> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));

        table.getColumns().addAll(batchIdCol, animalIdCol, volumeCol, dateCol);

        table.setItems(model.getMilkbatches());

       
         totalTodayLabel = new Label("Today: 0.00 L");
        totalWeekLabel = new Label("This Week: 0.00 L");
        avgDailyLabel = new Label("Avg Daily: 0.00 L");

        
        Button addBtn = new Button("Add Milk Batch");
        Button editBtn = new Button("Edit");
        Button deleteBtn = new Button("Delete");
        Button recordTodayBtn = new Button("Record Today's Collection");

       
        addBtn.setOnAction(e -> {
            Optional<MilkBatch> opt = showMilkBatchDialog(null);
            opt.ifPresent(batch -> {
                model.addMilkBatch(batch);
                table.refresh();
                updateMilkStats(totalTodayLabel, totalWeekLabel, avgDailyLabel);
                refreshAll();
            });
        });

      
        editBtn.setOnAction(e -> {
            MilkBatch sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) {
                showAlert(Alert.AlertType.INFORMATION, "Select", "Select a milk batch to edit.");
                return;
            }
            Optional<MilkBatch> opt = showMilkBatchDialog(sel);
            opt.ifPresent(updated -> {
                int idx = model.getMilkbatches().indexOf(sel);
                model.getMilkbatches().set(idx, updated);
                table.refresh();
                updateMilkStats(totalTodayLabel, totalWeekLabel, avgDailyLabel);
                refreshAll();
            });
        });

        
        deleteBtn.setOnAction(e -> {
            MilkBatch sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) {
                showAlert(Alert.AlertType.INFORMATION, "Select", "Select a batch to delete.");
                return;
            }
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    "Delete batch " + sel.getBatchId() + "?");
            Optional<ButtonType> r = confirm.showAndWait();
            if (r.isPresent() && r.get() == ButtonType.OK) {
                model.getMilkbatches().remove(sel);
                table.refresh();
                updateMilkStats(totalTodayLabel, totalWeekLabel, avgDailyLabel);
                refreshAll();
            }
        });

        
        recordTodayBtn.setOnAction(e -> {
            Dialog<MilkBatch> d = new Dialog<>();
            d.setTitle("Record Today's Collection");
            d.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            GridPane gp = new GridPane();
            gp.setVgap(8); gp.setHgap(8); gp.setPadding(new Insets(12));

            
            ComboBox<String> animalBox = new ComboBox<>();
            model.getAnimals().forEach(a -> animalBox.getItems().add(a.getAnimalId()));

            TextField batchId = new TextField();
            batchId.setPromptText("Auto-generated if empty");

            TextField volume = new TextField();
            volume.setPromptText("0.0");

            gp.addRow(0, new Label("Batch ID:"), batchId);
            gp.addRow(1, new Label("Animal ID:"), animalBox);
            gp.addRow(2, new Label("Volume (L):"), volume);

            d.getDialogPane().setContent(gp);

            d.setResultConverter(btn -> {
                if (btn == ButtonType.OK) {
                    try {
                        String bid = batchId.getText().isBlank() ?
                                "BATCH-" + System.currentTimeMillis() :
                                batchId.getText().trim();
                        String aid = animalBox.getValue();
                        double vol = Double.parseDouble(volume.getText().trim());

                        if (aid == null || aid.isBlank()) {
                            showAlert(Alert.AlertType.ERROR, "Error", "Select an animal");
                            return null;
                        }

                        return new MilkBatch(bid, aid, vol, LocalDate.now());
                    } catch (Exception ex) {
                        showAlert(Alert.AlertType.ERROR, "Invalid input", ex.getMessage());
                        return null;
                    }
                }
                return null;
            });

            d.showAndWait().ifPresent(batch -> {
                model.addMilkBatch(batch);
                table.refresh();
                updateMilkStats(totalTodayLabel, totalWeekLabel, avgDailyLabel);
                refreshAll();
            });
        });

        HBox controls = new HBox(8, addBtn, editBtn, deleteBtn, recordTodayBtn);
        controls.setPadding(new Insets(8));

        HBox stats = new HBox(20, totalTodayLabel, totalWeekLabel, avgDailyLabel);
        stats.setPadding(new Insets(12));
        stats.setStyle("-fx-border-color: #ddd; -fx-border-radius: 6; -fx-background-color: #fafafa;");

        VBox v = new VBox(8, stats, table, controls);
        v.setPadding(new Insets(12));
        t.setContent(v);

        updateMilkStats(totalTodayLabel, totalWeekLabel, avgDailyLabel);

        return t;
    }


    private Optional<MilkBatch> showMilkBatchDialog(MilkBatch existing) {
        Dialog<MilkBatch> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Add Milk Batch" : "Edit Milk Batch");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setVgap(8);
        grid.setHgap(8);
        grid.setPadding(new Insets(12));

        TextField batchIdField = new TextField();
        ComboBox<String> animalBox = new ComboBox<>();
        TextField volumeField = new TextField();
        DatePicker datePicker = new DatePicker(LocalDate.now());


        model.getAnimals().forEach(a -> animalBox.getItems().add(a.getAnimalId()));

        if (existing != null) {
            batchIdField.setText(existing.getBatchId());
            batchIdField.setDisable(true); // ID is immutable
            animalBox.setValue(existing.getAnimalId());
            volumeField.setText(String.valueOf(existing.getVolume()));
            datePicker.setValue(existing.getDate());
        }

        grid.addRow(0, new Label("Batch ID:"), batchIdField);
        grid.addRow(1, new Label("Animal ID:"), animalBox);
        grid.addRow(2, new Label("Volume (L):"), volumeField);
        grid.addRow(3, new Label("Date:"), datePicker);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    String id = batchIdField.getText().isBlank() ?
                            "BATCH-" + System.currentTimeMillis() :
                            batchIdField.getText().trim();
                    String animalId = animalBox.getValue();
                    double volume = Double.parseDouble(volumeField.getText().trim());
                    LocalDate date = datePicker.getValue();

                    if (animalId == null || animalId.isBlank()) {
                        showAlert(Alert.AlertType.ERROR, "Error", "Select an animal ID");
                        return null;
                    }
                    if (volume < 0) {
                        showAlert(Alert.AlertType.ERROR, "Error", "Volume must be non-negative");
                        return null;
                    }

                    return new MilkBatch(id, animalId, volume, date);
                } catch (Exception ex) {
                    showAlert(Alert.AlertType.ERROR, "Invalid input", ex.getMessage());
                    return null;
                }
            }
            return null;
        });

        return dialog.showAndWait();
    }

    private void updateMilkStats(Label todayLabel, Label weekLabel, Label avgLabel) {
        LocalDate today = LocalDate.now();
        LocalDate weekAgo = today.minusDays(7);

        // Today total
        double totalToday = model.getMilkbatches().stream()
                .filter(b -> b.getDate().equals(today))
                .mapToDouble(MilkBatch::getVolume)
                .sum();

        // week total
        double totalWeek = model.getMilkbatches().stream()
                .filter(b -> !b.getDate().isBefore(weekAgo) && !b.getDate().isAfter(today))
                .mapToDouble(MilkBatch::getVolume)
                .sum();

        // Average daily
        double avgDaily = totalWeek / 7.0;

        todayLabel.setText(String.format("Today: %.2f L", totalToday));
        weekLabel.setText(String.format("This Week: %.2f L", totalWeek));
        avgLabel.setText(String.format("Avg Daily: %.2f L", avgDaily));

    }

    //Orders Tab
    private Tab createOrdersTab() {
        Tab t = new Tab("Orders");
        t.setClosable(false);

        TableView<Order> table = new TableView<>();
        TableColumn<Order, String> idC = new TableColumn<>("Order ID");
        idC.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        TableColumn<Order, String> custC = new TableColumn<>("Customer ID");
        custC.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        TableColumn<Order, Double> volC = new TableColumn<>("Volume (L)");
        volC.setCellValueFactory(new PropertyValueFactory<>("volume"));
        TableColumn<Order, Double> priceC = new TableColumn<>("Total Price");
        priceC.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
        TableColumn<Order, LocalDate> dateC = new TableColumn<>("Date");
        dateC.setCellValueFactory(new PropertyValueFactory<>("date"));

        table.getColumns().addAll(idC, custC, volC, priceC, dateC);

        table.setItems(model.getOrders());

//        recheck
        ObservableList<Order> itemsUsed = model.getOrders(); // recommended
        table.setItems(itemsUsed);

        Button add = new Button("Add Order");
        Button remove = new Button("Remove Order");

        add.setOnAction(e -> {
            Dialog<Order> d = new Dialog<>();
            d.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            GridPane gp = new GridPane();
            gp.setVgap(8);
            gp.setHgap(8);
            gp.setPadding(new Insets(12));

            TextField id = new TextField(),
                    cust = new TextField(),
                    vol = new TextField("0"),
                    price = new TextField("0");

            gp.addRow(0, new Label("Order ID:"), id);
            gp.addRow(1, new Label("Customer ID:"), cust);
            gp.addRow(2, new Label("Volume (L):"), vol);
            gp.addRow(3, new Label("Total Price:"), price);
            d.getDialogPane().setContent(gp);

            d.setResultConverter(btn -> {
                if (btn == ButtonType.OK) {
                    try {
                        Order o = new Order(
                                id.getText().trim(),
                                cust.getText().trim(),
                                Double.parseDouble(vol.getText().trim()),
                                Double.parseDouble(price.getText().trim()),
                                LocalDate.now());
                        return o;
                    } catch (Exception ex) {
                        showAlert(Alert.AlertType.ERROR, "Invalid", ex.getMessage());
                        return null; }
                }
                return null;
            });
            d.showAndWait().ifPresent(o -> {
                ordersObs.add(o);
                model.getOrders().add(o);
                refreshAll();
            });
        });

        remove.setOnAction(e -> {
            Order sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { showAlert(Alert.AlertType.INFORMATION, "Select", "Select order to remove"); return; }
            model.getOrders().remove(sel);
            ordersObs.remove(sel);
            refreshAll();
        });

        HBox controls = new HBox(8, add, remove);
        controls.setPadding(new Insets(8));
        VBox v = new VBox(8, table, controls);
        v.setPadding(new Insets(12));
        t.setContent(v);
        return t;
    }

    //Employees & Tasks Tab
    private Tab createEmployeesTasksTab() {
        Tab t = new Tab("Employees & Tasks");
        t.setClosable(false);

        // Employees table
        TableView<Employee> empTable = new TableView<>();
        TableColumn<Employee, String> empId = new TableColumn<>("Employee ID");
        empId.setCellValueFactory(new PropertyValueFactory<>("employeeId"));
        TableColumn<Employee, String> empName = new TableColumn<>("Name");
        empName.setCellValueFactory(new PropertyValueFactory<>("name"));
        TableColumn<Employee, String> empRole = new TableColumn<>("Role");
        empRole.setCellValueFactory(new PropertyValueFactory<>("role"));

        empTable.getColumns().addAll(empId, empName, empRole);

        empTable.setItems(model.getEmployees());

//        recheck
        ObservableList<Employee> itemsUsed = model.getEmployees();
        empTable.setItems(itemsUsed);

        Button addEmp = new Button("Add Employee");
        Button removeEmp = new Button("Remove");

        addEmp.setOnAction(e -> {
            Dialog<Employee> d = new Dialog<>();
            d.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            GridPane gp = new GridPane();
            gp.setVgap(8);
            gp.setHgap(8);
            gp.setPadding(new Insets(12));

            TextField id = new TextField(),
                    name = new TextField(),
                    role = new TextField();

            gp.addRow(0, new Label("ID:"), id);
            gp.addRow(1, new Label("Name:"), name);
            gp.addRow(2, new Label("Role:"), role);
            d.getDialogPane().setContent(gp);

            d.setResultConverter(btn -> {
                if (btn == ButtonType.OK) {
                    return new Employee(
                            id.getText().trim(),
                            name.getText().trim(),
                            role.getText().trim());
                }
                return null;
            });
            d.showAndWait().ifPresent(emp -> {
                employeesObs.add(emp);
                model.getEmployees().add(emp); });
        });

        removeEmp.setOnAction(e -> {
            Employee sel = empTable.getSelectionModel().getSelectedItem();
            if (sel == null) { showAlert(Alert.AlertType.INFORMATION, "Select", "Select employee"); return; }
            model.getEmployees().remove(sel);
            employeesObs.remove(sel);
        });

        HBox empControls = new HBox(8, addEmp, removeEmp);
        empControls.setPadding(new Insets(8));

        // Tasks table
        TableView<Task> taskTable = new TableView<>();
        TableColumn<Task, String> tId = new TableColumn<>("Task ID");
        tId.setCellValueFactory(new PropertyValueFactory<>("taskId"));
        TableColumn<Task, String> tEmp = new TableColumn<>("Employee ID");
        tEmp.setCellValueFactory(new PropertyValueFactory<>("employeeId"));
        TableColumn<Task, String> tDesc = new TableColumn<>("Description");
        tDesc.setCellValueFactory(new PropertyValueFactory<>("description"));
        TableColumn<Task, LocalDate> tDue = new TableColumn<>("Due Date");
        tDue.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        TableColumn<Task, String> tStatus = new TableColumn<>("Status");
        tStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        taskTable.getColumns().addAll(tId, tEmp, tDesc, tDue, tStatus);

        taskTable.setItems(model.getTasks());

//        recheck
        ObservableList<Task> itemsUsed1 = model.getTasks();
        taskTable.setItems(itemsUsed1);

        Button addTask = new Button("Add Task");
        Button markDone = new Button("Mark Completed");
        Button removeTask = new Button("Remove Task");

        addTask.setOnAction(e -> {
            Dialog<Task> d = new Dialog<>();
            d.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            GridPane gp = new GridPane();
            gp.setVgap(8);
            gp.setHgap(8);
            gp.setPadding(new Insets(12));

            TextField id = new TextField(),
                    emp = new TextField(),
                    desc = new TextField();
            DatePicker dp = new DatePicker(LocalDate.now());

            gp.addRow(0, new Label("Task ID:"), id);
            gp.addRow(1, new Label("Employee ID:"), emp);
            gp.addRow(2, new Label("Description:"), desc);
            gp.addRow(3, new Label("Due Date:"), dp);
            d.getDialogPane().setContent(gp);

            d.setResultConverter(btn -> {
                if (btn == ButtonType.OK) {
                    return new Task(
                            id.getText().trim(),
                            emp.getText().trim(),
                            desc.getText().trim(),
                            dp.getValue(), "pending");
                }
                return null;
            });
            d.showAndWait().ifPresent(
                    tsk -> {
                        tasksObs.add(tsk);
                        model.getTasks().add(tsk); });
        });

        markDone.setOnAction(e -> {
            Task sel = taskTable.getSelectionModel().getSelectedItem();
            if (sel == null) { showAlert(
                    Alert.AlertType.INFORMATION, "Select", "Select task");
                return;
            }
            sel.setStatus("completed");
            taskTable.refresh();
        });

        removeTask.setOnAction(e -> {
            Task sel = taskTable.getSelectionModel().getSelectedItem();
            if (sel == null) {
                showAlert(Alert.AlertType.INFORMATION, "Select", "Select task");
                return;
            }
            model.getTasks().remove(sel);
            tasksObs.remove(sel);
        });

        HBox taskControls = new HBox(8, addTask, markDone, removeTask);
        taskControls.setPadding(new Insets(8));

        VBox empBox = new VBox(8, new Label("Employees"), empTable, empControls);
        VBox taskBox = new VBox(8, new Label("Tasks"), taskTable, taskControls);
        HBox both = new HBox(12, empBox, taskBox);
        both.setPadding(new Insets(12));
        t.setContent(both);
        return t;
    }

    //Customers Tab
    private Tab createCustomersTab() {
        Tab t = new Tab("Customers");
        t.setClosable(false);

        TableView<Customer> table = new TableView<>();
        TableColumn<Customer, String> cid = new TableColumn<>("Customer ID");
        cid.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        TableColumn<Customer, String> cname = new TableColumn<>("Name");
        cname.setCellValueFactory(new PropertyValueFactory<>("name"));
        TableColumn<Customer, String> ctype = new TableColumn<>("Type");
        ctype.setCellValueFactory(new PropertyValueFactory<>("type"));
        TableColumn<Customer, Double> price = new TableColumn<>("Price/L");
        price.setCellValueFactory(new PropertyValueFactory<>("pricePerLiter"));
        table.getColumns().addAll(cid, cname, ctype, price);


        table.setItems(model.getCustomers());

//        recheck
        ObservableList<Customer> itemsUsed = model.getCustomers(); // recommended
        table.setItems(itemsUsed);

        Button add = new Button("Add Customer");
        Button remove = new Button("Remove");

        add.setOnAction(e -> {
            Dialog<Customer> d = new Dialog<>();
            d.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            GridPane gp = new GridPane();
            gp.setVgap(8);
            gp.setHgap(8);
            gp.setPadding(new Insets(12));

            TextField id = new TextField(),
                    name = new TextField(),
                    type = new TextField(),
                    priceF = new TextField("0");

            gp.addRow(0, new Label("ID:"), id);
            gp.addRow(1, new Label("Name:"), name);
            gp.addRow(2, new Label("Type:"), type);
            gp.addRow(3, new Label("Price/L:"), priceF);
            d.getDialogPane().setContent(gp);

            d.setResultConverter(btn -> {
                if (btn == ButtonType.OK) {
                    return new Customer(
                            id.getText().trim(),
                            name.getText().trim(),
                            type.getText().trim(),
                            Double.parseDouble(priceF.getText().trim()));
                }
                return null;
            });
            d.showAndWait().ifPresent(c -> {
                customersObs.add(c);
                model.getCustomers().add(c); });
        });

        remove.setOnAction(e -> {
            Customer sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { showAlert(
                    Alert.AlertType.INFORMATION, "Select", "Select customer");
                return;
            }
            model.getCustomers().remove(sel); customersObs.remove(sel);
        });

        HBox ctrl = new HBox(8, add, remove);
        ctrl.setPadding(new Insets(8));
        VBox v = new VBox(8, table, ctrl);
        v.setPadding(new Insets(12));
        t.setContent(v);
        return t;
    }


    private Tab createFinancialsTab() {
        Tab t = new Tab("Financials");
        t.setClosable(false);


        finSales.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");
        finExpense.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");
        finProfit.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");

        Button addExpense = new Button("Add Expense");
        addExpense.setStyle("-fx-font-size: 12; -fx-padding: 8;");

        addExpense.setOnAction(e -> {
            TextInputDialog td = new TextInputDialog("0");
            td.setHeaderText("Add expense amount");

            td.showAndWait().ifPresent(txt -> {
                try {
                    double v = Double.parseDouble(txt);
                    model.getFinancialManager().addExpense(v);
                    refreshAll();
                } catch (Exception ex) {
                    showAlert(Alert.AlertType.ERROR, "Invalid", "Enter numeric");
                }
            });
        });

        // Create boxes for each financial metric
        VBox salesBox = createFinancialBox("Total Sales", finSales);
        VBox expenseBox = createFinancialBox("Total Expenses", finExpense);
        VBox profitBox = createFinancialBox("Profit", finProfit);

        HBox metricsBox = new HBox(20, salesBox, expenseBox, profitBox);
        metricsBox.setPadding(new Insets(12));
        metricsBox.setAlignment(Pos.CENTER);

        VBox v = new VBox(12, metricsBox, addExpense);
        v.setPadding(new Insets(12));
        t.setContent(v);


        updateFinancialLabels();

        return t;
    }

    private VBox createFinancialBox(String title, Label value) {
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");
        value.setStyle("-fx-font-size: 18; -fx-text-fill: #2ecc71;");

        VBox box = new VBox(8, titleLabel, value);
        box.setPadding(new Insets(12));
        box.setStyle("-fx-border-color: #ddd; -fx-border-radius: 6; " +
                "-fx-background-color: #f9f9f9;");
        box.setPrefWidth(250);
        return box;
    }


    private void updateFinancialLabels() {
        FinancialManager fm = model.getFinancialManager();
        finSales.setText("Total Sales: Rs. " +
                String.format("%.2f", fm.getTotalSales()));
        finExpense.setText("Total Expenses: Rs. " +
                String.format("%.2f", fm.getTotalExpenses()));
        finProfit.setText("Profit: Rs. " +
                String.format("%.2f", fm.getProfit()));
    }
      //  Helpers and refresh

    private void safeLoad() {
        try {
            model.loadAllData();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // reinitialize observables
        animalsObs = FXCollections.observableArrayList(model.getAnimals());
        tanksObs = FXCollections.observableArrayList(model.getTanks());
        feedObs = FXCollections.observableArrayList(model.getFeedItems());
        ordersObs = FXCollections.observableArrayList(model.getOrders());
        employeesObs = FXCollections.observableArrayList(model.getEmployees());
        tasksObs = FXCollections.observableArrayList(model.getTasks());
        customersObs = FXCollections.observableArrayList(model.getCustomers());
        refreshAll();

    }

    private void safeSave() {
        try {
            // sync model lists
            model.animals.clear();
            model.animals.addAll(new ArrayList<>(animalsObs));

            model.tanks.clear();
            model.tanks.addAll(new ArrayList<>(tanksObs));

            model.feedItems.clear();
            model.feedItems.addAll(new ArrayList<>(feedObs));

            model.orders.clear();
            model.orders.addAll(new ArrayList<>(ordersObs));

            model.employees.clear();
            model.employees.addAll(new ArrayList<>(employeesObs));

            model.tasks.clear();
            model.tasks.addAll(new ArrayList<>(tasksObs));

            model.customers.clear();
            model.customers.addAll(new ArrayList<>(customersObs));
            model.saveAllData();

            updateMilkStats(totalTodayLabel,totalWeekLabel,avgDailyLabel);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR,
                    "Save failed", e.getMessage());
        }
    }



    private void refreshAll() {
        // KPIs
        kpiTotalMilk.setText(String.format("%.2f", model.getTotalMilkToday()));
        kpiAnimals.setText(String.valueOf(model.getAnimals().size()));
        kpiProfit.setText(String.format("%.2f", model.getFinancialManager().getProfit()));

        // Refresh charts
        if (milkChart != null) refreshMilkChart(milkChart);
        if (tankPie != null) refreshTankPie(tankPie);

        updateMilkStats(totalTodayLabel,totalWeekLabel,avgDailyLabel);

        updateFinancialLabels();
    }

    private void showInfo(String text) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, text, ButtonType.OK);
        a.setHeaderText(null);
        a.showAndWait();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert a = new Alert(type, message, ButtonType.OK);
        a.setHeaderText(title);
        a.showAndWait();
    }
}
