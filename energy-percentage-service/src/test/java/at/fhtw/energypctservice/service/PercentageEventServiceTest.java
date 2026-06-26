package at.fhtw.energypctservice.service;

import at.fhtw.energycommon.EnergyCalculationService;
import at.fhtw.energycontract.EnergyMessage;
import at.fhtw.energycontract.EnergyMessageType;
import at.fhtw.energypersistence.model.CurrentPercentageEntity;
import at.fhtw.energypersistence.model.HourlyUsageEntity;
import at.fhtw.energypersistence.repository.CurrentPercentageRepository;
import at.fhtw.energypersistence.repository.HourlyUsageRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PercentageEventServiceTest {

    @Test
    void savesCalculatedPercentagesForHourlyUsage() {
        CurrentPercentageRepository currentRepository = mock(CurrentPercentageRepository.class);
        HourlyUsageRepository hourlyRepository = mock(HourlyUsageRepository.class);
        when(hourlyRepository.findByHour("2026-06-26T10")).thenReturn(Optional.of(new HourlyUsageEntity("2026-06-26T10", 10.0, 8.0, 2.0)));

        PercentageEventService service = new PercentageEventService(currentRepository, hourlyRepository, new EnergyCalculationService());

        service.processUpdate(new EnergyMessage(EnergyMessageType.UPDATE, null, 0.0, "2026-06-26T10:30:00"));

        verify(currentRepository).save(any(CurrentPercentageEntity.class));
    }

    @Test
    void ignoresNonUpdateMessages() {
        CurrentPercentageRepository currentRepository = mock(CurrentPercentageRepository.class);
        HourlyUsageRepository hourlyRepository = mock(HourlyUsageRepository.class);

        PercentageEventService service = new PercentageEventService(currentRepository, hourlyRepository, new EnergyCalculationService());

        service.processUpdate(new EnergyMessage(EnergyMessageType.PRODUCER, null, 1.0, "2026-06-26T10:30:00"));

        verify(currentRepository, org.mockito.Mockito.never()).save(any(CurrentPercentageEntity.class));
    }
}
