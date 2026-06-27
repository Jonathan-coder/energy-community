package at.fhtw.energygui;

public class HourlyUsageRow {
    private final String period;
    private final String communityProduced;
    private final String communityUsed;
    private final String gridUsed;

    public HourlyUsageRow(String period, String communityProduced,
                          String communityUsed, String gridUsed) {
        this.period = period;
        this.communityProduced = communityProduced;
        this.communityUsed = communityUsed;
        this.gridUsed = gridUsed;
    }

    public String getPeriod() { return period; }
    public String getCommunityProduced() { return communityProduced; }
    public String getCommunityUsed() { return communityUsed; }
    public String getGridUsed() { return gridUsed; }
}
