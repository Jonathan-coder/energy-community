package at.fhtw.energycontract;

public record EnergyMessage(
        EnergyMessageType type,
        EnergyAssociation association,
        double kwh,
        String datetime
) {
}
