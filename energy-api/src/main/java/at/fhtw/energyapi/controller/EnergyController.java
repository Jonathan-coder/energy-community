package at.fhtw.energyapi.controller;

import at.fhtw.energyapi.model.CurrentPercentage;
import at.fhtw.energyapi.model.HourlyUsage;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/energy")
@CrossOrigin  // erlaubt Anfragen vom JavaFX-Client
public class EnergyController {

    @GetMapping("/current")
    public CurrentPercentage getCurrent() {
        return new CurrentPercentage(
                "2025-01-10T14:00:00",
                100.0,
                5.63
        );
    }

    @GetMapping("/historical")
    public List<HourlyUsage> getHistorical(
            @RequestParam String start,
            @RequestParam String end) {

        // Milestone 1: statische Beispieldaten
        return List.of(
                new HourlyUsage("2025-01-10T14:00:00", 18.05, 18.05, 1.076),
                new HourlyUsage("2025-01-10T13:00:00", 15.015, 14.033, 2.049),
                new HourlyUsage("2025-01-10T12:00:00", 12.5, 11.8, 0.9)
        );
    }
}