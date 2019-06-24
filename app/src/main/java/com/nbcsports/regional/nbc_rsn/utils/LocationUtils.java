package com.nbcsports.regional.nbc_rsn.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.view.View;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.Task;
import com.nbcsports.regional.nbc_rsn.MainActivity;
import com.nbcsports.regional.nbc_rsn.navigation.NavigationManager;
import com.nbcsports.regional.nbc_rsn.teamselector.TeamSelectorFragment;
import com.nbcsports.regional.nbc_rsn.urban_airship.NotificationsManagerKt;

import java.lang.ref.WeakReference;

import timber.log.Timber;

import static android.content.Context.LOCATION_SERVICE;
import static android.location.LocationManager.GPS_PROVIDER;
import static android.location.LocationManager.NETWORK_PROVIDER;

public class LocationUtils {

    private static int LOCATION_POLLING_MIN_TIME = 60000; //Milliseconds. 1 min.
    private static int LOCATION_POLLING_MIN_DISTANCE = 100; //Meters.
    private static int LOCATION_PERMISSION_REQUEST_CODE = 10;

    public static final String DENY_LOCATION_SERVICES = "deny_location_services";

    private static boolean waitingForConfig = false;
    private static Location userGPSLocation;
    private static Location userIPLocation;
    private static boolean locationReceived = false;
    private static LocationManager locationManager;
    private static LocationListener locationListener;
    private static boolean wasResolutionDialogShown = false;

