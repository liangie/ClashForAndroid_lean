package com.lux.design.view

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.content.ContextCompat
import com.github.kr328.clash.design.R


class HProgressView : View {

    private var defProgressColor: Int = 0
    private val defNormalColor: Int
    private val defTxtDesColor: Int
    private val defTxtNumColor: Int
    private val defTxtSize: Float
    private val defProgressVWidth: Float
    private val defTxt2ProgressMargin: Float
    private val defHMargin: Float

    private var hMargin: Float = 0f
    private var progressColor: Int = 0
    private var normalColor: Int = 0
    private var txtDesColor: Int = 0
    private var txtNumColor: Int = 0
    private var txtSize: Float = 0f
    private var progressVWidth: Float = 0f
    private var txt2ProgressMargin: Float = 0f
    private var withAnim: Boolean
    private var withClick: Boolean
    private var mWidth: Int = 0
    private var mHeight: Int = 0

    private var startCenterX: Float = 0f
    private var startCenterY: Float = 0f
    private var endCenterX: Float = 0f
    private var endCenterY: Float = 0f
    private var cirlceRadius: Float = 0f
    private var top: Float = 0f
    private var bottom: Float = 0f

    private lateinit var recLeftCircle: RectF
    private lateinit var recRightCircle: RectF
    private lateinit var rectProgressArea: RectF
    private lateinit var rectProgressPass: RectF
    private val bounds: Rect

    private val normalPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val txtPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var mProgress: Float = 0f

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        /*
        给属性设置默认值
         */
        defProgressColor = ContextCompat.getColor(context, R.color.color_progress_1)
        defNormalColor = ContextCompat.getColor(context, R.color.white)
        defTxtDesColor = ContextCompat.getColor(context, R.color.white)
        defTxtNumColor = ContextCompat.getColor(context, R.color.white)

        defTxtSize = dip2px(16f)

        defProgressVWidth = dip2px(10f)

        defTxt2ProgressMargin = dip2px(30f)

        defHMargin = dip2px(12f)

        val a = context.obtainStyledAttributes(attrs, R.styleable.HProgressView)
        try {
            mProgress = a.getFloat(R.styleable.HProgressView_progress, 0f)
            hMargin = a.getDimension(R.styleable.HProgressView_h_margin, defHMargin)
            progressColor = a.getColor(R.styleable.HProgressView_progress_color, defProgressColor)
            normalColor = a.getColor(R.styleable.HProgressView_normal_color, defNormalColor)
            txtDesColor = a.getColor(R.styleable.HProgressView_text_des_color, defTxtDesColor)
            txtNumColor = a.getColor(R.styleable.HProgressView_text_num_color, defTxtNumColor)
            txtSize = a.getDimension(R.styleable.HProgressView_text_size, defTxtSize)
            progressVWidth =
                a.getDimension(R.styleable.HProgressView_progress_v_width, defProgressVWidth)
            txt2ProgressMargin = a.getDimension(
                R.styleable.HProgressView_text_to_progress_margin,
                defTxt2ProgressMargin
            )
            withAnim = a.getBoolean(R.styleable.HProgressView_with_anim, true)
            withClick = a.getBoolean(R.styleable.HProgressView_with_click, false)
        } finally {
            a.recycle()
        }

        if (mProgress < 0) mProgress = 0f
        if (mProgress > 100) mProgress = 100f

        normalPaint.color = normalColor
        normalPaint.style = Paint.Style.FILL

        progressPaint.color = progressColor
        progressPaint.style = Paint.Style.FILL

        txtPaint.style = Paint.Style.FILL
        txtPaint.textSize = txtSize

        bounds = Rect()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mWidth = w
        mHeight = h
        cirlceRadius = progressVWidth / 2
        startCenterX = cirlceRadius + hMargin
        endCenterX = mWidth - cirlceRadius - hMargin
        startCenterY = mHeight / 2 - txt2ProgressMargin / 2
        endCenterY = startCenterY

