package com.readora.readora.dto;

public class AdminSubscriptionStats {

    private long totalSubscriptions;
    private long activeSubscriptions;
    private long freeCount;
    private long premiumCount;
    private long institutionalCount;
    private double monthlyRecurringRevenue;

    public AdminSubscriptionStats() {
    }

    public AdminSubscriptionStats(long totalSubscriptions, long activeSubscriptions,
                                  long freeCount, long premiumCount, long institutionalCount,
                                  double monthlyRecurringRevenue) {
        this.totalSubscriptions = totalSubscriptions;
        this.activeSubscriptions = activeSubscriptions;
        this.freeCount = freeCount;
        this.premiumCount = premiumCount;
        this.institutionalCount = institutionalCount;
        this.monthlyRecurringRevenue = monthlyRecurringRevenue;
    }

    public long getTotalSubscriptions() { return totalSubscriptions; }
    public void setTotalSubscriptions(long totalSubscriptions) { this.totalSubscriptions = totalSubscriptions; }

    public long getActiveSubscriptions() { return activeSubscriptions; }
    public void setActiveSubscriptions(long activeSubscriptions) { this.activeSubscriptions = activeSubscriptions; }

    public long getFreeCount() { return freeCount; }
    public void setFreeCount(long freeCount) { this.freeCount = freeCount; }

    public long getPremiumCount() { return premiumCount; }
    public void setPremiumCount(long premiumCount) { this.premiumCount = premiumCount; }

    public long getInstitutionalCount() { return institutionalCount; }
    public void setInstitutionalCount(long institutionalCount) { this.institutionalCount = institutionalCount; }

    public double getMonthlyRecurringRevenue() { return monthlyRecurringRevenue; }
    public void setMonthlyRecurringRevenue(double monthlyRecurringRevenue) { this.monthlyRecurringRevenue = monthlyRecurringRevenue; }
}