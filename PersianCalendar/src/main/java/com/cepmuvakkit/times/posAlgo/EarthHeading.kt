/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cepmuvakkit.times.posAlgo

/**
 * @author mgeden
 */
class EarthHeading(val heading: Double, private val mMetres: Long) {
    val kiloMetres: Long
        get() = mMetres / 1000
}
