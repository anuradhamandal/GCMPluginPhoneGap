package com.google.android.gcm.my.app;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import android.os.StrictMode;
import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;
import org.apache.cordova.PluginResult;
import android.widget.Toast;
import android.content.Intent;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

//import android.app.Activity;
//import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Bundle;
//import android.util.Log;
import android.view.View;
import android.os.StrictMode;

//import android.widget.TextView;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.lang.*;
import java.net.URLConnection;
import java.io.OutputStreamWriter;

public class GcmPlugin extends CordovaPlugin {

	//Context context;

	public static final String EXTRA_MESSAGE = "message";
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    /**
     * Substitute you own sender ID here. This is the project number you got
     * from the API Console, as described in "Getting Started."
     */
    String SENDER_ID = "your project number";

    /**
     * Tag used on log messages.
     */
    static final String TAG = "GCM TESTING";

    String mDisplay;
    GoogleCloudMessaging gcm;
    AtomicInteger msgId = new AtomicInteger();
    public Context context;
    String regid;

	public GcmPlugin(){
	}
    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        context = this.cordova.getActivity();
        googleCloudMsg(context);

    }
	@Override
	public boolean execute(String action, JSONArray args, CallbackContext callbackContext) {
		PluginResult.Status status = PluginResult.Status.OK;
		String result = "";
		if(action.equals("Gcm")) {
			//startGcmActivity();
			//googleCloudMsg(context);
			callbackContext.success(getRegistrationId(context));
			Log.d(TAG,"GcmPlugin Called: " + this.getRegistrationId(context));
			return true;
		}else {
			return false;
		}
	}
	public void googleCloudMsg(Context context) {
        
        // Check device for Play Services APK. If check succeeds, proceed with GCM registration.
        if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(context);
            regid = getRegistrationId(context);

            if (regid.isEmpty()) {
                Log.d(TAG, "googleCloudMsg regid isEmpty");
                if (android.os.Build.VERSION.SDK_INT > 9) {  
                     StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();  
                     StrictMode.setThreadPolicy(policy);  
                }
                registerInBackground();
            }
        } else {
            Log.d(TAG, "No valid Google Play Services APK found.");
        }
    }
    
private boolean checkPlayServices() {
    context = this.cordova.getActivity();
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                Log.d(TAG, "isUserRecoverableError running.");
            } else {
                Log.d(TAG, "This device is not supported.");
            }
            return false;
        }
        return true;
    
}

    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = context.getSharedPreferences("MyPrefs", context.MODE_PRIVATE);
        int appVersion = getAppVersion(context);
        Log.d(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }

    /**
     * Gets the current registration ID for application on GCM service, if there is one.
     * <p>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     *         registration ID.
     */
    private String getRegistrationId(Context context) {
        //final SharedPreferences prefs = getGcmPreferences(context);
        final SharedPreferences prefs = context.getSharedPreferences("MyPrefs", context.MODE_PRIVATE);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.d(TAG, "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.d(TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }
    private void registerInBackground() {
        context = this.cordova.getActivity();
         AsyncTask<Void,Void,String> myTask = new AsyncTask<Void, Void, String>() {
             @Override
             protected String doInBackground(Void... params) {
                Log.d(TAG,"Running AsyncTask");
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    regid = gcm.register(SENDER_ID);
                    msg = "Device registered, registration ID=" + regid;
                    Log.d(TAG, "Registration Id : " + regid);
                    storeRegistrationId(context, regid);
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    Log.d(TAG, "registerInBackground catch : " + msg);
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }
                 return msg;
            }
        };
        if (android.os.Build.VERSION.SDK_INT >= 9)
            myTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        else
            myTask.execute();
    }
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

}

