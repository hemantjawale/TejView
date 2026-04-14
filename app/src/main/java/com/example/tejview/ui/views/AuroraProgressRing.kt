package com.example.tejview.ui.views

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.content.ContextCompat
import com.example.tejview.R

/**
 * Custom circular progress ring with Aurora glow effect.
 * Used for serenity score and other circular metrics on the dashboard.
 */
class AuroraProgressRing @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var progress = 0f
    private var maxProgress = 100f
    private var animatedProgress = 0f

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 10f
        color = Color.parseColor("#1AA78BFA")
        strokeCap = Paint.Cap.ROUND
    }

    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 10f
        strokeCap = Paint.Cap.ROUND
    }

    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 16f
        strokeCap = Paint.Cap.ROUND
        maskFilter = BlurMaskFilter(12f, BlurMaskFilter.Blur.NORMAL)
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        textSize = 28f
        typeface = Typeface.create("sans-serif-light", Typeface.NORMAL)
    }

    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        textSize = 16f
    }

    private val rect = RectF()
    private var gradientShader: SweepGradient? = null

    private val violetColor = Color.parseColor("#A78BFA")
    private val emeraldColor = Color.parseColor("#34D399")
    private val violetGlow = Color.parseColor("#407C3AED")
    private val emeraldGlow = Color.parseColor("#3310B981")

    init {
        // Hardware acceleration needed for blur
        setLayerType(LAYER_TYPE_SOFTWARE, null)

        // Resolve text color from theme
        val typedArray = context.obtainStyledAttributes(intArrayOf(R.attr.auroraTextPrimary))
        val textColor = typedArray.getColor(0, Color.parseColor("#F0F0F5"))
        typedArray.recycle()
        textPaint.color = textColor

        val typedArray2 = context.obtainStyledAttributes(intArrayOf(R.attr.auroraTextTertiary))
        val labelColor = typedArray2.getColor(0, Color.parseColor("#6B7280"))
        typedArray2.recycle()
        labelPaint.color = labelColor
    }

    fun setProgress(value: Float, animate: Boolean = true) {
        progress = value.coerceIn(0f, maxProgress)
        if (animate) {
            ValueAnimator.ofFloat(0f, progress).apply {
                duration = 1200
                interpolator = AccelerateDecelerateInterpolator()
                addUpdateListener { animation ->
                    animatedProgress = animation.animatedValue as Float
                    invalidate()
                }
                start()
            }
        } else {
            animatedProgress = progress
            invalidate()
        }
    }

    fun setMaxProgress(max: Float) {
        maxProgress = max
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val padding = 20f
        rect.set(padding, padding, w - padding, h - padding)

        val cx = w / 2f
        val cy = h / 2f
        gradientShader = SweepGradient(cx, cy, intArrayOf(violetColor, emeraldColor, violetColor), null)
        progressPaint.shader = gradientShader
        glowPaint.shader = SweepGradient(cx, cy, intArrayOf(violetGlow, emeraldGlow, violetGlow), null)

        textPaint.textSize = h * 0.24f
        labelPaint.textSize = h * 0.12f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Background ring
        canvas.drawArc(rect, -90f, 360f, false, backgroundPaint)

        val sweepAngle = (animatedProgress / maxProgress) * 360f

        // Glow effect
        canvas.drawArc(rect, -90f, sweepAngle, false, glowPaint)

        // Progress arc
        canvas.drawArc(rect, -90f, sweepAngle, false, progressPaint)

        // Center text
        val cx = width / 2f
        val cy = height / 2f
        canvas.drawText("${animatedProgress.toInt()}", cx, cy + textPaint.textSize * 0.1f, textPaint)
    }
}
