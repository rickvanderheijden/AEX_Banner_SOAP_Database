package linechartsclient;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class AEXLineChartClient extends Application {
    private Connection connection = null;
    private final Map<String, List<Pair<Timestamp, Double>>> fondsen = new HashMap<>();
    private final Map<String, XYChart.Series> series = new HashMap<>();
    private LineChart<String, Number> lineChart;
    private final Set<String> names = new HashSet<>();
    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy HH-mm-ss");

    @Override
    public void start(Stage stage) {
        stage.setTitle("AEX");
        final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();
        yAxis.setTickUnit(30.0);
        xAxis.setLabel("");
        xAxis.setTickLabelsVisible(false);

        Pane root = new Pane();
        ComboBox comboBoxTimePeriod = new ComboBox();
        comboBoxTimePeriod.setLayoutX(10);
        comboBoxTimePeriod.setLayoutY(10);
        comboBoxTimePeriod.getItems().addAll("1", "5", "10", "20", "30", "60");
        comboBoxTimePeriod.getSelectionModel().selectedItemProperty().addListener( (options, oldValue, newValue) -> {
                updateGraph(Integer.parseInt((String)newValue));
                xAxis.setLabel("Showing last " + newValue + " minute(s)");
            }
        );

        lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("AEX Effectenbeurs");

        root.getChildren().addAll(comboBoxTimePeriod, lineChart);

        Scene scene = new Scene(root,1280,600);

        lineChart.setLayoutX(0);
        lineChart.setLayoutY(50);
        lineChart.setMinWidth(root.getWidth());

        connectToDatabase();

        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    @Override
    public void stop() {
    }

    private void updateGraph(int rangeInMinutes) {
        if(connection != null) {

            long rangeInMillis = 60000 * rangeInMinutes;
            long currentTimeInMillis = System.currentTimeMillis();
            Timestamp startDate = new Timestamp(currentTimeInMillis);
            Timestamp endDate = new Timestamp(currentTimeInMillis - rangeInMillis);

            try {
                series.clear();
                names.clear();
                fondsen.clear();
                lineChart.getData().clear();
                ResultSet resultSet = executeQuery("SELECT * FROM EFFECTENBEURS WHERE DATEANDTIME BETWEEN '" + endDate + "' AND '" + startDate + "'");

                while (resultSet.next()) {
                    String name = resultSet.getString("NAME");
                    Double koers = resultSet.getDouble("KOERS");
                    Timestamp timestamp = resultSet.getTimestamp("DATEANDTIME");

                    if (!names.contains(name)) {
                        names.add(name);
                        fondsen.put(name, new ArrayList<>());
                        series.put(name, new XYChart.Series());
                        series.get(name).setName(name);
                    }

                    fondsen.get(name).add(new Pair<>(timestamp, koers));
                }

                for (String name : names) {
                    for (Pair<Timestamp, Double> values : fondsen.get(name)) {
                        String value = dateFormatter.format(values.getKey().getTime());

                        //Platform.runLater(() -> {
                            series.get(name).getData().add(new XYChart.Data(value, values.getValue()));
                        //});
                    }

                    //Platform.runLater(() -> {
                        if (!lineChart.getData().contains(series.get(name))) {
                            lineChart.getData().add(series.get(name));
                        }
                    //});
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void connectToDatabase() {
        try {
            Class.forName("org.apache.derby.jdbc.ClientDriver").newInstance();
            connection = DriverManager.getConnection("jdbc:derby://localhost:1527/effectenbeurs");
        } catch (Exception e) {
            System.out.println("Unable to connect to database");
        }

    }

    private ResultSet executeQuery(String query) {
        ResultSet resultSet = null;

        if (connection != null) {
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                if (query != null) {
                    resultSet = preparedStatement.executeQuery();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return resultSet;
    }

    public static void main(String[] args) {
        launch(args);
    }
}