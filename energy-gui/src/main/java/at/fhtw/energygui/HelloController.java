package at.fhtw.energygui;

import at.fhtw.energycontract.CurrentPercentage;
import at.fhtw.energycontract.HourlyUsage;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class HelloController {
    private static final String API_BASE = "http://localhost:8080/energy";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final HttpClient http = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    @FXML private Label labelCurrent;
    @FXML private Label labelStatus;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
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

        startDatePicker.setValue(LocalDate.now().minusDays(7));
        endDatePicker.setValue(LocalDate.now());

        loadCurrent();
        loadHistorical();
    }

    @FXML
    private void loadCurrent() {
        try {
            var request = HttpRequest.newBuilder()
                    .uri(URI.create(API_BASE + "/current"))
                    .build();
            var response = http.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                throw new IllegalStateException(readError(response.body()));
            }
            var current = mapper.readValue(response.body(), CurrentPercentage.class);
            labelCurrent.setText(String.format(
                    "Hour: %s%nCommunity depleted: %.2f%%%nGrid portion: %.2f%%",
                    current.hour(),
                    current.communityDepleted(),
                    current.gridPortion()
            ));
            labelStatus.setText("Current values loaded successfully.");
        } catch (Exception e) {
            labelCurrent.setText("No current data available.");
            labelStatus.setText("Unable to load current data: " + e.getMessage());
        }
    }

    @FXML
    private void loadHistorical() {
        try {
            var start = startDatePicker.getValue().atStartOfDay();
            var end = endDatePicker.getValue().atTime(23, 59, 59);
            var url = API_BASE + "/historical?start=" + start.format(FORMATTER) + "&end=" + end.format(FORMATTER);
            var request = HttpRequest.newBuilder().uri(URI.create(url)).build();
            var response = http.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                throw new IllegalStateException(readError(response.body()));
            }

            List<HourlyUsage> usages = mapper.readValue(response.body(), new TypeReference<>() { });
            var rows = FXCollections.<HourlyUsageRow>observableArrayList();
            for (HourlyUsage usage : usages) {
                rows.add(new HourlyUsageRow(
                        usage.hour(),
                        usage.communityProduced(),
                        usage.communityUsed(),
                        usage.gridUsed()
                ));
            }
            tableHistorical.setItems(rows);
            labelStatus.setText("Historical data loaded successfully.");
        } catch (Exception e) {
            tableHistorical.getItems().clear();
            labelStatus.setText("Unable to load historical data: " + e.getMessage());
            new Alert(Alert.AlertType.ERROR, "Unable to load historical data.\n" + e.getMessage()).showAndWait();
        }
    }

    private String readError(String body) {
        return body == null || body.isBlank() ? "The API returned an error response." : body;
    }
}
