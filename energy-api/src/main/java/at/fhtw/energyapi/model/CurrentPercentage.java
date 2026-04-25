package at.fhtw.energyapi.model;

public record CurrentPercentage(
        String hour,
        double communityDepleted,
        double gridPortion
) {}