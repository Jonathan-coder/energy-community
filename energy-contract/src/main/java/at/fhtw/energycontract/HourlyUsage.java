package at.fhtw.energycontract;

public record HourlyUsage(
        String hour,
        double communityProduced,
        double communityUsed,
        double gridUsed
) {
}