    public static void requestUserForLocationPermissions(MainActivity activity) {
        locationManager = (LocationManager) activity.getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener(activity);

        try {
            String[] permissions = new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.INTERNET};

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && isFineLocationAndCoarseLocationDisabled(activity)) {
                activity.requestPermissions(permissions, LOCATION_PERMISSION_REQUEST_CODE);

            } else {
                Location location = locationManager.getLastKnownLocation(GPS_PROVIDER);
                if (location != null) {
                    userGPSLocation = location;
                    locationReceived = true;
                }
                requestLocationUpdates();
            }
        } catch (SecurityException e) {
            Timber.e(e);
            onDeviceLocationUnavailable(activity);
        }
    }

    /*
    This function builds a dialog to enable Location Services without having to
    go directly into the settings.
     */
    public static void displayLocationSettingsRequest(MainActivity activity) {
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(activity)
                .addApi(LocationServices.API).build();
        googleApiClient.connect();

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(1000 * 30);
        locationRequest.setFastestInterval(1000 * 15);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        Task<LocationSettingsResponse> task = LocationServices.getSettingsClient(activity).checkLocationSettings(builder.build());
        task.addOnCompleteListener(task1 -> {

            try {
                LocationSettingsResponse response = task1.getResult(ApiException.class);
                requestLocationUpdates();
                Timber.d("All location settings are satisfied.");

            } catch (ApiException exception) {
                switch (exception.getStatusCode()) {
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        Timber.e("Location settings are not satisfied. Show the user a dialog to upgrade location settings ");

                        // Show the dialog by calling startResolutionForResult(), and check the result
                        // in onActivityResult().
                        if (!wasResolutionDialogShown) {
                            wasResolutionDialogShown = true;

                            try {
                                ResolvableApiException resolvableApiException = (ResolvableApiException) exception;
                                resolvableApiException.startResolutionForResult(activity, LOCATION_PERMISSION_REQUEST_CODE);
                            } catch (IntentSender.SendIntentException ignore) {
                                // ignore
                                Timber.e("PendingIntent unable to execute request.");
                            } catch (ClassCastException ignore) {
                                // ignore
                            }
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Timber.e("Location settings are inadequate, and cannot be fixed here. Dialog not created.");
                        break;
                }
            }
        });
    }

    public static void onRequestPermissionsResultReceived(MainActivity activity, int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        try {
            if (NavigationManager.getInstance().getCurrentFragment() instanceof TeamSelectorFragment) {
                ((TeamSelectorFragment) NavigationManager.getInstance().getCurrentFragment()).setLoadingSpinnerVisibility(true);
            }
            if (isLocationPermissionsDenied(activity, requestCode, grantResults)) {
                onDeviceLocationUnavailable(activity);
                return;
            }

            Location location = locationManager.getLastKnownLocation(GPS_PROVIDER);
            if (location != null) {
                userGPSLocation = location;
                locationReceived = true;
                activity.onReceivedLocation(location);
            }

            requestLocationUpdates();
        } catch (SecurityException e) {
            Timber.e(e);
            onDeviceLocationUnavailable(activity);
        }
    }

    @SuppressLint("MissingPermission")
    private static void requestLocationUpdates() {
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locationManager.requestLocationUpdates(GPS_PROVIDER, LOCATION_POLLING_MIN_TIME, LOCATION_POLLING_MIN_DISTANCE, locationListener);
        }
        locationManager.requestLocationUpdates(NETWORK_PROVIDER, LOCATION_POLLING_MIN_TIME, LOCATION_POLLING_MIN_DISTANCE, locationListener);
    }

    public static void setGeoLocation(Location ipLocation) {
        userIPLocation = ipLocation;
        locationReceived = true;
    }

    public static boolean isLocationReceived() {
        return locationReceived;
    }

    public static void releaseLocationListener() {
        if (locationManager == null) {
            return;
        }
        if (locationListener == null) {
            return;
        }

        locationListener.releaseActivityReference();
        locationManager.removeUpdates(locationListener);
    }

    public static Location getUserLocation() {
        if (userGPSLocation != null) {
            return userGPSLocation;
        }
        return userIPLocation;
    }

    public static void onDeviceLocationUnavailable(MainActivity activity) {
        /*
        * We used to get geolocation from a web service, an url provided by config, but as requested
        * in RSNAPP-1174, we're changing the app behaviour to show an error message instead.
        * I'm keeping the code of parsing and using the geolocation url from config, in case one
        * day the client wants to change the behaviour again.
        * Same thing on iOS.
        * */

        if (NavigationManager.getInstance().getCurrentFragment() instanceof TeamSelectorFragment) {
            ((TeamSelectorFragment) NavigationManager.getInstance().getCurrentFragment()).setLoadingSpinnerVisibility(false);
        }
        NotificationsManagerKt.INSTANCE.showLocationPermissionError(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
                intent.setData(uri);
                activity.startActivity(intent);
            }
        });
    }

    public static void onConfigReceived(MainActivity activity) {
        if (waitingForConfig) {
            activity.getIpGeolocation();
        }
    }

    private static boolean isLocationPermissionsDenied(MainActivity activity, int requestCode, @NonNull int[] grantResults) {
        return requestCode != LOCATION_PERMISSION_REQUEST_CODE
                || grantResults.length == 0
                || grantResults[0] != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED;
    }

    private static boolean isFineLocationAndCoarseLocationDisabled(MainActivity activity) {
        return ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED;
    }

    static class LocationListener implements android.location.LocationListener {
        private WeakReference<MainActivity> activityWeakReference;

        LocationListener(MainActivity activity) {
            activityWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void onLocationChanged(Location location) {
            userGPSLocation = location;
            locationReceived = true;
            if (activityWeakReference != null && activityWeakReference.get() != null) {
                activityWeakReference.get().onReceivedLocation(location);
            }
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
            Timber.d("onStatusChanged %s", s);
        }

        @Override
        public void onProviderEnabled(String s) {
            Timber.d("onProviderEnabled %s", s);
        }

        @Override
        public void onProviderDisabled(String s) {
            if (activityWeakReference != null && activityWeakReference.get() != null) {
                if (PreferenceUtils.INSTANCE.getBoolean(DENY_LOCATION_SERVICES, false)) {
                    onDeviceLocationUnavailable(activityWeakReference.get());
                } else {
                    displayLocationSettingsRequest(activityWeakReference.get());
                }
            }
        }

        void releaseActivityReference() {
            if (activityWeakReference != null) activityWeakReference.clear();
        }

    }
}