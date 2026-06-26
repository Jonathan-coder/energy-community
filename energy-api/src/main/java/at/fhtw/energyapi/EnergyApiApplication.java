package at.fhtw.energyapi;

import at.fhtw.energypersistence.config.PersistenceConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(PersistenceConfig.class)
public class EnergyApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(EnergyApiApplication.class, args);
	}

}
