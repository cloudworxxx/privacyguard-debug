package ca.uwaterloo.crysp.privacyguard.Application.Database;

/**
 * Created by rongjunyan on 2017-08-08.
 */

public class Traffic {
    private final String appName;
    private final String destIP;
    private final boolean encrypted;
    private final int size;
    private final boolean outgoing;

    public Traffic(String appName, String destIP, boolean encrypted, int size, boolean outgoing){
        this.appName = appName;
        this.destIP = destIP;
        this.encrypted = encrypted;
        this.size = size;
        this.outgoing = outgoing;
    }

    public String getAppName(){
        return appName;
    }

    public String getDestIP(){
        return destIP;
    }

    public boolean isEncrypted(){
        return encrypted;
    }

    public int getSize(){
        return size;
    }

    public boolean isOutgoing(){
        return outgoing;
    }
}
