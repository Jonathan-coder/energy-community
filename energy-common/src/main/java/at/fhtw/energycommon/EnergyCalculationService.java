package at.fhtw.energycommon;

import at.fhtw.energycontract.CurrentPercentage;
import at.fhtw.energycontract.HourlyUsage;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class EnergyCalculationService {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public HourlyUsage calculateUsage(String hour, double communityProduced, double communityUsed, double gridUsed) {
        double safeCommunityProduced = Math.max(0.0, communityProduced);
        double safeCommunityUsed = Math.max(0.0, communityUsed);
        double safeGridUsed = Math.max(0.0, gridUsed);

        double effectiveCommunityUsed = Math.min(safeCommunityUsed, safeCommunityProduced);
        double effectiveGridUsed = Math.max(0.0, safeCommunityUsed - effectiveCommunityUsed) + safeGridUsed;

        return new HourlyUsage(
                hour,
                round3(safeCommunityProduced),
                round3(effectiveCommunityUsed),
                round3(effectiveGridUsed)
        );
    }

    public CurrentPercentage calculatePercentage(String hour, double communityProduced, double communityUsed, double gridUsed) {
        double effectiveCommunityUsed = Math.min(Math.max(0.0, communityUsed), Math.max(0.0, communityProduced));
        double effectiveGridUsed = Math.max(0.0, Math.max(0.0, communityUsed) - effectiveCommunityUsed) + Math.max(0.0, gridUsed);
        double total = effectiveCommunityUsed + effectiveGridUsed;

        double communityDepleted = communityProduced == 0.0 ? 0.0 : round2((effectiveCommunityUsed / communityProduced) * 100.0);
        double gridPortion = total == 0.0 ? 0.0 : round2((effectiveGridUsed / total) * 100.0);

        return new CurrentPercentage(hour, communityDepleted, gridPortion);
    }

    public String normalizeHour(LocalDateTime localDateTime) {
        return localDateTime.format(FORMATTER);
    }

    private double round2(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    private double round3(double value) {
        return BigDecimal.valueOf(value).setScale(3, RoundingMode.HALF_UP).doubleValue();
    }
}
