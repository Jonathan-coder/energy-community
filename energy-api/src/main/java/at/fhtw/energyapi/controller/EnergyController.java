package at.fhtw.energyapi.controller;

import at.fhtw.energyapi.service.EnergyDataService;
import at.fhtw.energycontract.CurrentPercentage;
import at.fhtw.energycontract.HourlyUsage;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/energy")
@CrossOrigin
public class EnergyController {
    private final EnergyDataService energyDataService;

    public EnergyController(EnergyDataService energyDataService) {
        this.energyDataService = energyDataService;
    }

    @GetMapping("/current")
    public CurrentPercentage getCurrent() {
        return energyDataService.getCurrentPercentage();
    }

    @GetMapping("/historical")
    public List<HourlyUsage> getHistorical(
            @RequestParam("start")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime start,
            @RequestParam("end")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime end
    ) {
        return energyDataService.getHistorical(start, end);
    }
}
