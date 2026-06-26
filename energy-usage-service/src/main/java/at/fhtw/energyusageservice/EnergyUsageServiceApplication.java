package at.fhtw.energyusageservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"at.fhtw.energyusageservice", "at.fhtw.energypersistence", "at.fhtw.energycommon"})
public class EnergyUsageServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(EnergyUsageServiceApplication.class, args);
    }
}
