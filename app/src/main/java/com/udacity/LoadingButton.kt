package com.udacity

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.annotation.ColorInt
import androidx.core.content.withStyledAttributes
import kotlin.properties.Delegates

class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var widthSize = 0
    private var heightSize = 0

    private var buttonBackgroundColorInt: Int = Color.BLACK
    private var buttonBackgroundLoadingColorInt: Int = Color.BLACK
    private var buttonTextColourInt: Int = Color.WHITE
    private var initialButtonText: String
    private var loadingButtonText: String

    private var percentageProgress = 0f
    private val valueAnimator = ValueAnimator.ofFloat(0f, 1.01f)
    
    private val buttonPaint = Paint().apply {
        isAntiAlias = true
        textSize = resources.getDimension(R.dimen.default_text_size)
    }

    private var buttonState: ButtonState by Delegates.observable<ButtonState>(ButtonState.Initial) { p, old, new ->
        when (new) {
            ButtonState.Loading -> {
                valueAnimator.duration = 3000
                valueAnimator.repeatCount = 1
                valueAnimator.addUpdateListener(object: ValueAnimator.AnimatorUpdateListener {
                    override fun onAnimationUpdate(animation: ValueAnimator?) {
                        animation?.animatedValue.let {
                            percentageProgress = it as Float
                            Log.i(TAG, "Animated Value Percentage Progress: " + percentageProgress)
                        }
                        invalidate()
                    }
                })
                valueAnimator.addListener(object: AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        buttonState = ButtonState.Completed
                    }
                })
                valueAnimator.start()
            }
            ButtonState.Completed -> {
                valueAnimator.removeAllUpdateListeners()
                valueAnimator.cancel()
                invalidate()
            }
        }
    }


    init {
        context.withStyledAttributes(attrs, R.styleable.LoadingButton) {
            // TODO CHANGE DEFAULT TO ACCENT COLOUR
            buttonBackgroundColorInt = getColor(R.styleable.LoadingButton_backgroundColor, buttonBackgroundColorInt)
            buttonBackgroundLoadingColorInt = getColor(R.styleable.LoadingButton_loadingColour, buttonBackgroundLoadingColorInt)
            buttonTextColourInt = getColor(R.styleable.LoadingButton_textColour, buttonTextColourInt)
        }
        initialButtonText = resources.getString(R.string.button_name)
        loadingButtonText = resources.getString(R.string.button_loading)
    }

    fun setCustomButtonState(newButtonState: ButtonState) {
        buttonState = newButtonState

    }

    override fun performClick(): Boolean {
        if (super.performClick()) return true
        //invalidate()
        return true
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        Log.i("LoadingButton", "Redrawing button....")
        canvas?.save()
        // Draw button background rectangle
        buttonPaint.color = buttonBackgroundColorInt
        canvas?.drawRect(0f, 0f, widthSize.toFloat(), heightSize.toFloat(), buttonPaint)

        if (valueAnimator.isRunning()) {
            // Draw loading rectangle colour
            canvas?.clipRect(0, 0, widthSize, heightSize)
            buttonPaint.color = buttonBackgroundLoadingColorInt
            canvas?.drawRect(
                    0f,
                    0f,
                    widthSize * percentageProgress,
                    heightSize.toFloat(),
                    buttonPaint
            )
            Log.i(TAG, "Percentage Progress: " + percentageProgress)
            Log.i(TAG, "Width Size: " + widthSize)
            Log.i(TAG, "Calculated Loading Width: " + widthSize / 100 * percentageProgress)

            // Draw loading arc
            buttonPaint.color = Color.YELLOW
            val radius = 20f
            val arcLeft = widthSize / 2f + widthSize / 4f
            val arcTop = 20f
            val arcRight = arcLeft + radius * 2
            val arcBottom = arcTop + radius * 2
            val sweepAngle = 360 * percentageProgress
            Log.i(TAG, "Sweep Angle: " + sweepAngle)
            canvas?.drawArc(
                    arcLeft,
                    arcTop,
                    arcRight,
                    arcBottom,
                    0f,
                    sweepAngle,
                    true,
                    buttonPaint
            )
        }

        // Draw loading text
        canvas?.restore()
        buttonPaint.color = buttonTextColourInt
        buttonPaint.textAlign = Paint.Align.CENTER

        var textToPaint = when (buttonState) {
            ButtonState.Loading -> loadingButtonText
            else -> initialButtonText
        }
        canvas?.drawText(
            textToPaint,
            (widthSize / 2).toFloat(),
            (heightSize / 2).toFloat() + buttonPaint.textSize / 2,
            buttonPaint
        )
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minw: Int = paddingLeft + paddingRight + suggestedMinimumWidth
        val w: Int = resolveSizeAndState(minw, widthMeasureSpec, 1)
        val h: Int = resolveSizeAndState(
            MeasureSpec.getSize(w),
            heightMeasureSpec,
            0
        )
        widthSize = w
        heightSize = h
        setMeasuredDimension(w, h)
    }

    companion object {
        private const val TAG ="LoadingButton"
    }

}