        top = startCenterY - cirlceRadius
        bottom = startCenterY + cirlceRadius
    }

    override fun onDraw(canvas: Canvas) {
        drawNormal(canvas)
        drawProgress(canvas)
//        这里暂时注释掉文字进度，若有需求可加上
//        drawDesTxt(canvas)
    }

    private fun drawNormal(canvas: Canvas) {
        recLeftCircle = RectF(hMargin, top, hMargin + progressVWidth, bottom)
        canvas.drawArc(recLeftCircle, 90f, 180f, true, normalPaint)

        recRightCircle = RectF(mWidth - progressVWidth - hMargin, top, mWidth - hMargin, bottom)
        canvas.drawArc(recRightCircle, -90f, 180f, true, normalPaint)

        rectProgressArea =
            RectF(hMargin + cirlceRadius, top, mWidth - hMargin - cirlceRadius, bottom)
        canvas.drawRect(rectProgressArea, normalPaint)
    }

    private fun drawProgress(canvas: Canvas) {
        if (mProgress > 0) {
            canvas.drawArc(recLeftCircle, 90f, 180f, true, progressPaint)

            if (mProgress >= 0.5f) {//为了防止mprogress为0.1-0.9时左边绘制有误
                val left = hMargin + cirlceRadius
                val right =
                    (mWidth - 2 * hMargin - progressVWidth) * mProgress / 100 + hMargin + cirlceRadius
                rectProgressPass = RectF(left, top, right, bottom)
                canvas.drawRect(rectProgressPass, progressPaint)

                rectProgressPass = RectF(right - cirlceRadius, top, right + cirlceRadius, bottom)
                canvas.drawArc(rectProgressPass, -90f, 180f, true, progressPaint)
            }
        }
    }

    private fun drawDesTxt(canvas: Canvas) {
        val des = "已完成${mProgress}%"
        txtPaint.getTextBounds(des, 0, des.length, bounds)
        val desW = bounds.width()
        val desH = bounds.height()
        val lw = txtPaint.measureText(des, 0, 3)

        txtPaint.color = txtDesColor
        canvas.drawText(
            des,
            0,
            3,
            mWidth / 2 - desW / 2f,
            mHeight / 2 + defTxt2ProgressMargin / 2 + desH / 2,
            txtPaint
        )

        txtPaint.color = txtNumColor
        canvas.drawText(
            des,
            3,
            des.length,
            mWidth / 2 - desW / 2f + lw,
            mHeight / 2 + defTxt2ProgressMargin / 2 + desH / 2,
            txtPaint
        )
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (withClick) {
            when (event.action) {
                MotionEvent.ACTION_MOVE -> dealMotionEvent(event.x, event.y, true)
                MotionEvent.ACTION_UP -> dealMotionEvent(event.x, event.y, withAnim)
            }
        }
        return true
    }

    private fun dealMotionEvent(x: Float, y: Float, withAnim: Boolean) {
        val rectLeftHalf = RectF(hMargin, top, hMargin + cirlceRadius, bottom)
        val rectRightHalf = RectF(mWidth - hMargin - cirlceRadius, top, mWidth - hMargin, bottom)
        if (rectLeftHalf.contains(x, y)) setProgress(0f)
        if (rectRightHalf.contains(x, y)) setProgress(100f)
        if (rectProgressArea.contains(x, y)) {
            val progress =
                (x - hMargin - cirlceRadius) / (mWidth - 2 * hMargin - progressVWidth) * 100f
            setProgress(String.format("%.1f", progress).toFloat(), withAnim)
        }
    }

    fun setWithAnim(withAnim: Boolean) {
        this.withAnim = withAnim
    }

    fun setWithClick(withClick: Boolean) {
        this.withClick = withClick
    }

    fun setProgress(progress: Float) = setProgress(progress, withAnim)


    fun setProgress(progress: Float, withAnim: Boolean) {
        if (progress < 0) mProgress = 0f
        if (progress > 100) mProgress = 100f
        if (withAnim) {
            val animator = ValueAnimator.ofFloat(mProgress, progress)
            animator.addUpdateListener { anim ->
                mProgress = String.format("%.1f", anim.animatedValue).toFloat()
                invalidate()
            }
            animator.interpolator = LinearInterpolator()
            animator.duration = (Math.abs(mProgress - progress) * 50).toLong()
            animator.start()
        } else {
            mProgress = progress
            invalidate()
        }
    }

    private fun dip2px(dipValue: Float) = context.resources.displayMetrics.density * dipValue
}