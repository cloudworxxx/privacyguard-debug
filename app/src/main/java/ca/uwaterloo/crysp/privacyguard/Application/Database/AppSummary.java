package ca.uwaterloo.crysp.privacyguard.Application.Database;

public class AppSummary {
    private final String packageName;
    private final String appName;
    private final int totalLeaks;
    private final int ignore;

    public AppSummary(String packageName, String appName, int totalLeaks, int ignore) {
        this.packageName = packageName;
        this.appName = appName;
        this.totalLeaks = totalLeaks;
        this.ignore = ignore;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getAppName() {
        return appName;
    }

    public int getTotalLeaks() {
        return totalLeaks;
    }

    public int getIgnore() {
        return ignore;
    }
}
