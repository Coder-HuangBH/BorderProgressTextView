package com.hbh.borderprogresstextview

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.animation.LinearInterpolator
import androidx.annotation.ColorInt
import androidx.annotation.IntRange
import androidx.appcompat.widget.AppCompatTextView


class BorderProgressTextView : AppCompatTextView {


    private val TAG = BorderProgressTextView::class.java.simpleName
    private var mBorderPaint: Paint? = null //边框画笔
    private var mMaskPaint :Paint? = null //遮罩画笔
    private var mDest : Path? = null//渲染路径
    private var mPathMeasure : PathMeasure? = null//边框路径
    private var mMaskRectF : RectF? = null//遮罩层Rect
    private var mProgressAnimator : ValueAnimator? = null //动画
    private var mWidth = 0  //尺寸
    private var mHeight = 0  //尺寸
    private var mMaxProgress = 100 //总进度
    private var mCurrentProgress = 0 //当前进度
    private var mPathLength = 0f //边框总长度
    private var mCornerRadius = 0 //边框圆角
    private var mBorderWidth = 0 //边框宽度
    private var mBorderColor = 0 //边框颜色
    private var mMaskColor = 0 //遮罩层颜色(最好带透明度)
    private var mDuration = 3000 //动画总时长
    private var mRepeatCount = 0 //重复次数
    private var mListener : OnAnimationEventListener? = null //事件回调监听

    constructor(context: Context) : super(context) {
        init(null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(attrs)
    }

    private fun init(attrs: AttributeSet?) {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.BorderProgressTextView, 0, 0)
        mCornerRadius = ta.getDimensionPixelSize(R.styleable.BorderProgressTextView_bp_corner_radius, mCornerRadius)
        mBorderWidth = ta.getDimensionPixelSize(R.styleable.BorderProgressTextView_bp_border_width, mBorderWidth)
        mBorderColor = ta.getColor(R.styleable.BorderProgressTextView_bp_border_color, mBorderColor)
        mMaskColor = ta.getColor(R.styleable.BorderProgressTextView_bp_mask_color, mMaskColor)
        mDuration = ta.getInt(R.styleable.BorderProgressTextView_bp_duration, mDuration)
            .coerceAtLeast(500)
        mMaxProgress = mMaxProgress.coerceAtLeast(
            ta.getInt(R.styleable.BorderProgressTextView_bp_max_progress, mMaxProgress))
        mRepeatCount = ta.getInt(R.styleable.BorderProgressTextView_bp_repeat_count, mRepeatCount)
        mCurrentProgress = mMaxProgress
        ta.recycle()

        super.setPadding(
            mBorderWidth + paddingStart, mBorderWidth + paddingTop,
            mBorderWidth + paddingEnd, mBorderWidth + paddingBottom
        )
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        Log.d(TAG, "onSizeChanged")
        mWidth = w
        mHeight = h

        //圆角最大值为：宽高短的一边的一半
        mCornerRadius = (mWidth.coerceAtMost(mHeight) / 2).coerceAtMost(mCornerRadius)

        initMask()
        initBorder()
    }

    fun setMaskColor(@ColorInt maskColor: Int) {
        if (this.mMaskColor == maskColor) return
        this.mMaskColor = maskColor
        initMask()
        invalidate()
    }

    private fun initMask() {
        if (mMaskColor != 0) {
            mMaskRectF = RectF(0f, 0f, mWidth.toFloat(), mHeight.toFloat())
            mMaskPaint = Paint().apply {
                color = mMaskColor
                isAntiAlias = true
            }
        } else {
            mMaskRectF = null
            mMaskPaint = null
        }
    }

    fun setCornerRadius(@IntRange(from = 0) cornerRadius: Int) {
        if (this.mCornerRadius == cornerRadius) return
        this.mCornerRadius = cornerRadius
        initMask()
        initBorder()
        invalidate()
    }

    fun setBorderWidth(@IntRange(from = 0) borderWidth: Int) {
        this.mBorderWidth = borderWidth
        initBorder()
        super.setPadding(
            borderWidth + paddingStart, borderWidth + paddingTop,
            borderWidth + paddingEnd, borderWidth + paddingBottom
        )
    }


    override fun setPadding(
        @IntRange(from = 0) left: Int, @IntRange(from = 0) top: Int,
        @IntRange(from = 0) right: Int, @IntRange(from = 0) bottom: Int
    ) {
        super.setPadding(
            left + mBorderWidth, top + mBorderWidth,
            right + mBorderWidth, bottom + mBorderWidth
        )
    }

