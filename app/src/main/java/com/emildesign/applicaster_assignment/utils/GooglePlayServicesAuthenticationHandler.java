package com.emildesign.applicaster_assignment.utils;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;

import com.emildesign.applicaster_assignment.utils.AndroidUtils;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.youtube.YouTubeScopes;

import java.util.Arrays;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * Created by EmilAdz on 11/25/17.
 */

public class GooglePlayServicesAuthenticationHandler {

    public interface GooglePlayServicesHandlerCallback {
        void onAccountSelected();
    }

    public static final String PREF_ACCOUNT_NAME = "accountName";
    public static final String[] SCOPES = { YouTubeScopes.YOUTUBE_READONLY };

    public static final int REQUEST_ACCOUNT_PICKER = 1000;
    public static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    public static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;

    private final Activity mActivity;
    private final GoogleAccountCredential mCredential;
    private final GooglePlayServicesHandlerCallback mGooglePlayServicesHandlerCallback;

    GooglePlayServicesAuthenticationHandler(Activity aActivity, GooglePlayServicesHandlerCallback aGooglePlayServicesHandlerCallback) {
        this.mActivity = aActivity;
        this.mCredential = GoogleAccountCredential.usingOAuth2(mActivity.getApplicationContext(), Arrays.asList(SCOPES)).setBackOff(new ExponentialBackOff());
        this.mGooglePlayServicesHandlerCallback = aGooglePlayServicesHandlerCallback;
    }

    void runAuthenticationSequence() {
        if (!AndroidUtils.isGooglePlayServicesAvailable(mActivity)) {
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        }
    }

    boolean isAuthenticated() {
        boolean isAuthenticated = false;
        if (!AndroidUtils.isGooglePlayServicesAvailable(mActivity)) {
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() != null) {
            isAuthenticated = true;
        }

        return isAuthenticated;
    }

    public void setSelectedAccountName(String aName) {
        mCredential.setSelectedAccountName(aName);
        mGooglePlayServicesHandlerCallback.onAccountSelected();
    }

    /**
     * Attempts to set the account used with the API credentials. If an account
     * name was previously saved it will use that one; otherwise an account
     * picker dialog will be shown to the user. Note that the setting the
     * account to use with the credentials object requires the app to have the
     * GET_ACCOUNTS permission, which is requested here if it is not already
     * present. The AfterPermissionGranted annotation indicates that this
     * function will be rerun automatically whenever the GET_ACCOUNTS permission
     * is granted.
     */
    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    void chooseAccount() {
        if (EasyPermissions.hasPermissions(mActivity, Manifest.permission.GET_ACCOUNTS)) {
            String accountName = mActivity.getPreferences(Context.MODE_PRIVATE).getString(PREF_ACCOUNT_NAME, null);
            if (accountName != null) {
                setSelectedAccountName(accountName);
            } else {
                // Start a dialog from which the user can choose an account
                mActivity.startActivityForResult(mCredential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
            }
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(mActivity,
                    "This app needs to access your Google account (via Contacts).",
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS);
        }
    }

    /**
     * Attempt to resolve a missing, out-of-date, invalid or disabled Google
     * Play Services installation via a user dialog, if possible.
     */
    void acquireGooglePlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        final int connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(mActivity);
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }

    /**
     * Display an error dialog showing that Google Play Services is missing
     * or out of date.
     * @param connectionStatusCode code describing the presence (or lack of)
     *     Google Play Services on this device.
     */
    void showGooglePlayServicesAvailabilityErrorDialog(final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(mActivity, connectionStatusCode, REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    public GoogleAccountCredential getCredential() {
        return mCredential;
    }
}
