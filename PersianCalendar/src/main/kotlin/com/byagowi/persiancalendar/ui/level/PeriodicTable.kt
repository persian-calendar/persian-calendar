package com.byagowi.persiancalendar.ui.level

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import androidx.core.graphics.withMatrix
import androidx.core.graphics.withTranslation
import androidx.fragment.app.FragmentActivity
import com.byagowi.persiancalendar.ui.map.ZoomableView
import com.google.android.material.dialog.MaterialAlertDialogBuilder

fun showPeriodicTableDialog(activity: FragmentActivity) {
    val view = object : ZoomableView(activity) {
        val cellSize = 100
        init {
            contentWidth = 100f * 18
            contentHeight = 100f * 9
            maxScale = 64f
        }

        val rect = RectF(0f, 0f, cellSize.toFloat(), cellSize.toFloat()).also {
            it.inset(cellSize * .02f, cellSize * .02f)
        }
        val rectPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
            it.style = Paint.Style.FILL
            it.textAlign = Paint.Align.CENTER
        }
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
            it.style = Paint.Style.FILL
            it.textAlign = Paint.Align.CENTER
            it.color = Color.BLACK
        }

        override fun zoomableDraw(canvas: Canvas, matrix: Matrix) {
            canvas.withMatrix(matrix) {
                (0 until 18).forEach { i ->
                    (0 until 9).forEach { j ->
                        withTranslation(i * cellSize.toFloat(), j * cellSize.toFloat()) {
                            val index = elementsIndices.getOrNull(i + j * 18)
                                ?: return@withTranslation
                            val details = elements[index - 1].split(",")
                            rectPaint.color = elementsColor.getValue(index).toInt()
                            drawRect(rect, rectPaint)
                            textPaint.textSize = cellSize * .35f
                            drawText(details[0], cellSize / 2f, cellSize * .37f, textPaint)
                            drawText(index.toString(), cellSize / 2f, cellSize * .70f, textPaint)
                            textPaint.textSize = cellSize * .15f
                            drawText(details[1], cellSize / 2f, cellSize * .87f, textPaint)
                        }
                    }
                }
            }
        }
    }

    MaterialAlertDialogBuilder(activity)
        .setView(view)
        .show()
}

private val elementsColor = mapOf(
    1 to 0xffb0c0e6, 7 to 0xffb0c0e6, 8 to 0xffb0c0e6, 9 to 0xffb0c0e6, 16 to 0xffb0c0e6,
    17 to 0xffb0c0e6, 35 to 0xffb0c0e6, 53 to 0xffb0c0e6,
    2 to 0xffe0b0e6, 10 to 0xffe0b0e6, 18 to 0xffe0b0e6, 36 to 0xffe0b0e6, 54 to 0xffe0b0e6,
    86 to 0xffe0b0e6, 118 to 0xffe0b0e6,
    5 to 0xffb0e0e6, 6 to 0xffb0e0e6, 14 to 0xffb0e0e6, 15 to 0xffb0e0e6, 32 to 0xffb0e0e6,
    34 to 0xffb0e0e6, 52 to 0xffb0e0e6,
).withDefault { 0xffeee8aa }

private val elementsIndices = buildList {
    var i = 1
    add(i++)
    addAll(List(16) { null })
    add(i++)
    repeat(2) {
        addAll(List(2) { i++ })
        addAll(List(10) { null })
        addAll(List(6) { i++ })
    }
    repeat(2) { addAll(List(18) { i++ }) }
    repeat(2) {
        addAll(List(2) { i++ })
        i += 14
        addAll(List(16) { i++ })
    }
    repeat(2) {
        i = if (it == 0) 57 else 89
        addAll(List(2) { null })
        addAll(List(14) { i++ })
        addAll(List(2) { null })
    }
}

private val elements = """
H,Hydrogen
He,Helium
Li,Lithium
Be,Beryllium
B,Boron
C,Carbon
N,Nitrogen
O,Oxygen
F,Fluorine
Ne,Neon
Na,Sodium
Mg,Magnesium
Al,Aluminium
Si,Silicon
P,Phosphorus
S,Sulfur
Cl,Chlorine
Ar,Argon
K,Potassium
Ca,Calcium
Sc,Scandium
Ti,Titanium
V,Vanadium
Cr,Chromium
Mn,Manganese
Fe,Iron
Co,Cobalt
Ni,Nickel
Cu,Copper
Zn,Zinc
Ga,Gallium
Ge,Germanium
As,Arsenic
Se,Selenium
Br,Bromine
Kr,Krypton
Rb,Rubidium
Sr,Strontium
Y,Yttrium
Zr,Zirconium
Nb,Niobium
Mo,Molybdenum
Tc,Technetium
Ru,Ruthenium
Rh,Rhodium
Pd,Palladium
Ag,Silver
Cd,Cadmium
In,Indium
Sn,Tin
Sb,Antimony
Te,Tellurium
I,Iodine
Xe,Xenon
Cs,Caesium
Ba,Barium
La,Lanthanum
Ce,Cerium
Pr,Praseodymium
Nd,Neodymium
Pm,Promethium
Sm,Samarium
Eu,Europium
Gd,Gadolinium
Tb,Terbium
Dy,Dysprosium
Ho,Holmium
Er,Erbium
Tm,Thulium
Yb,Ytterbium
Lu,Lutetium
Hf,Hafnium
Ta,Tantalum
W,Tungsten
Re,Rhenium
Os,Osmium
Ir,Iridium
Pt,Platinum
Au,Gold
Hg,Mercury
Tl,Thallium
Pb,Lead
Bi,Bismuth
Po,Polonium
At,Astatine
Rn,Radon
Fr,Francium
Ra,Radium
Ac,Actinium
Th,Thorium
Pa,Protactinium
U,Uranium
Np,Neptunium
Pu,Plutonium
Am,Americium
Cm,Curium
Bk,Berkelium
Cf,Californium
Es,Einsteinium
Fm,Fermium
Md,Mendelevium
No,Nobelium
Lr,Lawrencium
Rf,Rutherfordium
Db,Dubnium
Sg,Seaborgium
Bh,Bohrium
Hs,Hassium
Mt,Meitnerium
Ds,Darmstadtium
Rg,Roentgenium
Cn,Copernicium
Nh,Nihonium
Fl,Flerovium
Mc,Moscovium
Lv,Livermorium
Ts,Tennessine
Og,Oganesson
""".trim().split("\n")
