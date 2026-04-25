package at.fhtw.energyapi.model;

public record HourlyUsage(
        String hour,
        double communityProduced,
        double communityUsed,
        double gridUsed
) {}