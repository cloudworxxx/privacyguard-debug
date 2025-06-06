package ca.uwaterloo.crysp.privacyguard.Application.Database;

import java.text.ParseException;
import java.util.Date;

public class DataLeak {

    private final String packageName;
    private final String appName;
    private final String category;
    private final String type;
    private final String leakContent;
    private final String timestamp;
    private final Date timestampDate;
    private final int foregroundStatus;
    private final String destination;

    public DataLeak(String packageName, String appName, String category, String type, String content, String timestamp, int foregroundStatus, String destination){
        this.packageName = packageName;
        this.appName = appName;
        this.category = category;
        this.type = type;
        this.leakContent = content;
        this.timestamp = timestamp;
        this.foregroundStatus = foregroundStatus;
        this.destination = destination;

        try {
            this.timestampDate = DatabaseHandler.getDateFormat().parse(timestamp);
        }
        catch (ParseException ex) {
            throw new RuntimeException("Invalid timestamp for DataLeak, tried to parse: " + timestamp);
        }
    }

    public String getPackageName() {
        return packageName;
    }

    public String getAppName() {
        return appName;
    }

    public String getCategory() {
        return category;
    }

    public String getType() {
        return type;
    }

    public String getLeakContent() {
        return leakContent;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public Date getTimestampDate() {
        return timestampDate;
    }

    public int getForegroundStatus() {
        return foregroundStatus;
    }

    public String getDestination() {
        return destination;
    }
}
