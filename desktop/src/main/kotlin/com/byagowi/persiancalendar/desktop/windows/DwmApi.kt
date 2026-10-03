package com.byagowi.persiancalendar.desktop.windows

import com.byagowi.persiancalendar.utils.logException
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.Structure
import com.sun.jna.platform.win32.WinDef.HWND
import com.sun.jna.ptr.IntByReference
import com.sun.jna.win32.StdCallLibrary
import java.awt.Window

@Suppress("FunctionName", "ClassName")
interface DwmApi : StdCallLibrary {
    fun DwmSetWindowAttribute(
        hwnd: HWND,
        dwAttribute: Int,
        pvAttribute: IntByReference,
        cbAttribute: Int,
    ): Int

    fun DwmExtendFrameIntoClientArea(hwnd: HWND, pMarMargins: MARGINS): Int

    @Structure.FieldOrder("cxLeftWidth", "cxRightWidth", "cyTopHeight", "cyBottomHeight")
    class MARGINS(
        @JvmField var cxLeftWidth: Int = 0,
        @JvmField var cxRightWidth: Int = 0,
        @JvmField var cyTopHeight: Int = 0,
        @JvmField var cyBottomHeight: Int = 0,
    ) : Structure()

    @Suppress("SpellCheckingInspection")
    enum class DWM_SYSTEMBACKDROP_TYPE {
        DWMSBT_AUTO, DWMSBT_NONE, DWMSBT_MAINWINDOW, DWMSBT_TRANSIENTWINDOW, DWMSBT_TABBEDWINDOW
    }

    companion object {
        private val INSTANCE: DwmApi? by lazy(LazyThreadSafetyMode.NONE) {
            runCatching {
                Native.load("dwmapi", DwmApi::class.java)
            }.onFailure(logException).getOrNull()
        }

        private fun Window.hwnd() = HWND(Pointer(Native.getWindowID(this)))

        fun applyWindowsDarkMode(window: Window, isDark: Boolean) {
            if ("win" in System.getProperty("os.name").lowercase()) runCatching {
                DwmApi.INSTANCE?.DwmSetWindowAttribute(
                    window.hwnd(),
                    // DWMWA_USE_IMMERSIVE_DARK_MODE = 20 is Windows 10 (Build 18985+) older
                    // versions needed 19 but skipped for simplicity
                    20,
                    IntByReference(if (isDark) 1 else 0),
                    Integer.BYTES,
                )
                window.repaint()
            }.onFailure(logException)
        }

        fun applyBackdrop(window: Window, backdrop: DWM_SYSTEMBACKDROP_TYPE) {
            if ("win" in System.getProperty("os.name").lowercase()) runCatching {
                val dwm = INSTANCE ?: return@runCatching
                val hwnd = window.hwnd()
                dwm.DwmExtendFrameIntoClientArea(hwnd, MARGINS(-1, -1, -1, -1))
                dwm.DwmSetWindowAttribute(
                    hwnd,
                    38, // DWMWA_SYSTEMBACKDROP_TYPE
                    IntByReference(backdrop.ordinal),
                    Integer.BYTES,
                )
                window.repaint()
            }.onFailure(logException)
        }
    }
}
