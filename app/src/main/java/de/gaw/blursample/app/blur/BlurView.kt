package de.gaw.blursample.app.blur

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Bitmap.Config.ARGB_8888
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PorterDuff
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes
import androidx.core.content.res.use
import androidx.core.graphics.withScale
import de.gaw.blursample.app.R
import kotlin.math.roundToInt

private const val MIN_BLUR_RADIUS = 0f
private const val MAX_BLUR_RADIUS = 25f

class BlurView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    @AttrRes defStyleAttr: Int = 0,
    @StyleRes defStyleRes: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {

    private var downscaledWidth: Int = 300 // Arbitrary, will be overwritten after measure
    private var downscaledHeight: Int = 300
    val downscaleFactor = 10f

    var radius: Float = 8f
        set(value) {
            field = value.coerceIn(MIN_BLUR_RADIUS, MAX_BLUR_RADIUS)
            invalidate()
        }

    init {
        context.obtainStyledAttributes(
            attrs,
            R.styleable.BlurView,
            defStyleAttr,
            defStyleRes,
        ).use {
            radius = it.getFloat(R.styleable.BlurView_blurRadius, radius)
        }

        setWillNotDraw(false)
    }

    private var renderScript: RenderScript? = null
    private var blurScript: ScriptIntrinsicBlur? = null

    private lateinit var unblurredContent: Bitmap
    private lateinit var blurredContent: Bitmap
    private lateinit var unblurredContentCanvas: Canvas

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        recreateBitmaps(width, height)

        renderScript = RenderScript.create(context)
        blurScript = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript))
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        recycleBitmaps()
        renderScript?.destroy()
        renderScript = null
        blurScript = null
    }

    override fun draw(canvas: Canvas) {
        if (radius <= 0 || width <= 0 || height <= 0) {
            super.draw(canvas)
        } else {
            // Clear the previous canvas and draw the unblurred content into a bitmap
            unblurredContentCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.MULTIPLY)
            unblurredContentCanvas.withScale(1f / downscaleFactor, 1f / downscaleFactor) {
                super.draw(unblurredContentCanvas)
            }

            // Blur the content bitmap with renderscript
            blurBitmap(
                inputBitmap = unblurredContent,
                radius = radius,
                outputBitmap = blurredContent,
            )

            // Draw blurred content on the view's canvas
            canvas.withScale(downscaleFactor, downscaleFactor) {
                canvas.drawBitmap(blurredContent, 0f, 0f, null)
            }
        }
    }

    override fun onDescendantInvalidated(child: View, target: View) {
        super.onDescendantInvalidated(child, target)
        // Re-blur when content changes
        invalidate()
    }

    override fun onSizeChanged(width: Int, height: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(width, height, oldw, oldh)
        recycleBitmaps()
        recreateBitmaps(width, height)
    }

    private fun recreateBitmaps(width: Int, height: Int) {
        if (width > 0 && height > 0) { // Bitmaps cannot be created with a width or height <= 0
            downscaledWidth = (width / downscaleFactor).roundToInt()
            downscaledHeight = (height / downscaleFactor).roundToInt()

            unblurredContent = Bitmap.createBitmap(downscaledWidth, downscaledHeight, ARGB_8888)
            unblurredContentCanvas = Canvas(unblurredContent)
            blurredContent = Bitmap.createBitmap(downscaledWidth, downscaledHeight, ARGB_8888)
        }
    }

    private fun recycleBitmaps() {
        if (::unblurredContent.isInitialized) {
            unblurredContent.recycle()
        }
        if (::blurredContent.isInitialized) {
            blurredContent.recycle()
        }
    }

    private fun blurBitmap(
        inputBitmap: Bitmap,
        radius: Float = 8f,
        outputBitmap: Bitmap,
    ): Bitmap {
        val tmpInputBitmapAllocation = Allocation.createFromBitmap(renderScript, inputBitmap)
        val tmpOutputBitmapAllocation = Allocation.createFromBitmap(renderScript, outputBitmap)

        blurScript?.apply {
            setRadius(radius)
            setInput(tmpInputBitmapAllocation)
            forEach(tmpOutputBitmapAllocation)
        } ?: error("RenderScript is not initialized")

        tmpOutputBitmapAllocation.copyTo(outputBitmap)
        return outputBitmap
    }
}