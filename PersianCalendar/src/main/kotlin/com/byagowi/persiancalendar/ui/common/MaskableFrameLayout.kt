// Copied from https://github.com/material-components/material-components-android/blob/7bc26e5/lib/java/com/google/android/material/carousel/MaskableFrameLayout.java
// then turned to Kotlin, simplified and removed use of internal things.
// We used to use the Material but since 1.10.0 it has become unusable, it just shows nothing and probably
// we shouldn't have used at the first place as it was intended to be used by carousel.
package com.byagowi.persiancalendar.ui.common

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Outline
import android.graphics.Path
import android.graphics.RectF
import android.os.Build
import android.os.Build.VERSION_CODES
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.FrameLayout
import androidx.annotation.RequiresApi
import com.google.android.material.shape.ShapeAppearanceModel
import com.google.android.material.shape.ShapeAppearancePathProvider
import com.google.android.material.shape.Shapeable

/** A [FrameLayout] than is able to mask itself and all children.  */
class MaskableFrameLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), Shapeable {
    private val maskRect = RectF()
    private var shapeAppearanceModel: ShapeAppearanceModel =
        ShapeAppearanceModel.builder(context, attrs, defStyleAttr, 0, 0).build()
    private val maskableDelegate = if (Build.VERSION.SDK_INT >= VERSION_CODES.TIRAMISU)
        MaskableDelegateV33(this) else MaskableDelegateV14()

    init {
        setShapeAppearanceModel(shapeAppearanceModel)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        onMaskChanged()
    }

    override fun setShapeAppearanceModel(shapeAppearanceModel: ShapeAppearanceModel) {
        this.shapeAppearanceModel = shapeAppearanceModel
        maskableDelegate.onShapeAppearanceChanged(this, shapeAppearanceModel)
    }

    override fun getShapeAppearanceModel(): ShapeAppearanceModel = shapeAppearanceModel

    private fun onMaskChanged() {
        if (width == 0) return
        // Translate the percentage into an actual pixel value of how much of this view should be
        // masked away.
        maskRect.set(0f, 0f, width.toFloat(), height.toFloat())
        maskableDelegate.onMaskChanged(this, maskRect)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        // Only handle touch events that are within the masked bounds of this view.
        if (!maskRect.isEmpty && event.action == MotionEvent.ACTION_DOWN) {
            val x = event.x
            val y = event.y
            if (!maskRect.contains(x, y)) return false
        }
        return super.onTouchEvent(event)
    }

    override fun dispatchDraw(canvas: Canvas) =
        maskableDelegate.maybeClip(canvas) { super.dispatchDraw(it) }

    /**
     * A delegate able to handle logic for when and how to mask a View based on the View's [ ] and mask bounds.
     */
    private abstract class MaskableDelegate {
        var shapeAppearanceModel: ShapeAppearanceModel? = null
        var maskBounds = RectF()
        val shapePath = Path()

        /**
         * Called due to changes in a delegate's shape, mask bounds or other parameters. Delegate
         * implementations should use this as an opportunity to ensure their method of clipping is
         * appropriate and invalidate the client view if necessary.
         *
         * @param view the client view
         */
        abstract fun invalidateClippingMethod(view: View)

        /**
         * Whether the client view should use canvas clipping to mask itself.
         *
         *
         * Note: It's important that no significant logic is run in this method as it is called from
         * dispatch draw, which should be as performant as possible. Logic for determining whether
         * compat clipping is used should be run elsewhere and stored for quick access.
         *
         * @return true if the client view should clip the canvas
         */
        abstract fun shouldUseCompatClipping(): Boolean

        /**
         * Called whenever the [ShapeAppearanceModel] of the client changes.
         *
         * @param view the client view
         * @param shapeAppearanceModel the update [ShapeAppearanceModel]
         */
        fun onShapeAppearanceChanged(view: View, shapeAppearanceModel: ShapeAppearanceModel) {
            this.shapeAppearanceModel = shapeAppearanceModel
            updateShapePath()
            invalidateClippingMethod(view)
        }

        /**
         * Called whenever the bounds of the clients mask changes.
         *
         * @param view the client view
         * @param maskBounds the updated bounds
         */
        fun onMaskChanged(view: View, maskBounds: RectF) {
            this.maskBounds = maskBounds
            updateShapePath()
            invalidateClippingMethod(view)
        }

        private val pathProvider = ShapeAppearancePathProvider()
        private fun updateShapePath() {
            if (!maskBounds.isEmpty && shapeAppearanceModel != null) {
                pathProvider.calculatePath(shapeAppearanceModel, 1f, maskBounds, shapePath)
            }
        }

        fun maybeClip(canvas: Canvas, op: (canvas: Canvas) -> Unit) {
            if (shouldUseCompatClipping() && !shapePath.isEmpty) {
                canvas.save()
                canvas.clipPath(shapePath)
                op(canvas)
                canvas.restore()
            } else op(canvas)
        }
    }

    /**
     * A [MaskableDelegate] implementation for API 14-32 that always clips using canvas
     * clipping.
     */
    private class MaskableDelegateV14 : MaskableDelegate() {
        override fun shouldUseCompatClipping() = true

        override fun invalidateClippingMethod(view: View) {
            if (shapeAppearanceModel == null || maskBounds.isEmpty) return
            view.invalidate()
        }
    }

    /**
     * A [MaskableDelegate] for API 33+ that uses [ViewOutlineProvider] to clip for all
     * shapes.
     *
     *
     * [Outline.setPath] was added in API 33 and allows using [ ] to clip for all shapes.
     */
    @RequiresApi(VERSION_CODES.TIRAMISU)
    private class MaskableDelegateV33(view: View) : MaskableDelegate() {
        init {
            view.outlineProvider = object : ViewOutlineProvider() {
                override fun getOutline(view: View, outline: Outline) {
                    if (!shapePath.isEmpty) outline.setPath(shapePath)
                }
            }
        }

        override fun shouldUseCompatClipping() = false

        override fun invalidateClippingMethod(view: View) {
            view.clipToOutline = true
            view.invalidateOutline()
        }
    }
}
