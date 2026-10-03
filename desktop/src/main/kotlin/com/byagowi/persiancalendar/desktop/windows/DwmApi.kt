package com.byagowi.persiancalendar.desktop.windows

import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.platform.win32.WinDef.HWND
import com.sun.jna.ptr.IntByReference
import com.sun.jna.win32.StdCallLibrary
import com.byagowi.persiancalendar.utils.logException
import com.sun.jna.Structure
import java.awt.Window

@Suppress("FunctionName")
interface DwmApi : StdCallLibrary {
    fun DwmSetWindowAttribute(
        hwnd: HWND,
        dwAttribute: Int,
        pvAttribute: IntByReference,
        cbAttribute: Int,
    ): Int

    fun DwmExtendFrameIntoClientArea(hwnd: HWND, pMarMargins: MARGINS): Int

    // Win32 MARGINS structure layout mapping for JNA
    @Structure.FieldOrder("cxLeftWidth", "cxRightWidth", "cyTopHeight", "cyBottomHeight")
    class MARGINS : Structure() {
        @JvmField var cxLeftWidth: Int = 0
        @JvmField var cxRightWidth: Int = 0
        @JvmField var cyTopHeight: Int = 0
        @JvmField var cyBottomHeight: Int = 0
    }

    companion object {
        private val INSTANCE: DwmApi? by lazy(LazyThreadSafetyMode.NONE) {
            runCatching {
                Native.load("dwmapi", DwmApi::class.java)
            }.onFailure(logException).getOrNull()
        }

        fun applyWindowsDarkMode(window: Window, isDark: Boolean) {
            if ("win" in System.getProperty("os.name").lowercase()) runCatching {
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

        fun applyAcrylicEffect(window: Window) {
            if ("win" in System.getProperty("os.name").lowercase()) runCatching {
                val dwm = INSTANCE ?: return@runCatching
                val hwndAddress = Native.getWindowID(window)
                val hwnd = HWND(Pointer(hwndAddress))

                // 1. DwmExtendFrameIntoClientArea
                val margins = MARGINS().also {
                    it.cxLeftWidth = -1
                    it.cxRightWidth = -1
                    it.cyTopHeight = -1
                    it.cyBottomHeight = -1
                }
                dwm.DwmExtendFrameIntoClientArea(hwnd, margins)

                val backdropVal = IntByReference(3) // Transient window backdrop (Acrylic-like effect)
                dwm.DwmSetWindowAttribute(
                    hwnd,
                    38, // DWMWA_SYSTEMBACKDROP_TYPE
                    backdropVal,
                    Integer.BYTES
                )

                window.repaint()
            }.onFailure(logException)
        }
    }
}
