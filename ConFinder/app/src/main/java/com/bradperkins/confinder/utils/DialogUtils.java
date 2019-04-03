package com.bradperkins.confinder.utils;

// Date 1/19/19
// 
// Bradley Perkins

// AID - 1809

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.Switch;

// PerkinsBradley_CE
public class DialogUtils {


    public static void notificationDialog(Context context) {
        final Switch toggleBtn;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Con Notifications");
        toggleBtn = new Switch(context);
        builder.setView(toggleBtn);
        builder.setPositiveButton("Reset Password", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        AlertDialog forgotDialog = builder.create();
        forgotDialog.show();

    }


}
