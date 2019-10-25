package com.byagowi.persiancalendar.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityManager;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.entities.AbstractEvent;
import com.byagowi.persiancalendar.entities.CityItem;
import com.byagowi.persiancalendar.entities.DeviceCalendarEvent;
import com.byagowi.persiancalendar.entities.ShiftWorkRecord;
import io.github.persiancalendar.praytimes.Coordinate;
import com.byagowi.persiancalendar.service.ApplicationService;
import com.byagowi.persiancalendar.service.UpdateWorker;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static android.content.Context.ACCESSIBILITY_SERVICE;
import static com.byagowi.persiancalendar.ConstantsKt.*;
import static com.byagowi.persiancalendar.utils.FunctionsKt.*;
import static com.byagowi.persiancalendar.utils.UtilsKt.*;

public class Utils {


}
