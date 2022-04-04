package com.byagowi.persiancalendar.ui.utils

import android.view.View
import androidx.fragment.app.Fragment
import kotlin.properties.ReadOnlyProperty

/**
 * This relies on view ability to hold arbitrary objects, uses field name hash to cache
 * result of given lambda query from the view and calls 'bind' lambda lazily
 * when it misses view's cache.
 *
 * As it stores lambda's result inside the view's cache it doesn't need a clean up
 * on fragment's view lifecycle as the view itself will be purged along the cache store
 * and whenever the field is needed again and it misses the cache it will execute the
 * 'bind' lambda again.
 *
 * It is inspired from https://github.com/wada811/ViewBinding-ktx
 *
 * Created by Farhad Beigirad on 3/1/22.
 */
fun <T> viewKeeper(bind: (View) -> T): ReadOnlyProperty<Fragment, T> {
    return ReadOnlyProperty { fragment, property ->
        val tag = property.name.hashCode()
        val fragmentView = fragment.requireView()
        @Suppress("UNCHECKED_CAST")
        fragmentView.getTag(tag) as? T ?: bind(fragmentView).also { fragmentView.setTag(tag, it) }
    }
}
