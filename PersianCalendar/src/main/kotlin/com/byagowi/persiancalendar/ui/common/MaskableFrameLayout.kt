// Copied from https://github.com/material-components/material-components-android/blob/7bc26e5/lib/java/com/google/android/material/carousel/MaskableFrameLayout.java
// then turned to Kotlin, simplified and removed use of Material library internal things.
// We used to use the Material's impl but since 1.10.0 it has become unusable, it just shows nothing
// and probably  we shouldn't have used at the first place as it was intended to be used by carousel.
package com.byagowi.persiancalendar.ui.common

import android.content.Context
import android.graphics.Canvas
import android.graphics.Outline
import android.graphics.Path
import android.graphics.RectF
import android.os.Build
import android.os.Build.VERSION_CODES
import android.util.AttributeSet
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.FrameLayout
import androidx.annotation.RequiresApi
import androidx.core.graphics.withClip
import com.google.android.material.shape.ShapeAppearanceModel
import com.google.android.material.shape.ShapeAppearancePathProvider
import com.google.android.material.shape.Shapeable

/** A [FrameLayout] than is able to mask itself and all children.  */
class MaskableFrameLayout(context: Context, attrs: AttributeSet? = null) :
    FrameLayout(context, attrs), Shapeable {
    private val maskRect = RectF()
    private var shapeAppearanceModel: ShapeAppearanceModel =
        ShapeAppearanceModel.builder(context, attrs, 0, 0, 0).build()
    private val maskableDelegate = if (Build.VERSION.SDK_INT >= VERSION_CODES.TIRAMISU)
        MaskableDelegateV33(shapeAppearanceModel, this)
    else MaskableDelegateV14(shapeAppearanceModel)

    init {
        setShapeAppearanceModel(shapeAppearanceModel)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        maskRect.set(0f, 0f, width.toFloat(), height.toFloat())
        maskableDelegate.onMaskChanged(this, maskRect)
    }

    override fun setShapeAppearanceModel(shapeAppearanceModel: ShapeAppearanceModel) {
        this.shapeAppearanceModel = shapeAppearanceModel
        maskableDelegate.onShapeAppearanceChanged(this, shapeAppearanceModel)
    }

    override fun getShapeAppearanceModel(): ShapeAppearanceModel = shapeAppearanceModel

    override fun dispatchDraw(canvas: Canvas) =
        maskableDelegate.maybeClip(canvas) { super.dispatchDraw(this) }

    /**
     * A delegate able to handle logic for when and how to mask a View based on the View's [ ] and mask bounds.
     */
    private abstract class MaskableDelegate {
        protected abstract var shapeAppearanceModel: ShapeAppearanceModel
        protected var maskBounds = RectF()
        protected val shapePath = Path()

        /**
         * Called due to changes in a delegate's shape, mask bounds or other parameters. Delegate
         * implementations should use this as an opportunity to ensure their method of clipping is
         * appropriate and invalidate the client view if necessary.
         *
         * @param view the client view
         */
        protected abstract fun invalidateClippingMethod(view: View)

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
        protected abstract val shouldUseCompatClipping: Boolean

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
            if (!maskBounds.isEmpty)
                pathProvider.calculatePath(shapeAppearanceModel, 1f, maskBounds, shapePath)
        }

        inline fun maybeClip(canvas: Canvas, op: Canvas.() -> Unit) {
            if (shouldUseCompatClipping && !shapePath.isEmpty) canvas.withClip(shapePath, op)
            else op(canvas)
        }
    }

    /**
     * A [MaskableDelegate] implementation for API 14-32 that always clips using canvas
     * clipping.
     */
    private class MaskableDelegateV14(override var shapeAppearanceModel: ShapeAppearanceModel) :
        MaskableDelegate() {
        override val shouldUseCompatClipping = true

        override fun invalidateClippingMethod(view: View) {
            if (!maskBounds.isEmpty) view.invalidate()
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
    private class MaskableDelegateV33(
        override var shapeAppearanceModel: ShapeAppearanceModel, view: View
    ) : MaskableDelegate() {
        init {
            view.outlineProvider = object : ViewOutlineProvider() {
                override fun getOutline(view: View, outline: Outline) {
                    if (!shapePath.isEmpty) outline.setPath(shapePath)
                }
            }
        }

        override val shouldUseCompatClipping = false

        override fun invalidateClippingMethod(view: View) {
            view.clipToOutline = true
            view.invalidateOutline()
        }
    }
}
