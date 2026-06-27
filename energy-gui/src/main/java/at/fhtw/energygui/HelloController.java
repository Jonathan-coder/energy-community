package at.fhtw.energygui;

import at.fhtw.energycontract.CurrentPercentage;
import at.fhtw.energycontract.HourlyUsage;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
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
    @FXML private TableColumn<HourlyUsageRow, String> colProduced;
    @FXML private TableColumn<HourlyUsageRow, String> colUsed;
    @FXML private TableColumn<HourlyUsageRow, String> colGrid;

    @FXML
    public void initialize() {
        colHour.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getPeriod()));
        colProduced.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getCommunityProduced()));
        colUsed.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getCommunityUsed()));
        colGrid.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getGridUsed()));

        startDatePicker.setValue(LocalDate.now().minusDays(7));
        endDatePicker.setValue(LocalDate.now());

        loadCurrent();
        labelStatus.setText("Select a date range and press Load Historical.");
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
            if (startDatePicker.getValue() == null || endDatePicker.getValue() == null) {
                throw new IllegalStateException("Please choose both start and end dates.");
            }
            var today = LocalDate.now();
            if (startDatePicker.getValue().isAfter(today) || endDatePicker.getValue().isAfter(today)) {
                throw new IllegalStateException("Das Datum liegt in der Zukunft.");
            }
            if (startDatePicker.getValue().isAfter(endDatePicker.getValue())) {
                throw new IllegalStateException("Start date must not be after end date.");
            }

            tableHistorical.getItems().clear();
            var start = startDatePicker.getValue().atStartOfDay();
            var end = endDatePicker.getValue().atTime(23, 59, 59);
            var url = API_BASE + "/historical?start=" + start.format(FORMATTER) + "&end=" + end.format(FORMATTER);
            var request = HttpRequest.newBuilder().uri(URI.create(url)).build();
            var response = http.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 404) {
                throw new IllegalStateException("No historical data in the selected range.");
            }
            if (response.statusCode() >= 400) {
                throw new IllegalStateException(readError(response.body()));
            }

            List<HourlyUsage> usages = mapper.readValue(response.body(), new TypeReference<>() { });
            if (usages.isEmpty()) {
                throw new IllegalStateException("No historical data in the selected range.");
            }

            var aggregatedRow = new HourlyUsageRow(
                    startDatePicker.getValue() + " to " + endDatePicker.getValue(),
                    formatKwh(usages.stream().mapToDouble(HourlyUsage::communityProduced).sum()),
                    formatKwh(usages.stream().mapToDouble(HourlyUsage::communityUsed).sum()),
                    formatKwh(usages.stream().mapToDouble(HourlyUsage::gridUsed).sum())
            );
            tableHistorical.setItems(FXCollections.observableArrayList(aggregatedRow));
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

    private String formatKwh(double value) {
        return String.format(Locale.US, "%.3f", value);
    }
}
