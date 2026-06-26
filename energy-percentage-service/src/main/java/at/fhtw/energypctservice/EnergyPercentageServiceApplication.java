package at.fhtw.energypctservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"at.fhtw.energypctservice", "at.fhtw.energypersistence", "at.fhtw.energycommon"})
public class EnergyPercentageServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(EnergyPercentageServiceApplication.class, args);
    }
}
