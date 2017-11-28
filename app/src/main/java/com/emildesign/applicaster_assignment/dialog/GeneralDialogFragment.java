package com.emildesign.applicaster_assignment.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;

/**
 * Created by EmilAdz on 11/27/17.
 */

public class GeneralDialogFragment extends BaseDialogFragment<GeneralDialogFragment.OnDialogFragmentClickListener> {

    // interface to handle the dialog click back to the Activity
    public interface OnDialogFragmentClickListener {
        public void onOkClicked(GeneralDialogFragment dialog);
        public void onCancelClicked(GeneralDialogFragment dialog);
    }

    // Create an instance of the Dialog with the input
    public static GeneralDialogFragment newInstance(String title, String message, boolean shouldShowCancel) {
        GeneralDialogFragment frag = new GeneralDialogFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putString("message", message);
        args.putBoolean("shouldShowCancel", shouldShowCancel);
        frag.setArguments(args);
        return frag;
    }
    // Create a Dialog using default AlertDialog builder , if not inflate custom view in onCreateView
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(getArguments().getString("title"))
                .setMessage(getArguments().getString("message"))
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                getActivityInstance().onOkClicked(GeneralDialogFragment.this);
                            }
                        }
                );

        if (getArguments().getBoolean("shouldShowCancel")) {
            builder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        getActivityInstance().onCancelClicked(GeneralDialogFragment.this);
                    }
                }
            );
        }

        return builder.create();
    }

}
