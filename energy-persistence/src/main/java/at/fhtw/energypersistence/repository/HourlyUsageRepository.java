package at.fhtw.energypersistence.repository;

import at.fhtw.energypersistence.model.HourlyUsageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HourlyUsageRepository extends JpaRepository<HourlyUsageEntity, Long> {
    Optional<HourlyUsageEntity> findByHour(String hour);
}
