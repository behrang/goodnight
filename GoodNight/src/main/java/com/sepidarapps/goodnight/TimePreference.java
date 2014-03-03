package com.sepidarapps.goodnight;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TimePicker;

import java.util.Calendar;
import java.util.Date;

public class TimePreference extends DialogPreference {
    private int lastHour;
    private int lastMinute;
    private TimePicker timePicker;

    static int getHour(String time) {
        String[] hourMinute = time.split(":");
        return Integer.parseInt(hourMinute[0]);
    }

    static int getMinute(String time) {
        String[] hourMinute = time.split(":");
        return Integer.parseInt(hourMinute[1]);
    }

    public TimePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getString(index);
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        String time;
        if (restorePersistedValue) {
            time = getPersistedString("0:0");
        } else {
            time = defaultValue.toString();
            persistString(time);
        }
        lastHour = getHour(time);
        lastMinute = getMinute(time);
        setSummary(getSummary());
    }

    @Override
    protected View onCreateDialogView() {
        timePicker = new TimePicker(getContext());
        return timePicker;
    }

    @Override
    protected void onBindDialogView(@SuppressWarnings("NullableProblems") View view) {
        super.onBindDialogView(view);
        //noinspection ConstantConditions
        timePicker.setIs24HourView(DateFormat.is24HourFormat(timePicker.getContext()));
        timePicker.setCurrentHour(lastHour);
        timePicker.setCurrentMinute(lastMinute);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        if (positiveResult) {
            lastHour = timePicker.getCurrentHour();
            lastMinute = timePicker.getCurrentMinute();
            String time = lastHour + ":" + lastMinute;
            if (callChangeListener(time)) {
                persistString(time);
                notifyChanged();
            }
        }
    }

    @Override
    public CharSequence getSummary() {
        if (!isEnabled()) {
            return " ";
        }
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MINUTE, lastMinute);
        calendar.set(Calendar.HOUR_OF_DAY, lastHour);
        return DateFormat.getTimeFormat(getContext()).format(new Date(calendar.getTimeInMillis()));
    }
}
