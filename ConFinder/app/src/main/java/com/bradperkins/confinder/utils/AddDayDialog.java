package com.bradperkins.confinder.utils;

// Date 1/6/19
// 
// Bradley Perkins

// AID - 1809

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.bradperkins.confinder.R;

// PerkinsBradley_CE
public class AddDayDialog {

    public void showDialog(Context context){
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.custom_add_con_day);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

        String[] months = context.getResources().getStringArray(R.array.month);
        String[] days = context.getResources().getStringArray(R.array.days);
        String[] year = {"2018", "2019"};
        String[] amPm = {"am", "pm"};
        String[] hour = context.getResources().getStringArray(R.array.hour);
        String[] min = context.getResources().getStringArray(R.array.minute);

        //Month
        Spinner monthSpinner = dialog.findViewById(R.id.month_spin);
        ArrayAdapter<String> monthAdpater = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, months);
        monthAdpater.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        monthSpinner.setAdapter(monthAdpater);


        //Days
        Spinner daySpinner = dialog.findViewById(R.id.day_spin);
        ArrayAdapter<String> dayAdpater = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, days);
        dayAdpater.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        daySpinner.setAdapter(dayAdpater);

        //Year
        Spinner yearSpinner = dialog.findViewById(R.id.year_spin);
        ArrayAdapter<String> yearAdpater = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, year);
        yearAdpater.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        yearSpinner.setAdapter(yearAdpater);

        //Start Hour
        Spinner startHourSpin = dialog.findViewById(R.id.start_hour_spin);
        ArrayAdapter<String> startHourAdpater = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, hour);
        startHourAdpater.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        startHourSpin.setAdapter(startHourAdpater);

        //Start Min
        Spinner startMinSpin = dialog.findViewById(R.id.start_min_spin);
        ArrayAdapter<String> startMinAdpater = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, min);
        startMinAdpater.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        startMinSpin.setAdapter(startMinAdpater);

        //Start am/pm
        Spinner startAmPmSpin = dialog.findViewById(R.id.start_am_pm_spin);
        ArrayAdapter<String> startAmPmAdpater = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, amPm);
        startAmPmAdpater.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        startAmPmSpin.setAdapter(startAmPmAdpater);

        //End Hour
        Spinner endHourSpin = dialog.findViewById(R.id.end_hour_spin);
        ArrayAdapter<String> endHourAdpater = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, hour);
        endHourAdpater.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        endHourSpin.setAdapter(endHourAdpater);

        //End Min
        Spinner endMinSpin = dialog.findViewById(R.id.end_min_spin);
        ArrayAdapter<String> endMinAdpater = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, min);
        endMinAdpater.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        endMinSpin.setAdapter(endMinAdpater);

        //End am/pm
        Spinner endAmPmSpin = dialog.findViewById(R.id.end_am_pm_spin);
        ArrayAdapter<String> endAmPmAdpater = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, amPm);
        endAmPmAdpater.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        endAmPmSpin.setAdapter(endAmPmAdpater);





        Button dialogButton = dialog.findViewById(R.id.cancel_con_dialog_btn);
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
        dialog.getWindow().setAttributes(lp);


    }

}
