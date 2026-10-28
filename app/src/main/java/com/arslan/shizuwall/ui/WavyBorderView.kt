package com.arslan.shizuwall.ui

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PathMeasure
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import com.google.android.material.color.MaterialColors
import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.math.sin

class WavyBorderView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private val density = resources.displayMetrics.density
    private val amplitude = 1.5f * density
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3f * density
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        color = MaterialColors.getColor(this@WavyBorderView, androidx.appcompat.R.attr.colorPrimary)
    }
    private val measure = PathMeasure()
    private val wave = Path()
    private val pos = FloatArray(2)
    private val tan = FloatArray(2)
    private var waveK = 0f
    private var t = 0f
    private val animator = ValueAnimator.ofFloat(0f, 1f).apply {
        duration = 1600L
        repeatCount = ValueAnimator.INFINITE
        interpolator = LinearInterpolator()
        addUpdateListener { t = it.animatedValue as Float; invalidate() }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        val inset = amplitude + paint.strokeWidth / 2
        val rect = RectF(inset, inset, w - inset, h - inset)
        measure.setPath(Path().apply { addRoundRect(rect, rect.height() / 2, rect.height() / 2, Path.Direction.CW) }, true)
        waveK = (2 * PI * (measure.length / (10f * density)).roundToInt() / measure.length).toFloat()
    }

    override fun onVisibilityAggregated(isVisible: Boolean) {
        super.onVisibilityAggregated(isVisible)
        if (isVisible) animator.start() else animator.cancel()
    }

    override fun onDraw(canvas: Canvas) {
        val length = measure.length
        val segment = length * 0.4f
        val head = t * length
        val phase = t * 6 * PI.toFloat()
        wave.reset()
        var s = 0f
        while (s <= segment) {
            val d = (head - segment + s + length) % length
            measure.getPosTan(d, pos, tan)
            val offset = amplitude * sin(waveK * d - phase)
            val x = pos[0] - tan[1] * offset
            val y = pos[1] + tan[0] * offset
            if (s == 0f) wave.moveTo(x, y) else wave.lineTo(x, y)
            s += density
        }
        canvas.drawPath(wave, paint)
    }
}
