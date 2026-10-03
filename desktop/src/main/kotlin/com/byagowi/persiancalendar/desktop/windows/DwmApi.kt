package com.byagowi.persiancalendar.desktop.windows

import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.platform.win32.WinDef.HWND
import com.sun.jna.ptr.IntByReference
import com.sun.jna.win32.StdCallLibrary
import com.byagowi.persiancalendar.utils.logException
import java.awt.Window

@Suppress("FunctionName")
interface DwmApi : StdCallLibrary {
    fun DwmSetWindowAttribute(
        hwnd: HWND,
        dwAttribute: Int,
        pvAttribute: IntByReference,
        cbAttribute: Int,
    ): Int

    companion object {
        private val INSTANCE: DwmApi? by lazy(LazyThreadSafetyMode.NONE) {
            runCatching {
                Native.load("dwmapi", DwmApi::class.java)
            }.onFailure(logException).getOrNull()
        }

        fun applyWindowsDarkMode(window: Window, isDark: Boolean) {
            if (System.getProperty("os.name").lowercase().contains("win")) runCatching {
                DwmApi.INSTANCE?.DwmSetWindowAttribute(
                    HWND(Pointer(Native.getWindowID(window))),
                    // DWMWA_USE_IMMERSIVE_DARK_MODE = 20 is Windows 10 (Build 18985+) older
                    // versions needed 19 but skipped for simplicity
                    20,
                    IntByReference(if (isDark) 1 else 0),
                    Integer.BYTES,
                )
                window.repaint()
            }.onFailure(logException)
        }
    }
}
