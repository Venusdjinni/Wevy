package com.venus.app.Utils;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;

/**
 * Created by arnold on 02/11/16.
 */
public class SimpleMessageDialog extends AppCompatDialogFragment {

    public SimpleMessageDialog putArguments(String message) {
        Bundle bundle = new Bundle();
        bundle.putString("message", message);
        setArguments(bundle);
        return this;
    }

    public SimpleMessageDialog putArguments(String message, boolean is_terminating) {
        Bundle bundle = new Bundle();
        bundle.putString("message", message);
        bundle.putBoolean("is_terminating", is_terminating);
        setArguments(bundle);
        return this;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog dialog =  new AlertDialog.Builder(getActivity())
                .setTitle("Information")
                .setMessage(getArguments().getString("message"))
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                // do something...
                                if (getArguments().getBoolean("is_terminating")) {
                                    // si l'activité implémente Terminating
                                    ((Terminating) getActivity()).terminer();
                                }
                            }
                        }
                )
                .setCancelable(false)
                .create();
        if (getArguments().getBoolean("is_terminating")) dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }
}
