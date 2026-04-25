package at.fhtw.energygui;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class HelloController {
    private static final String API_BASE = "http://localhost:8080/energy";
    private final HttpClient http = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    @FXML private Label labelCurrent;
    @FXML private TextField fieldStart;
    @FXML private TextField fieldEnd;
    @FXML private TableView<HourlyUsageRow> tableHistorical;
    @FXML private TableColumn<HourlyUsageRow, String> colHour;
    @FXML private TableColumn<HourlyUsageRow, Double> colProduced;
    @FXML private TableColumn<HourlyUsageRow, Double> colUsed;
    @FXML private TableColumn<HourlyUsageRow, Double> colGrid;

    @FXML
    public void initialize() {
        colHour.setCellValueFactory(new PropertyValueFactory<>("hour"));
        colProduced.setCellValueFactory(new PropertyValueFactory<>("communityProduced"));
        colUsed.setCellValueFactory(new PropertyValueFactory<>("communityUsed"));
        colGrid.setCellValueFactory(new PropertyValueFactory<>("gridUsed"));
    }

    @FXML
    private void loadCurrent() {
        try {
            var request = HttpRequest.newBuilder()
                    .uri(URI.create(API_BASE + "/current"))
                    .build();
            var response = http.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode node = mapper.readTree(response.body());
            labelCurrent.setText(
                    "Hour: " + node.get("hour").asText() +
                            " | Community Depleted: " + node.get("communityDepleted").asDouble() + "%" +
                            " | Grid Portion: " + node.get("gridPortion").asDouble() + "%"
            );
        } catch (Exception e) {
            labelCurrent.setText("Error: " + e.getMessage());
        }
    }

    @FXML
    private void loadHistorical() {
        try {
            String url = API_BASE + "/historical?start=" +
                    fieldStart.getText() + "&end=" + fieldEnd.getText();
            var request = HttpRequest.newBuilder().uri(URI.create(url)).build();
            var response = http.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode arr = mapper.readTree(response.body());
            var rows = FXCollections.<HourlyUsageRow>observableArrayList();
            for (JsonNode n : arr) {
                rows.add(new HourlyUsageRow(
                        n.get("hour").asText(),
                        n.get("communityProduced").asDouble(),
                        n.get("communityUsed").asDouble(),
                        n.get("gridUsed").asDouble()
                ));
            }
            tableHistorical.setItems(rows);
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Error: " + e.getMessage()).show();
        }
    }
}