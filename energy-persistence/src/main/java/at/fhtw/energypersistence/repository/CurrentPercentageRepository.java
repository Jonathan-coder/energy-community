package at.fhtw.energypersistence.repository;

import at.fhtw.energypersistence.model.CurrentPercentageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CurrentPercentageRepository extends JpaRepository<CurrentPercentageEntity, Long> {
    Optional<CurrentPercentageEntity> findByHour(String hour);
    Optional<CurrentPercentageEntity> findTopByOrderByHourDesc();
}
