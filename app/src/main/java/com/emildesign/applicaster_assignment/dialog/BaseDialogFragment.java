package com.emildesign.applicaster_assignment.dialog;

import android.app.Activity;
import android.support.v4.app.DialogFragment;

/**
 * Created by EmilAdz on 11/27/17.
 */

public abstract class BaseDialogFragment<T> extends DialogFragment {
    private T mActivityInstance;

    public final T getActivityInstance() {
        return mActivityInstance;
    }

    @Override
    public void onAttach(Activity activity) {
        mActivityInstance = (T) activity;
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mActivityInstance = null;
    }
}