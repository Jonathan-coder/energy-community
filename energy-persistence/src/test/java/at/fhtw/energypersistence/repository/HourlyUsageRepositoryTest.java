package at.fhtw.energypersistence.repository;

import at.fhtw.energypersistence.model.HourlyUsageEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Import(at.fhtw.energypersistence.config.PersistenceConfig.class)
class HourlyUsageRepositoryTest {

    @Autowired
    private HourlyUsageRepository repository;

    @Test
    void savesAndLoadsHourlyUsage() {
        var entity = repository.save(new HourlyUsageEntity("2026-06-26T10:00:00", 18.0, 16.0, 2.0));

        var persisted = repository.findByHour("2026-06-26T10:00:00");

        assertTrue(persisted.isPresent());
        assertEquals(16.0, persisted.get().getCommunityUsed());
        assertEquals(2.0, persisted.get().getGridUsed());
    }
}