    fun setBorderColor(borderColor: Int) {
        this.mBorderColor = borderColor
        initBorder()
        invalidate()
    }

    private fun initBorder() {
        if (mBorderWidth == 0 || mBorderColor == 0) {
            mDest = null
            mPathMeasure = null
            mBorderPaint = null
            return
        }

        val halfBorderWidth: Float = (mBorderWidth shr 1).toFloat()
        val borderRadius: Float = (mCornerRadius - halfBorderWidth).coerceAtLeast(0f)
        val offset = mCornerRadius + (if (borderRadius > 0) halfBorderWidth / 2 else 0f)
        val mStartOffset: Int = mWidth / 2 - mCornerRadius
        val x: Float = mWidth - halfBorderWidth
        val y: Float = mHeight - halfBorderWidth

        val path = Path()
        //上面的路径
        path.moveTo(borderRadius + mStartOffset, halfBorderWidth)
        path.lineTo(mWidth - offset, halfBorderWidth)
        path.quadTo(x, halfBorderWidth, x, offset)
        //右边的路径
        path.lineTo(x, mHeight - offset)
        path.quadTo(x, y, mWidth - offset, y)
        //下面的路径
        path.lineTo(offset, y)
        path.quadTo(halfBorderWidth, y, halfBorderWidth, mHeight - offset)
        //左边的路径
        path.lineTo(halfBorderWidth, offset)
        path.quadTo(halfBorderWidth, halfBorderWidth, offset, halfBorderWidth)
        path.close()
        mPathMeasure = PathMeasure().apply {
            setPath(path, false)
            mPathLength = length
            mDest = Path()
        }
        mBorderPaint = Paint().apply {
            color = mBorderColor
            style = Paint.Style.STROKE
            strokeWidth = mBorderWidth.toFloat()
            isAntiAlias = true
        }
    }

    /**
     * 修改动画时长会停止动画，并将进度条重置
     */
    fun setDuration(@IntRange(from = 500) duration: Int) {
        this.mDuration = duration
        stopProgressAnimation()
        mCurrentProgress = mMaxProgress
        invalidate()
    }

    fun setRepeatCount(repeatCount: Int) {
        this.mRepeatCount = repeatCount
    }

    fun setCurrentProgress(progress : Int) {
        mCurrentProgress = (progress.coerceAtLeast(0)).coerceAtMost(mMaxProgress)
        invalidate()
    }

    fun startProgressAnimation() {
        stopProgressAnimation()
        mProgressAnimator = ValueAnimator.ofInt(mMaxProgress, 0).apply {
            duration = mDuration.toLong()
            interpolator = LinearInterpolator() //匀速插值器
            repeatCount = mRepeatCount
            repeatMode = ValueAnimator.REVERSE
            addUpdateListener { animation ->
                mCurrentProgress = animation.animatedValue as Int
                invalidate()
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationRepeat(animation: Animator) {
                    mListener?.onAnimationRepeat()
                }

                override fun onAnimationEnd(animation: Animator) {
                    mCurrentProgress = mMaxProgress
                    mListener?.onAnimationEnd()
                }
            })
            start()
        }
    }

    fun isAnimationRunning() : Boolean {
        return mProgressAnimator?.isRunning == true
    }

    fun resumeProgressAnimation() {
        mProgressAnimator?.resume()
    }

    fun pauseProgressAnimation() {
        mProgressAnimator?.pause()
    }

    fun stopProgressAnimation() {
        mProgressAnimator?.run {
            cancel()
            end()
        }
    }

    override fun onDraw(canvas: Canvas) {
        //背景遮罩层
        if (mMaskRectF != null && mMaskPaint != null) {
            mMaskPaint?.let { canvas.drawRoundRect(mMaskRectF!!,
                mCornerRadius.toFloat(), mCornerRadius.toFloat(), it) }
        }

        //进度从百分百递减到0
        if (mDest != null && mPathMeasure != null && mBorderPaint != null) {
            mDest!!.reset()
            mPathMeasure!!.getSegment(
                mPathLength * (1 - mCurrentProgress / (mMaxProgress * 1f)),
                mPathLength, mDest, true
            )
            canvas.drawPath(mDest!!, mBorderPaint!!)
        }
        super.onDraw(canvas)
    }

    fun setListener(listener: OnAnimationEventListener) {
        this.mListener = listener
    }
}