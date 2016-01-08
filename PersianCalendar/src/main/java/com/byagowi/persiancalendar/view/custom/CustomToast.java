package com.byagowi.persiancalendar.view.custom;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.byagowi.persiancalendar.R;

/**
 * persian_calendar
 * Author: hamidsafdari22@gmail.com
 * Date: 1/8/16
 */
public class CustomToast {
    public static Toast makeToast(Context context, @NonNull CharSequence title, @NonNull CharSequence message, @NonNull int duration) {
        Toast toast = new Toast(context);

        LayoutInflater inflater = LayoutInflater.from(context);
        View toastView = inflater.inflate(R.layout.toast_custom, null);
        ((TextView) toastView.findViewById(R.id.toast_title)).setText(title);
        ((TextView) toastView.findViewById(R.id.toast_message)).setText(message);
        toast.setView(toastView);
        toast.setDuration(Toast.LENGTH_SHORT);

        return toast;
    }

    public static Toast makeToast(Context context, @NonNull @StringRes int title, @NonNull @StringRes int message, @NonNull int duration) {
        return makeToast(context, context.getString(title), context.getString(message), duration);
    }

    public static Toast makeToast(Context context, @NonNull @StringRes int title, @NonNull CharSequence message, @NonNull int duration) {
        return makeToast(context, context.getString(title), message, duration);
    }
}
