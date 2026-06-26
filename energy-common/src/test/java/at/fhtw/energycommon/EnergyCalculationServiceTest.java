package at.fhtw.energycommon;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EnergyCalculationServiceTest {

    private final EnergyCalculationService service = new EnergyCalculationService();

    @Test
    void usesCommunityEnergyBeforeGridAndCalculatesPercentages() {
        var usage = service.calculateUsage("2026-06-26T10:00:00", 10.0, 12.0, 3.0);
        var percentage = service.calculatePercentage("2026-06-26T10:00:00", 10.0, 12.0, 3.0);

        assertEquals(10.0, usage.communityProduced());
        assertEquals(10.0, usage.communityUsed());
        assertEquals(3.0, usage.gridUsed());
        assertEquals(100.0, percentage.communityDepleted());
        assertEquals(23.08, percentage.gridPortion());
    }

    @Test
    void handlesZeroProduction() {
        var usage = service.calculateUsage("2026-06-26T10:00:00", 0.0, 8.0, 1.0);
        var percentage = service.calculatePercentage("2026-06-26T10:00:00", 0.0, 8.0, 1.0);

        assertEquals(0.0, usage.communityUsed());
        assertEquals(1.0, usage.gridUsed());
        assertEquals(0.0, percentage.communityDepleted());
        assertEquals(100.0, percentage.gridPortion());
    }
}
