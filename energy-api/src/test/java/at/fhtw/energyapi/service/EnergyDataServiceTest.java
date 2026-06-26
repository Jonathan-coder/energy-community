package at.fhtw.energyapi.service;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EnergyDataServiceTest {

    private final EnergyDataService service = new EnergyDataService();

    @Test
    void returnsCurrentPercentageForLatestHour() {
        var current = service.getCurrentPercentage();

        assertEquals("2025-01-10T14:00:00", current.hour());
        assertEquals(100.0, current.communityDepleted());
        assertEquals(5.63, current.gridPortion());
    }

    @Test
    void returnsHistoricalRangeDescendingByHour() {
        var rows = service.getHistorical(
                LocalDateTime.parse("2025-01-10T12:00:00"),
                LocalDateTime.parse("2025-01-10T14:00:00")
        );

        assertEquals(3, rows.size());
        assertEquals("2025-01-10T14:00:00", rows.get(0).hour());
        assertEquals("2025-01-10T12:00:00", rows.get(2).hour());
    }

    @Test
    void rejectsInvertedTimeRanges() {
        assertThrows(ResponseStatusException.class, () ->
                service.getHistorical(
                        LocalDateTime.parse("2025-01-10T14:00:00"),
                        LocalDateTime.parse("2025-01-10T12:00:00")
                )
        );
    }
}
