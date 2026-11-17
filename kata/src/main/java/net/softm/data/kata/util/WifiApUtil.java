package net.softm.data.kata.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.Task;

import net.softm.data.kata.WApplication;

public class WifiApUtil {
    static AcsUtil acsUtil = null;

    public WifiApUtil(Context context)
    {
        mContext = context;
    }
    private Context mContext;


    private WifiManager.LocalOnlyHotspotReservation mReservation;
    private boolean isHotspotEnabled = false;
    private final int REQUEST_ENABLE_LOCATION_SYSTEM_SETTINGS = 101;

    @SuppressLint("MissingPermission")
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void turnOnHotspot() {
        WifiManager manager = (WifiManager) WApplication.mInstance.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (manager != null) {
            // Don't start when it started (existed)
            manager.startLocalOnlyHotspot(new WifiManager.LocalOnlyHotspotCallback() {

                @Override
                public void onStarted(WifiManager.LocalOnlyHotspotReservation reservation) {
                    super.onStarted(reservation);
                    //Log.d(TAG, "Wifi Hotspot is on now");
                    mReservation = reservation;
                    isHotspotEnabled = true;
                }

                @Override
                public void onStopped() {
                    super.onStopped();
                    //Log.d(TAG, "onStopped: ");
                    isHotspotEnabled = false;
                }

                @Override
                public void onFailed(int reason) {
                    super.onFailed(reason);
                    //Log.d(TAG, "onFailed: ");
                    isHotspotEnabled = false;
                }
            }, new Handler());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void turnOffHotspot() {
//        if (!isLocationPermissionEnable()) {
//            return;
//        }
        if (mReservation != null) {
            mReservation.close();
            isHotspotEnabled = false;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void toggleHotspot() {
        if (!isHotspotEnabled) {
            turnOnHotspot();
        } else {
            turnOffHotspot();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void enableLocationSettings() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10);
        mLocationRequest.setSmallestDisplacement(10);
        mLocationRequest.setFastestInterval(10);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest)
                .setAlwaysShow(false); // Show dialog

        Task<LocationSettingsResponse> task= LocationServices.getSettingsClient(mContext).checkLocationSettings(builder.build());

        task.addOnCompleteListener(task1 -> {
            try {
                LocationSettingsResponse response = task1.getResult(ApiException.class);
                // All location settings are satisfied. The client can initialize location
                // requests here.
                toggleHotspot();

            } catch (ApiException exception) {
                switch (exception.getStatusCode()) {
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. But could be fixed by showing the
                        // user a dialog.
                        try {
                            // Cast to a resolvable exception.
                            ResolvableApiException resolvable = (ResolvableApiException) exception;
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            resolvable.startResolutionForResult((Activity) mContext, REQUEST_ENABLE_LOCATION_SYSTEM_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        } catch (ClassCastException e) {
                            // Ignore, should be an impossible error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way to fix the
                        // settings so we won't show the dialog.
                        break;
                }
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        final LocationSettingsStates states = LocationSettingsStates.fromIntent(data);
        switch (requestCode) {
            case REQUEST_ENABLE_LOCATION_SYSTEM_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        // All required changes were successfully made
                        toggleHotspot();
                        Toast.makeText(mContext,states.isLocationPresent()+"",Toast.LENGTH_SHORT).show();
                        break;
                    case Activity.RESULT_CANCELED:
                        // The user was asked to change settings, but chose not to
                        Toast.makeText(mContext,"Canceled",Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        break;
                }
                break;
        }
    }

}

