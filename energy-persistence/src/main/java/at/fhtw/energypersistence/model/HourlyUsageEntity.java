package at.fhtw.energypersistence.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "hourly_usage")
public class HourlyUsageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usage_hour", nullable = false, unique = true)
    private String hour;

    @Column(nullable = false)
    private double communityProduced;

    @Column(nullable = false)
    private double communityUsed;

    @Column(nullable = false)
    private double gridUsed;

    protected HourlyUsageEntity() {
    }

    public HourlyUsageEntity(String hour, double communityProduced, double communityUsed, double gridUsed) {
        this.hour = hour;
        this.communityProduced = communityProduced;
        this.communityUsed = communityUsed;
        this.gridUsed = gridUsed;
    }

    public Long getId() {
        return id;
    }

    public String getHour() {
        return hour;
    }

    public double getCommunityProduced() {
        return communityProduced;
    }

    public double getCommunityUsed() {
        return communityUsed;
    }

    public double getGridUsed() {
        return gridUsed;
    }

    public void setCommunityProduced(double communityProduced) {
        this.communityProduced = communityProduced;
    }

    public void setCommunityUsed(double communityUsed) {
        this.communityUsed = communityUsed;
    }

    public void setGridUsed(double gridUsed) {
        this.gridUsed = gridUsed;
    }
}
