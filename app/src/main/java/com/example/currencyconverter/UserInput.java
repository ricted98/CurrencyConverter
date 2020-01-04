package com.example.currencyconverter;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;

import java.util.Date;

public class UserInput extends AppCompatActivity {
    private static Context mContext;
    static String userDateStr = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_input);
        mContext = this.getApplicationContext();
    }

    public static Context getAppContext(){
        return mContext;
    }

    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            c.add(Calendar.DAY_OF_MONTH, -1);
            int yearMax = c.get(Calendar.YEAR);
            int monthMax = c.get(Calendar.MONTH);
            int dayMax = c.get(Calendar.DAY_OF_MONTH);
            Date maxDate = c.getTime();
            c.add(Calendar.DAY_OF_MONTH, -90);
            Date minDate = c.getTime();

            // Create a new instance of DatePickerDialog and return it
            DatePickerDialog datePickerDialog =
                    new DatePickerDialog(getActivity(), this, yearMax, monthMax, dayMax);

            datePickerDialog.getDatePicker().setMaxDate(maxDate.getTime());
            datePickerDialog.getDatePicker().setMinDate(minDate.getTime());
            return datePickerDialog;
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            Calendar c = Calendar.getInstance();
            c.set(year, month, day);
            if(c.isWeekend()){
                if(Calendar.SUNDAY == c.get(Calendar.DAY_OF_WEEK)){
                    c.add(Calendar.DAY_OF_MONTH, -2);
                } else {
                    c.add(Calendar.DAY_OF_MONTH, -1);
                }
            }
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            userDateStr = sdf.format(c.getTime());
            Intent intent = new Intent(getAppContext(), MainActivity.class);
            intent.putExtra("UserDate", userDateStr);
            startActivity(intent);
        }
    }

    public void showDatePickerDialog(View v) {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getFragmentManager(), "datePicker");
    }
}
