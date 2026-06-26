package at.fhtw.energycontract;

public record CurrentPercentage(
        String hour,
        double communityDepleted,
        double gridPortion
) {
}
