package com.byagowi.persiancalendar.ui.map

import android.annotation.SuppressLint
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RuntimeShader
import android.view.View
import androidx.fragment.app.FragmentActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder

@SuppressLint("NewApi")
fun showShaderDemoDialog(activity: FragmentActivity) {
    val view = object : View(activity) {
        private val shader = RuntimeShader(
            """
// https://twitter.com/notargs/status/1250468645030858753
uniform float time;
uniform vec2 resolution;

vec4 main(vec2 fragCoord)
{
    vec3 d = .5-vec3(fragCoord.xy,1)/resolution.y,p,o;
    for(int i=0;i<32;i++)
    {
        o=p;
        o.z-=time*9.;
        float a=o.z*.1;
        o.xy*=mat2(cos(a),sin(a),-sin(a),cos(a));
        p+=(.1-length(cos(o.xy)+sin(o.yz)))*d;
    }
    return vec4((sin(p)+vec3(2,5,9))/length(p)*vec3(1), 1);
}"""
        )
        private val paint = Paint().also { it.shader = shader }
        override fun onDraw(canvas: Canvas) {
            shader.setFloatUniform("time", System.nanoTime() / 1000000000f)
            shader.setFloatUniform("resolution", width.toFloat(), height.toFloat())
            canvas.drawPaint(paint)
            postInvalidate()
        }
    }
    MaterialAlertDialogBuilder(activity)
        .setView(view)
        .show()
}
