package com.byagowi.persiancalendar.utils;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;

import com.byagowi.persiancalendar.Constants;

import java.lang.reflect.Field;

// https://gist.github.com/artem-zinnatullin/7749076
public class TypefaceUtils {

    /**
     * Using reflection to override default typeface
     * NOTICE: DO NOT FORGET TO SET TYPEFACE FOR APP THEME AS DEFAULT TYPEFACE WHICH WILL BE OVERRIDDEN
     */
    public static void overrideFont(String defaultFontNameToOverride, Typeface face) {
        try {
            final Field defaultFontTypefaceField = Typeface.class.getDeclaredField(defaultFontNameToOverride);
            defaultFontTypefaceField.setAccessible(true);
            defaultFontTypefaceField.set(null, face);
        } catch (Exception e) {
            Log.e("TAG", "Can not set custom font " + face + " instead of " + defaultFontNameToOverride);
        }
    }

    public static Typeface getAppFont(Context context) {
        return Typeface.createFromAsset(context.getAssets(), Constants.FONT_PATH);
    }

    public static Typeface getCalendarFragmentFont(Context context) {
        return isCustomFontEnabled()
                ? Typeface.create("sans-serif-light", Typeface.NORMAL)
                : getAppFont(context);
    }

    public static boolean isCustomFontEnabled() {
        return Utils.isArabicDigitSelected() || Utils.isNonArabicScriptSelected();
    }
}
