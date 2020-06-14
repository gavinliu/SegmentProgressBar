package cn.gavinliu.android.widget

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View


/**
 * @author Gavin Liu
 *
 * Created on 2020/06/14.
 */
class SegmentProgressBar : View {

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    private val paint: Paint = Paint().apply {
        isAntiAlias = true
    }

    private val minHeight: Int = 32 * 3

    private var middleHeight: Float = 0F
    private var centerX: Float = 0F
    private var centerY: Float = 0F
    private var radius: Float = minHeight / 2F

    private var middleBarHeight: Float = 14 * 3F
    private var middleBarRect: RectF = RectF()

    private var segmentBarHeight: Float = 10 * 3F
    private var segmentBarSpacing = 2 * 3F

    private var maxProgress = 0
    private var currentProgress = 0

    private var bars: ArrayList<Bar> = ArrayList()

    init {
        minimumHeight = minHeight
    }

    /**
     *
     * @param data
     */
    fun setData(data: List<Int>) {
        bars.clear()
        maxProgress = 0
        data.forEachIndexed { index, value ->
            bars.add(
                Bar(
                    index,
                    data.size,
                    maxProgress,
                    value
                )
            )
            maxProgress += value
        }
        requestLayout()
    }

    /**
     *
     */
    fun progress(progress: Int) {
        currentProgress = progress
        updateProgress()
    }

    /**
     *
     */
    fun progressPlus() {
        currentProgress++
        updateProgress()
    }

    /**
     *
     */
    fun progressMinus() {
        currentProgress--
        updateProgress()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val defaultWidth = getMeasureSize(suggestedMinimumWidth, widthMeasureSpec)
        val defaultHeight = getMeasureSize(suggestedMinimumHeight, heightMeasureSpec)
        setMeasuredDimension(defaultWidth, defaultHeight)

        middleHeight = defaultHeight / 2F

        centerX = defaultWidth - radius
        centerY = middleHeight

        middleBarRect.left = 0F
        middleBarRect.top = middleHeight - middleBarHeight / 2F
        middleBarRect.right = middleBarRect.left + defaultWidth
        middleBarRect.bottom = middleBarRect.top + middleBarHeight

        val barMaxWidth =
            defaultWidth - segmentBarSpacing * 2 - radius * 2 - segmentBarSpacing * (bars.size - 1)

        var currentRight = 0F

        bars.forEach { bar ->
            val barWith = 1.0f * barMaxWidth * bar.value / maxProgress

            bar.rect.left = currentRight + segmentBarSpacing
            bar.rect.top = middleHeight - segmentBarHeight / 2F
            bar.rect.right = bar.rect.left + barWith
            bar.rect.bottom = bar.rect.top + segmentBarHeight
            println(bar.rect)

            currentRight = bar.rect.right

            bar.onMeasure()
        }

    }


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        paint.color = Color.parseColor("#84b0ff")
        canvas?.drawCircle(centerX, centerY, radius, paint)
        canvas?.drawRoundRect(middleBarRect, middleBarHeight, middleBarHeight, paint)

        bars.forEach { it.onDraw(canvas, paint) }
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

    private fun updateProgress() {
        if (currentProgress < 0) currentProgress = 0
        if (currentProgress > maxProgress) currentProgress = maxProgress

        for (bar in bars) {
            when {
                bar.contains(currentProgress) -> {
                    bar.current = currentProgress - bar.start
                }

                currentProgress >= bar.start + bar.value -> {
                    bar.current = bar.value
                }

                else -> {
                    bar.current = 0
                }
            }

            bar.updateCurrentPath()
        }

        postInvalidate()
    }

    class Bar(
        private val index: Int,
        private val size: Int,
        val start: Int,
        val value: Int,
        var current: Int = 0,
        val rect: RectF = RectF()
    ) {

        private val path: Path = Path()

        private val catPath = Path()

        private val currentPath = Path()

        private fun isFirst(): Boolean = index == 0

        private fun isLast(): Boolean = index == size - 1

        fun contains(progress: Int): Boolean = progress >= start && progress <= start + value

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
            paint.color = Color.parseColor("#e7e9eb")
            canvas?.drawPath(path, paint)

            paint.color = Color.parseColor("#ffcb3b")
            canvas?.drawPath(currentPath, paint)
        }

    }

}
