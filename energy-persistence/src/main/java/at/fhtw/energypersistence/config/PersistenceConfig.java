package at.fhtw.energypersistence.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EntityScan(basePackages = "at.fhtw.energypersistence.model")
@EnableJpaRepositories(basePackages = "at.fhtw.energypersistence.repository")
public class PersistenceConfig {
}
