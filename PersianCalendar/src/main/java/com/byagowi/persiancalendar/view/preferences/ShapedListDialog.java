package com.byagowi.persiancalendar.view.preferences;

import android.content.DialogInterface;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.PreferenceDialogFragmentCompat;

import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.adapter.ShapedArrayAdapter;

import java.util.Arrays;

/**
 * Created by ebraminio on 2/21/16.
 */
public class ShapedListDialog extends PreferenceDialogFragmentCompat {

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        super.onPrepareDialogBuilder(builder);

        final ShapedListPreference listPref = (ShapedListPreference) getPreference();
        final CharSequence[] entriesValues = listPref.getEntryValues();

        int selectDialogLayout = R.layout.select_dialog_singlechoice_material;

        // It is better to avoid compat's single choice layout on Android 4.2.2 as its special issue
        // with RTL on making two radio button on left and right of each select item
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.JELLY_BEAN_MR1)
            selectDialogLayout = android.R.layout.select_dialog_singlechoice;

        ShapedArrayAdapter<CharSequence> entriesAdapter = new ShapedArrayAdapter<>(getContext(),
                selectDialogLayout, listPref.getEntries());

        int index = Arrays.asList(entriesValues).indexOf(listPref.getSelected());
        builder.setSingleChoiceItems(entriesAdapter, index, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                listPref.setSelected(entriesValues[which].toString());
                getDialog().dismiss();
            }
        });

        builder.setPositiveButton("", null);
    }

    @Override
    public void onDialogClosed(boolean b) {
    }
}
