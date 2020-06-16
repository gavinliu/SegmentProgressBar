package cn.gavinliu.android.widget

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator


/**
 * @author Gavin Liu
 *
 * Created on 2020/06/14.
 */
class SegmentProgressBar : View {

    constructor(context: Context?) : super(context) {
        init(context)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context, attrs, defStyleAttr)
    }

    private val paint: Paint = Paint().apply {
        isAntiAlias = true
    }

    private var middleHeight: Float = 0F

    private var bgCircleCenterX: Float = 0F
    private var bgCircleCenterY: Float = 0F
    private var bgCircleRadius: Float = 0F

    private var bgColor: Int = 0

    private var bgBarHeight: Float = 0F
    private var bgBarRect: RectF = RectF()

    private var segmentBarHeight: Float = 0F
    private var segmentBarSpacing: Float = 0F
    private var segmentBarBgColor: Int = 0

    private var maxProgress: Float = 0F
    private var currentProgress: Float = 0F
    private var progressValue: Float = 0F
        set(value) {
            field = value
            updateProgress()
        }

    private var animator: Animator? = null

    private var bars: ArrayList<SegmentBar> = ArrayList()

    private var flagDrawable: Drawable? = null
    private var flagDrawablePadding = 0

    private fun init(
        context: Context? = null,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
    ) {
        attrs?.let {
            context?.obtainStyledAttributes(attrs, R.styleable.SegmentProgressBar)
                ?.also { typeArray ->

                    typeArray.getResourceId(R.styleable.SegmentProgressBar_spbFlagIcon, 0).let {
                        flagDrawable = context.getDrawable(it)
                    }
                    flagDrawablePadding = typeArray.getDimensionPixelSize(
                        R.styleable.SegmentProgressBar_spbFlagIconPadding,
                        0
                    )

                    segmentBarHeight = typeArray.getDimensionPixelSize(
                        R.styleable.SegmentProgressBar_spbSegmentBarHeight,
                        0
                    ).toFloat()
                    segmentBarSpacing = typeArray.getDimensionPixelSize(
                        R.styleable.SegmentProgressBar_spbSegmentBarSpacing,
                        0
                    ).toFloat()

                    bgBarHeight = typeArray.getDimensionPixelSize(
                        R.styleable.SegmentProgressBar_spbBgBarHeight,
                        0
                    ).toFloat()

                    bgCircleRadius = typeArray.getDimensionPixelSize(
                        R.styleable.SegmentProgressBar_spbBgCircleRadius,
                        0
                    ).toFloat()

                    bgColor = typeArray.getColor(R.styleable.SegmentProgressBar_spbBgColor, 0)
                    segmentBarBgColor =
                        typeArray.getColor(R.styleable.SegmentProgressBar_spbSegmentBarBgColor, 0)

                    minimumHeight = (bgCircleRadius * 2).toInt()
                }?.recycle()
        }

    }

    /**
     *
     * @param data
     */
    fun setData(data: List<Segment>) {
        bars.clear()
        maxProgress = 0F
        data.forEachIndexed { index, segment ->

            bars.add(
                SegmentBar(
                    index = index,
                    size = data.size,
                    start = maxProgress,
                    value = segment.progress.toFloat(),
                    bgColor = segmentBarBgColor,
                    currentColor = segment.color
                )
            )
            maxProgress += segment.progress
        }
        requestLayout()
    }

    /**
     *
     */
    fun progress(progress: Int) {
        currentProgress = progress.toFloat()
        updateProgress()
    }

    /**
     *
     */
    fun progressPlus() {
        val current = currentProgress
        currentProgress++
        checkCurrentProgress()
        startAnim(current, currentProgress)
    }


    /**
     *
     */
    fun progressMinus() {
        val current = currentProgress
        currentProgress--
        checkCurrentProgress()
        startAnim(current, currentProgress)
    }

    private fun startAnim(start: Float, end: Float) {
        if (animator?.isRunning == true) {
            animator?.cancel()
        }
        animator = ObjectAnimator.ofFloat(this, "progressValue", start, end).also {
            it.duration = 250
            it.interpolator = AccelerateDecelerateInterpolator()
            it.start()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val defaultWidth = getMeasureSize(suggestedMinimumWidth, widthMeasureSpec)
        val defaultHeight = getMeasureSize(suggestedMinimumHeight, heightMeasureSpec)
        setMeasuredDimension(defaultWidth, defaultHeight)

        middleHeight = defaultHeight / 2F

        bgCircleCenterX = defaultWidth - bgCircleRadius
        bgCircleCenterY = middleHeight

        flagDrawable?.setBounds(
            (bgCircleCenterX - (bgCircleRadius - flagDrawablePadding)).toInt(),
            (bgCircleCenterY - (bgCircleRadius - flagDrawablePadding)).toInt(),
            (bgCircleCenterX + (bgCircleRadius - flagDrawablePadding)).toInt(),
            (bgCircleCenterY + (bgCircleRadius - flagDrawablePadding)).toInt()
        )

        bgBarRect.left = 0F
        bgBarRect.top = middleHeight - bgBarHeight / 2F
        bgBarRect.right = bgBarRect.left + defaultWidth
        bgBarRect.bottom = bgBarRect.top + bgBarHeight

        val barMaxWidth =
            defaultWidth - segmentBarSpacing * 2 - bgCircleRadius * 2 - segmentBarSpacing * (bars.size - 1)

        var currentRight = 0F

        bars.forEach { bar ->
            val barWith = 1.0f * barMaxWidth * bar.value / maxProgress

            bar.rect.left = currentRight + segmentBarSpacing
            bar.rect.top = middleHeight - segmentBarHeight / 2F
            bar.rect.right = bar.rect.left + barWith
            bar.rect.bottom = bar.rect.top + segmentBarHeight

            currentRight = bar.rect.right

            bar.onMeasure()
        }

    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas?.let {
            // Draw BG
            paint.color = bgColor
            canvas.drawCircle(bgCircleCenterX, bgCircleCenterY, bgCircleRadius, paint)
            canvas.drawRoundRect(bgBarRect, bgBarHeight, bgBarHeight, paint)

            // Draw Flag
            flagDrawable?.draw(canvas)

            // Draw Bars
            bars.forEach { it.onDraw(canvas, paint) }
        }
    }

    private fun getMeasureSize(size: Int, measureSpec: Int): Int {
        var result = size
        val specMode = MeasureSpec.getMode(measureSpec)
        val specSize = MeasureSpec.getSize(measureSpec)

        when (specMode) {
            MeasureSpec.EXACTLY -> result = specSize
            MeasureSpec.AT_MOST -> result = size
            MeasureSpec.UNSPECIFIED -> result = size
        }
        return result
    }

    private fun checkCurrentProgress() {
        if (currentProgress < 0) {
            currentProgress = 0F
        }
        if (currentProgress > maxProgress) {
            currentProgress = maxProgress
        }
    }

    private fun updateProgress() {
        for (bar in bars) {
            when {
                bar.contains(progressValue) -> {
                    bar.current = progressValue - bar.start
                }

                progressValue >= bar.start + bar.value -> {
                    bar.current = bar.value
                }

                else -> {
                    bar.current = 0F
                }
            }

            bar.updateCurrentPath()
        }

        postInvalidate()
    }


    data class Segment(val progress: Int, val color: Int)

    private class SegmentBar(
        private val index: Int,
        private val size: Int,
        val start: Float,
        val value: Float,
        val bgColor: Int,
        val currentColor: Int,
        var current: Float = 0F,
        val rect: RectF = RectF()
    ) {

        private val path: Path = Path()

        private val catPath = Path()

        private val currentPath = Path()

        private fun isFirst(): Boolean = index == 0

        private fun isLast(): Boolean = index == size - 1

        fun contains(progress: Float): Boolean = progress >= start && progress <= start + value

        fun onMeasure() {
            path.reset()

            when {
                isFirst() -> {
                    rect.right += rect.height() / 2F
                    path.addRoundRect(rect, rect.height(), rect.height(), Path.Direction.CW)
                }
                isLast() -> {
                    rect.left -= rect.height() / 2F
                    path.addRoundRect(rect, rect.height(), rect.height(), Path.Direction.CW)
                }
                else -> {
                    path.addRect(rect, Path.Direction.CW)
                }
            }

            if (isFirst()) {
                val catRectF = RectF(rect)
                catRectF.left = catRectF.right - rect.height() / 2F
                catPath.reset()
                catPath.addRect(catRectF, Path.Direction.CW)
                path.op(catPath, Path.Op.DIFFERENCE)
            } else if (isLast()) {
                val catRectF = RectF(rect)
                catRectF.right = catRectF.left + rect.height() / 2F
                catPath.reset()
                catPath.addRect(catRectF, Path.Direction.CW)
                path.op(catPath, Path.Op.DIFFERENCE)
            }

            updateCurrentPath()
        }

        fun updateCurrentPath() {
            currentPath.reset()

            if (current == value) {
                currentPath.addPath(path)
                return
            }

            val currentRectF = RectF(rect)

            currentRectF.right = currentRectF.left + currentRectF.width() * current / value

            when {
                isFirst() -> {
                    currentPath.addRoundRect(
                        currentRectF,
                        currentRectF.height(),
                        currentRectF.height(),
                        Path.Direction.CW
                    )
                }

                isLast() -> {
                    currentPath.addRoundRect(
                        currentRectF,
                        currentRectF.height(),
                        currentRectF.height(),
                        Path.Direction.CW
                    )
                    currentPath.op(catPath, Path.Op.DIFFERENCE)
                }

                else -> {
                    currentPath.addRect(currentRectF, Path.Direction.CW)
                }
            }
        }

        fun onDraw(canvas: Canvas?, paint: Paint) {
            paint.color = bgColor
            canvas?.drawPath(path, paint)

            paint.color = currentColor
            canvas?.drawPath(currentPath, paint)
        }

    }

}
