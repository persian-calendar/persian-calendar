package net.androgames.level

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.Surface
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.FragmentLevelBinding
import com.byagowi.persiancalendar.utils.getCompatDrawable
import com.byagowi.persiancalendar.utils.navigateSafe
import com.byagowi.persiancalendar.utils.onClick
import com.byagowi.persiancalendar.utils.setupUpNavigation
import net.androgames.level.orientation.OrientationProvider

/*
 *  This file is part of Level (an Android Bubble Level).
 *  <https://github.com/avianey/Level>
 *
 *  Copyright (C) 2014 Antoine Vianey
 *
 *  Level is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Level is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Level. If not, see <http://www.gnu.org/licenses/>
 */
class LevelFragment : Fragment() {
    private var provider: OrientationProvider? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val activity = activity ?: return View(inflater.context)
        val binding = FragmentLevelBinding.inflate(inflater, container, false)
        binding.appBar.toolbar.also {
            it.setTitle(R.string.level)
            it.setupUpNavigation()
            it.setNavigationOnClickListener { findNavController().navigateUp() }
        }
        provider = OrientationProvider(activity, binding.levelView)
        binding.bottomAppbar.menu.add(R.string.level).also {
            it.icon = binding.bottomAppbar.context.getCompatDrawable(R.drawable.ic_compass_menu)
            it.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
            it.onClick {
                // If compass wasn't in backstack (level is brought from shortcut), navigate to it
                if (!findNavController().popBackStack(R.id.compass, false))
                    findNavController().navigateSafe(LevelFragmentDirections.actionLevelToCompass())
            }
        }
        binding.fab.setOnClickListener {
            val provider = provider ?: return@setOnClickListener
            val stop = !provider.isListening
            binding.fab.setImageResource(if (stop) R.drawable.ic_stop else R.drawable.ic_play)
            binding.fab.contentDescription = getString(if (stop) R.string.stop else R.string.resume)
            if (stop) provider.startListening() else provider.stopListening()
        }
        return binding.root
    }

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onResume() {
        super.onResume()
        provider?.startListening()
        // https://stackoverflow.com/a/20017878
        val rotation = (activity ?: return).windowManager.defaultDisplay.rotation
        activity?.requestedOrientation = when (rotation) {
            Surface.ROTATION_180 -> ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
            Surface.ROTATION_270 -> ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
            Surface.ROTATION_0 -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            Surface.ROTATION_90 -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            else -> ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    override fun onPause() {
        if (provider?.isListening == true) provider?.stopListening()
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        super.onPause()
    }
}
