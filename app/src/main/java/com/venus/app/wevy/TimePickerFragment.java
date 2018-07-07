package com.venus.app.wevy;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialogFragment;
import android.support.v7.widget.AppCompatEditText;
import android.widget.TimePicker;

/**
 * Created by arnold on 13/08/17.
 */
public class TimePickerFragment extends AppCompatDialogFragment implements TimePickerDialog.OnTimeSetListener {
    AppCompatEditText editText = null;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Create a new instance of TimePickerDialog and return it
        return new TimePickerDialog(getActivity(), this, 0, 0, true);
    }

    @Override
    public void onTimeSet(TimePicker timePicker, int hours, int minutes) {
        String add1 = (hours < 10) ? "0" : "";
        String add2 = (minutes < 10) ? "0" : "";
        editText.setText(add1 + hours + ":" + add2 + minutes);
    }

    public void setView(AppCompatEditText editText){
        this.editText = editText;
    }
}
