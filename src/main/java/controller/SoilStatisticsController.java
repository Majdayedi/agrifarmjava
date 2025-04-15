package controller;

import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import service.SoilDataCRUD;
import entite.SoilData;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SoilStatisticsController {
    @FXML
    private BarChart<String, Number> humidityChart;
    @FXML
    private BarChart<String, Number> phChart;
    @FXML
    private BarChart<String, Number> nutrientChart;
    @FXML
    private PieChart soilTypeChart;

    private final SoilDataCRUD soilDataCRUD;
    private int cropId;

    public SoilStatisticsController() {
        this.soilDataCRUD = new SoilDataCRUD();
    }

    public void setCropId(int cropId) {
        this.cropId = cropId;
        loadStatistics();
    }

    private void loadStatistics() {
        try {
            List<SoilData> soilDataList = soilDataCRUD.getSoilDataByCropId(cropId);
            updateHumidityChart(soilDataList);
            updatePhChart(soilDataList);
            updateNutrientChart(soilDataList);
            updateSoilTypeChart(soilDataList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateHumidityChart(List<SoilData> soilDataList) {
        humidityChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Humidity");

        for (int i = 0; i < soilDataList.size(); i++) {
            SoilData data = soilDataList.get(i);
            series.getData().add(new XYChart.Data<>(
                "Data " + (i + 1),
                data.getHumidite()
            ));
        }

        humidityChart.getData().add(series);
    }

    private void updatePhChart(List<SoilData> soilDataList) {
        phChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("pH Level");

        for (int i = 0; i < soilDataList.size(); i++) {
            SoilData data = soilDataList.get(i);
            series.getData().add(new XYChart.Data<>(
                "Data " + (i + 1),
                data.getNiveau_ph()
            ));
        }

        phChart.getData().add(series);
    }

    private void updateNutrientChart(List<SoilData> soilDataList) {
        nutrientChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Nutrient Level");

        for (int i = 0; i < soilDataList.size(); i++) {
            SoilData data = soilDataList.get(i);
            series.getData().add(new XYChart.Data<>(
                "Data " + (i + 1),
                data.getNiveau_nutriment()
            ));
        }

        nutrientChart.getData().add(series);
    }

    private void updateSoilTypeChart(List<SoilData> soilDataList) {
        soilTypeChart.getData().clear();
        
        Map<String, Long> soilTypeCounts = soilDataList.stream()
            .collect(Collectors.groupingBy(
                SoilData::getType_sol,
                Collectors.counting()
            ));

        soilTypeCounts.forEach((type, count) -> {
            soilTypeChart.getData().add(new PieChart.Data(
                type + " (" + count + ")",
                count
            ));
        });
    }
} 