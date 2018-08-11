package com.byagowi.persiancalendar.view.preferences

import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.EditTextPreference
import com.byagowi.persiancalendar.Constants

/**
 * Created by ebraminio on 2/16/16.
 */
class AthanNumericPreference : EditTextPreference {

  private var mDouble: Double? = null

  constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {}

  constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

  constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

  constructor(context: Context) : super(context) {}

  // http://stackoverflow.com/a/10848393
  override fun setText(text: String) {
    val wasBlocking = shouldDisableDependents()
    mDouble = parseDouble(text)
    persistString(mDouble?.toString())
    val isBlocking = shouldDisableDependents()
    if (isBlocking != wasBlocking) notifyDependencyChange(isBlocking)
    LocalBroadcastManager.getInstance(context)
        .sendBroadcast(Intent(Constants.LOCAL_INTENT_UPDATE_PREFERENCE))
  }

  override fun getText(): String? = if (mDouble != null) mDouble.toString() else null

  private fun parseDouble(text: String): Double? {
    return try {
      java.lang.Double.parseDouble(text)
    } catch (e: NumberFormatException) {
      null
    } catch (e: NullPointerException) {
      null
    }

  }
}
