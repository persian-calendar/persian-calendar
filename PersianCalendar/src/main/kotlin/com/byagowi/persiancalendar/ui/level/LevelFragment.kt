package com.byagowi.persiancalendar.ui.level

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.FragmentLevelBinding
import com.byagowi.persiancalendar.ui.utils.SensorEventAnnouncer
import com.byagowi.persiancalendar.ui.utils.getCompatDrawable
import com.byagowi.persiancalendar.ui.utils.navigateSafe
import com.byagowi.persiancalendar.ui.utils.onClick
import com.byagowi.persiancalendar.ui.utils.setupUpNavigation
import net.androgames.level.OrientationProvider

class LevelFragment : Fragment() {

    private var provider: OrientationProvider? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val activity = activity ?: return View(inflater.context)
        val binding = FragmentLevelBinding.inflate(inflater, container, false)
        binding.appBar.toolbar.also { toolbar ->
            toolbar.setTitle(R.string.level)
            toolbar.setupUpNavigation()
        }
        provider = OrientationProvider(activity, binding.levelView)
        val announcer = SensorEventAnnouncer(R.string.level)
        binding.levelView.onIsLevel = { isLevel -> announcer.check(activity, isLevel) }
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
        if (provider?.isListening != true) return // don't tweak orientation if no sensor available
        // https://stackoverflow.com/a/20017878
        activity?.requestedOrientation = when (activity?.windowManager?.defaultDisplay?.rotation) {
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
