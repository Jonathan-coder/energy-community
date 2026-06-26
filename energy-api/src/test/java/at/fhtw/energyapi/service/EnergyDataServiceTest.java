package at.fhtw.energyapi.service;

import at.fhtw.energypersistence.model.CurrentPercentageEntity;
import at.fhtw.energypersistence.model.HourlyUsageEntity;
import at.fhtw.energypersistence.repository.CurrentPercentageRepository;
import at.fhtw.energypersistence.repository.HourlyUsageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnergyDataServiceTest {

    @Mock
    private HourlyUsageRepository hourlyUsageRepository;

    @Mock
    private CurrentPercentageRepository currentPercentageRepository;

    @InjectMocks
    private EnergyDataService service;

    @Test
    void returnsLatestCurrentPercentageFromRepository() {
        when(currentPercentageRepository.findTopByOrderByHourDesc()).thenReturn(java.util.Optional.of(
                new CurrentPercentageEntity("2025-01-10T10:00:00", 55.0, 20.0)
        ));

        var current = service.getCurrentPercentage();

        assertEquals("2025-01-10T10:00:00", current.hour());
        assertEquals(55.0, current.communityDepleted());
        assertEquals(20.0, current.gridPortion());
    }

    @Test
    void returnsHistoricalRowsForRequestedRange() {
        when(hourlyUsageRepository.findByHourBetweenOrderByHourDesc("2025-01-10T09:00:00", "2025-01-10T10:00:00")).thenReturn(List.of(
                new HourlyUsageEntity("2025-01-10T09:00:00", 100.0, 80.0, 15.0),
                new HourlyUsageEntity("2025-01-10T10:00:00", 120.0, 90.0, 12.0)
        ));

        var rows = service.getHistorical(
                LocalDateTime.parse("2025-01-10T09:00:00"),
                LocalDateTime.parse("2025-01-10T10:00:00")
        );

        assertEquals(2, rows.size());
        assertEquals("2025-01-10T10:00:00", rows.get(0).hour());
        assertEquals("2025-01-10T09:00:00", rows.get(1).hour());
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
