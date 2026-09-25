package com.blrp.game.gui.util

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.res.ResourcesCompat
import com.blrp.game.R
import kotlin.math.cos
import kotlin.math.sin


class SpeedometerView : View {
    private lateinit var arcBounds: RectF
    private var arcWidth: Float = 0.0f
    private var centerX = 0f
    private var centerY = 0f
    private var currentSpeed = 0
    private var divisionCount: Int = 0
    private var majorTextSize = 0f
    private var maxSpeed: Int = 0
    private var minorTextSize = 0f
    private lateinit var paintBackgroundArc: Paint
    private lateinit var paintProgressArc: Paint
    private lateinit var paintText: Paint
    private var radius = 0f
    private var startAngle: Float = 0.0f
    private var step: Int = 0

    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(attrs, 0)
    }

    private fun init(attrs: AttributeSet?, defStyleAttr: Int) {
        this.startAngle = 180.0f
        this.divisionCount = 13
        this.arcWidth = 16.0f
        this.maxSpeed = 0xF0
        this.step = 0xF0 / (13 - 1)
        val paint = Paint(1)
        paint.color = 1728053247
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        this.paintBackgroundArc = paint
        val paint2 = Paint(1)
        paint2.color = -1
        paint2.style = Paint.Style.STROKE
        paint2.strokeCap = Paint.Cap.ROUND
        this.paintProgressArc = paint2
        val paint3 = Paint(1)
        paint3.color = -1
        paint3.textAlign = Paint.Align.CENTER
        paint3.style = Paint.Style.FILL
        this.paintText = paint3
        this.arcBounds = RectF()
        val obtainStyledAttributes: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.SpeedometerView, defStyleAttr, 0)
        try {
            setCurrentSpeed(obtainStyledAttributes.getInt(R.styleable.SpeedometerView_speed_currentSpeed, defStyleAttr))
            setMaxSpeed(obtainStyledAttributes.getInt(R.styleable.SpeedometerView_speed_maxSpeed, 0xF0))
            val dimension: Float = obtainStyledAttributes.getDimension(R.styleable.SpeedometerView_speed_arcWidth, 16.0f)
            this.arcWidth = dimension
            paint.strokeWidth = dimension
            paint2.strokeWidth = arcWidth
            val resourceId: Int = obtainStyledAttributes.getResourceId(R.styleable.SpeedometerView_speed_font, 0)
            if (resourceId != 0) {
                paint3.setTypeface(ResourcesCompat.getFont(context, resourceId))
            }
        } finally {
            obtainStyledAttributes.recycle()
        }
    }



    fun setMaxSpeed(i: Int) {
        val adjustMaxSpeed = adjustMaxSpeed(i)
        if (this.maxSpeed != adjustMaxSpeed) {
            this.maxSpeed = adjustMaxSpeed
            this.step = adjustMaxSpeed / (this.divisionCount - 1)
            invalidate()
        }
    }


    fun setCurrentSpeed(i: Int) {
        if (this.currentSpeed != i) {
            this.currentSpeed = i
            invalidate()
        }
    }

    private val angleStep: Float
        get() = 180.0f / ((this.divisionCount - 1).toFloat())

    private fun adjustMaxSpeed(i: Int): Int {
        val coerceIn = i.coerceIn(120, 500)
        val i2 = this.divisionCount
        val i3 = coerceIn / (i2 - 1)
        return if (i3 % 5 == 0) coerceIn else ((i3 + 4) / 5) * 5 * (i2 - 1)
    }

    /* access modifiers changed from: protected */
    public override fun onMeasure(i: Int, i2: Int) {
        val size = MeasureSpec.getSize(i)
        setMeasuredDimension(size, resolveSize((size / 2) + (arcWidth.toInt()), i2))
    }

    /* access modifiers changed from: protected */
    public override fun onSizeChanged(i: Int, i2: Int, i3: Int, i4: Int) {
        super.onSizeChanged(i, i2, i3, i4)
        val f = i.toFloat()
        val f2 = f / 2.0f
        this.centerX = f2
        this.centerY = f2
        val f3 = 2f
        val strokeWidth = (this.arcWidth / f3) + (paintBackgroundArc.strokeWidth / f3)
        val f4 = f - strokeWidth
        arcBounds.set(strokeWidth, strokeWidth, f4, f4)
        this.radius = (f - ((5f) * strokeWidth)) / f3
        this.minorTextSize = f / 21.0f
        this.majorTextSize = f / 4.3f
    }

    /* access modifiers changed from: protected */
    public override fun onDraw(canvas: Canvas) {

        super.onDraw(canvas)
        canvas.drawArc(this.arcBounds, this.startAngle, 180.0f, false, this.paintBackgroundArc)
        val canvas2 = canvas
        canvas2.drawArc(
            this.arcBounds,
            this.startAngle,
            (currentSpeed.coerceIn(0, maxSpeed).toFloat() * 180.0f) / maxSpeed.toFloat(),
            false,
            this.paintProgressArc
        )
        paintText.textSize = this.minorTextSize
        val i = this.divisionCount
        var i2 = 0
        while (true) {
            var i3 = -1
            if (i2 < i) {
                val i4 = this.step * i2
                val f = 3f
                val radians =
                    (Math.toRadians((this.startAngle + ((i2.toFloat()) * angleStep)).toDouble())
                        .toFloat()).toDouble()
                val cos =
                    this.centerX + ((this.radius - (this.arcWidth * f)) * (cos(radians).toFloat()))
                val sin =
                    this.centerY + ((this.radius - (this.arcWidth * f)) * (sin(radians).toFloat()))
                val paint = this.paintText
                if (i4 > this.currentSpeed) {
                    i3 = -1879048193
                }
                paint.color = i3
                canvas.drawText(i4.toString(), cos, sin, this.paintText)
                i2++
            } else {
                paintText.color = -1
                paintText.textSize = this.majorTextSize
                canvas.drawText(
                    currentSpeed.toString(), (width.toFloat()) / 2.0f, (height.toFloat()) / 1.55f,
                    this.paintText
                )
                return
            }
        }
    }
}
