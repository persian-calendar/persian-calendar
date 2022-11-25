package com.byagowi.persiancalendar.ui.level

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.MenuItem
import android.view.Surface
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.FragmentLevelBinding
import com.byagowi.persiancalendar.ui.utils.SensorEventAnnouncer
import com.byagowi.persiancalendar.ui.utils.getCompatDrawable
import com.byagowi.persiancalendar.ui.utils.navigateSafe
import com.byagowi.persiancalendar.ui.utils.onClick
import com.byagowi.persiancalendar.ui.utils.setupUpNavigation

class LevelScreen : Fragment(R.layout.fragment_level) {

    private var provider: OrientationProvider? = null
    private var isStopped = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentLevelBinding.bind(view)
        binding.appBar.toolbar.also { toolbar ->
            toolbar.setTitle(R.string.level)
            toolbar.setupUpNavigation()
        }
        val activity = activity ?: return
        provider = OrientationProvider(activity, binding.levelView)
        val announcer = SensorEventAnnouncer(R.string.level)
        binding.levelView.onIsLevel = { isLevel -> announcer.check(activity, isLevel) }
        binding.bottomAppbar.menu.add(R.string.level).also {
            it.icon = binding.bottomAppbar.context.getCompatDrawable(R.drawable.ic_compass_menu)
            it.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        }.onClick {
            // If compass wasn't in backstack (level is brought from shortcut), navigate to it
            if (!findNavController().popBackStack(R.id.compass, false))
                findNavController().navigateSafe(LevelScreenDirections.actionLevelToCompass())
        }
        binding.fab.setOnClickListener {
            val provider = provider ?: return@setOnClickListener
            val stop = !provider.isListening
            binding.fab.setImageResource(if (stop) R.drawable.ic_stop else R.drawable.ic_play)
            binding.fab.contentDescription = getString(if (stop) R.string.stop else R.string.resume)
            isStopped = !stop
            if (stop) provider.startListening() else provider.stopListening()
            setRotationPrevention(stop)
        }
    }

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onResume() {
        super.onResume()
        if (!isStopped) provider?.startListening()
        setRotationPrevention(provider?.isListening == true)
    }

    private fun setRotationPrevention(stopRotation: Boolean) {
        // https://stackoverflow.com/a/20017878
        val current = if (stopRotation) activity?.windowManager?.defaultDisplay?.rotation else null
        activity?.requestedOrientation = when (current) {
            Surface.ROTATION_180 -> ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
            Surface.ROTATION_270 -> ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
            Surface.ROTATION_0 -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            Surface.ROTATION_90 -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            else -> ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    override fun onPause() {
        if (provider?.isListening == true) provider?.stopListening()
        setRotationPrevention(false)
        super.onPause()
    }
}
