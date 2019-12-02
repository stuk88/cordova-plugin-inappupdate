package com.ugotit.app;

import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;

import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.InstallStatus;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.tasks.OnSuccessListener;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

public class inAppUpdate extends CordovaPlugin implements OnSuccessListener<AppUpdateInfo> {
    private AppUpdateManager appUpdateManager;
    public static final int REQUEST_CODE = 1234;
    private FrameLayout layout;
    CallbackContext callback;
    private static JSONArray arguments = null;
    // final Context context = this.cordova.getActivity().getApplicationContext();

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);

        appUpdateManager = AppUpdateManagerFactory.create(cordova.getActivity());
        layout = (FrameLayout) webView.getView().getParent();
    }

    @Override
    public void onResume(boolean multitasking) {
        super.onResume(multitasking);
        appUpdateManager.getAppUpdateInfo().addOnSuccessListener(this);
    }

    @Override
    public boolean execute(final String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
        if (action.equals("inAppUpdate")) {
            callback = callbackContext;
            arguments = args;

            appUpdateManager.getAppUpdateInfo().addOnSuccessListener(this);

            appUpdateManager.getAppUpdateInfo().addOnFailureListener(err -> {
               Log.e("Ugotit -> appUpdateInfo", err.toString());
            });


        }
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                callback.success("user accpeted to update");
            } else if (resultCode == RESULT_CANCELED) {
                callback.success("user canceled to update");
            } else {
                callback.success("error occured");
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onSuccess(AppUpdateInfo appUpdateInfo) {
        try {
            JSONObject updateData = new JSONObject();

            updateData.put("versionCode", appUpdateInfo.availableVersionCode());
            updateData.put("status", appUpdateInfo.installStatus());

            JSONObject appData = arguments.getJSONObject(0);

            if (appData.optString("updateIndicator").toString().equals("mandatory")) {
                if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                    updateData.put("updateavailablity", "in_progress");
                    startUpdate(appUpdateInfo, AppUpdateType.IMMEDIATE);
                } else if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                    updateData.put("updateavailablity", "update available");
                    startUpdate(appUpdateInfo, AppUpdateType.IMMEDIATE);
                } else if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_NOT_AVAILABLE) {
                    updateData.put("updateavailablity", "update not available");
                } else if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {

                    popupSnackbarForCompleteUpdate(appData.optString("displayMessage").toString(), appData.optString("actionBtn").toString());
                }
            } else {
                if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                    updateData.put("updateavailablity", "in_progress");
                    startUpdate(appUpdateInfo, AppUpdateType.FLEXIBLE);
                } else if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                    updateData.put("updateavailablity", "update available");
                    startUpdate(appUpdateInfo, AppUpdateType.FLEXIBLE);
                } else if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_NOT_AVAILABLE) {
                    updateData.put("updateavailablity", "update not available");
                } else if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                    popupSnackbarForCompleteUpdate(appData.optString("displayMessage").toString(), appData.optString("actionBtn").toString());
                }
            }


        } catch (JSONException e) {
            /*callback.error(e.getMessage());*/
        }
    }

    private void startUpdate(final AppUpdateInfo appUpdateInfo, final int appUpdateType) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    appUpdateManager.startUpdateFlowForResult(appUpdateInfo,
                            appUpdateType,
                            cordova.getActivity(),
                            REQUEST_CODE);
                    PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
                    pluginResult.setKeepCallback(true);
                    callback.sendPluginResult(pluginResult);
                } catch (IntentSender.SendIntentException e) {
                    callback.error(e.getMessage());
                }
            }
        }).start();
    }

    /* Displays the snackbar notification and call to action. */
    private void popupSnackbarForCompleteUpdate(String msg, String action) {
        Toast.makeText(cordova.getActivity(), "An update has just been downloaded", Toast.LENGTH_LONG).show();
        Snackbar snackbar = Snackbar
                .make(layout, msg, Snackbar.LENGTH_INDEFINITE)
                .setAction(action, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        appUpdateManager.completeUpdate();
                    }
                });
        snackbar.show();
    }

/*    private void showFlexibleUpdateNotification(String msg) {
        Snackbar snackbar =
                Snackbar.make(
                        layout,
                        msg ,
                        Snackbar.LENGTH_INDEFINITE);
        snackbar.show();
    }*/
}


