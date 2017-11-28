package com.emildesign.applicaster_assignment.dialog;

import android.support.v7.app.AppCompatActivity;

/**
 * Created by EmilAdz on 11/27/17.
 */

public class DialogUtils {

    public static void showNetworkErrorDialog(AppCompatActivity aActivity) {
        GeneralDialogFragment generalDialogFragment = GeneralDialogFragment.newInstance("Network Error", "No network connection available.", false);
        generalDialogFragment.show(aActivity.getSupportFragmentManager(),"network_error");
    }

    public static void showGeneralErrorDialog(AppCompatActivity aActivity, String message) {
        GeneralDialogFragment generalDialogFragment = GeneralDialogFragment.newInstance("Error", message, false);
        generalDialogFragment.show(aActivity.getSupportFragmentManager(),"general_error");
    }

    public static  void showGooglePlayServicesErrorDialog(AppCompatActivity aActivity) {
        GeneralDialogFragment generalDialogFragment = GeneralDialogFragment.newInstance("Google Play Services Error", "This app requires Google Play Services. Please install\n" +
                "Google Play Services on your device and relaunch this app.", false);
        generalDialogFragment.show(aActivity.getSupportFragmentManager(),"play_error");
    }
}
