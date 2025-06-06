package ca.uwaterloo.crysp.privacyguard.Plugin;

import android.content.Context;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;

import java.io.IOException;

/**
 * Created by frank on 2014-06-23.
 */
public interface IPlugin {
    // May modify the content of the request and response
    LeakReport handleRequest(String request);
    LeakReport handleResponse(String response);
    String modifyRequest(String request);
    String modifyResponse(String response);
    void setContext(Context context);
}
