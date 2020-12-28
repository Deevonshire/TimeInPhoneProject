package anton.ryaby_belstu.time.in.phone.project.IntervalEnum;

import android.app.usage.UsageStatsManager;

public enum Interval {

    WEEKLY("Weekly", UsageStatsManager.INTERVAL_DAILY),
    MONTHLY("Monthly", UsageStatsManager.INTERVAL_WEEKLY),
    HALFYEAR("Half-year", UsageStatsManager.INTERVAL_MONTHLY),
    YEARLY("Yearly", UsageStatsManager.INTERVAL_YEARLY);

    public int mInterval;
    public String mStrInterval;

    Interval(String strInterval, int interval) {
        this.mStrInterval = strInterval;
        this.mInterval = interval;
    }

    public static Interval getValue(String strInterval) {
        for (Interval statUsageIn : values()) {
            if (statUsageIn.mStrInterval.equals(strInterval)) {
                return statUsageIn;
            }
        }
        return null;
    }
}
