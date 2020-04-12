package com.tilseier.starsforprogressbar.ui.custom

import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.animation.*
import android.widget.ProgressBar
import androidx.annotation.Px
import androidx.core.graphics.drawable.toBitmap
import com.tilseier.myfirstkotlinapp.extantions.dpToPx
import com.tilseier.starsforprogressbar.R
import java.lang.String


class ProgressStarsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    companion object {
        private const val DEFAULT_COUNT_OF_STARS = 3
        private const val DEFAULT_STARS_SIZE = 20
        private const val DEFAULT_STAR_1_PROGRESS = 50
        private const val DEFAULT_STAR_2_PROGRESS = 80
        private const val DEFAULT_STAR_3_PROGRESS = 100
    }

    @Px
    private var starsSize: Float = context.dpToPx(DEFAULT_STARS_SIZE)

    private var imageSize: Pair<Float, Float> = Pair(starsSize, starsSize)

    private var viewRectStar1 = Rect()
    private var viewRectStar2 = Rect()
    private var viewRectStar3 = Rect()
    private var offsetProgressBarBounds = Rect()

    private var imageBm: Bitmap? = null
    private var progressBar: ProgressBar? = null
    private var parentViewGroup: ViewGroup? = null
    private var drawableStar: Drawable? = null

    private var idProgressBar = 0
    private var idParent = 0

    private var countOfStars = DEFAULT_COUNT_OF_STARS

    private var currentProgress = 0

    private var star1Achieved = false
    private var star2Achieved = false
    private var star3Achieved = false

    private var star1AchievedAndAnimated = false
    private var star2AchievedAndAnimated = false
    private var star3AchievedAndAnimated = false

    private var star1AchieveProgress = DEFAULT_STAR_1_PROGRESS
    private var star2AchieveProgress = DEFAULT_STAR_2_PROGRESS
    private var star3AchieveProgress = DEFAULT_STAR_3_PROGRESS

    private var finalLeftStar1 = 0
    private var finalTopStar1 = 0
    private var finalLeftStar2 = 0
    private var finalTopStar2 = 0
    private var finalLeftStar3 = 0
    private var finalTopStar3 = 0

    private var startLeftStar1 = 0
    private var startTopStar1 = 0
    private var startLeftStar2 = 0
    private var startTopStar2 = 0
    private var startLeftStar3 = 0
    private var startTopStar3 = 0

    private var endLeftStar1 = 100
    private var endTopStar1 = 100
    private var endLeftStar2 = 200
    private var endTopStar2 = 100
    private var endLeftStar3 = 300
    private var endTopStar3 = 100

    init {
        if (attrs != null) {
            val ta = context.obtainStyledAttributes(attrs, R.styleable.ProgressStarsView)
            starsSize = ta.getDimension(
                R.styleable.ProgressStarsView_psv_stars_size,
                context.dpToPx(DEFAULT_STARS_SIZE)
            )
            imageSize = Pair(starsSize, starsSize)

            star1AchieveProgress =
                ta.getInt(R.styleable.ProgressStarsView_psv_star1_progress, DEFAULT_STAR_1_PROGRESS)
            star2AchieveProgress =
                ta.getInt(R.styleable.ProgressStarsView_psv_star2_progress, DEFAULT_STAR_2_PROGRESS)
            star3AchieveProgress =
                ta.getInt(R.styleable.ProgressStarsView_psv_star3_progress, DEFAULT_STAR_2_PROGRESS)

            drawableStar = ta.getDrawable(R.styleable.ProgressStarsView_psv_stars_src)

            countOfStars = ta.getInt(R.styleable.ProgressStarsView_psv_count_of_stars, DEFAULT_COUNT_OF_STARS)
            if (countOfStars < 0) {
                countOfStars = 0
            }
            if (countOfStars > DEFAULT_COUNT_OF_STARS) {
                countOfStars = DEFAULT_COUNT_OF_STARS
            }

            idProgressBar = ta.getResourceId(R.styleable.ProgressStarsView_psv_progress_bar, 0)
            idParent = ta.getResourceId(R.styleable.ProgressStarsView_psv_parent, 0)
            ta.recycle()//for more efficient use if resources
        }

//        isClickable = false
//        isFocusable = false
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        Log.e("CUSTOM_VIEW", "onAttachedToWindow")
        setupReferenceView()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        Log.e(
            "CUSTOM_VIEW", "" +
                    "onMeasure" +
                    "\nwidth: ${MeasureSpec.toString(widthMeasureSpec)}" +
                    "\nheight: ${MeasureSpec.toString(heightMeasureSpec)}"
        )
        val initSize = resolveDefaultSize(widthMeasureSpec)
        setMeasuredDimension(initSize, initSize)// max(initSize, size)
        Log.e("CUSTOM_VIEW", "onMeasure after set size: $measuredWidth $measuredHeight")
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        Log.e("CUSTOM_VIEW", "onSizeChanged w = $w, h = $h")
        if (w == 0 || progressBar == null || parentViewGroup == null) return

        prepareAllStars()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        //NOT allocate, ONLY draw
        if (countOfStars >= 1) {
            drawStar1(canvas)
        }
        if (countOfStars >= 2) {
            drawStar2(canvas)
        }
        if (countOfStars >= 3) {
            drawStar3(canvas)
        }
    }

    private fun prepareAllStars() {

        prepareImageBitmap(starsSize.toInt())

        if (offsetProgressBarBounds.width() <= 0) {
            //returns the visible bounds
            progressBar?.getDrawingRect(offsetProgressBarBounds)
            // calculates the relative coordinates to the parent
            parentViewGroup?.offsetDescendantRectToMyCoords(progressBar, offsetProgressBarBounds)

            updateStartEndStarsPosition()

            setProgress(progressBar?.progress ?: 0)
        }

        Log.d("ovb.top: ", String.valueOf(offsetProgressBarBounds.top))
        Log.d("ovb.left: ", String.valueOf(offsetProgressBarBounds.left))
        Log.d("ovb.right: ", String.valueOf(offsetProgressBarBounds.right))
        Log.d("ovb.bottom: ", String.valueOf(offsetProgressBarBounds.bottom))
        Log.d("ovb.width():", String.valueOf(offsetProgressBarBounds.width()))
        Log.d("ovb.height(): ", String.valueOf(offsetProgressBarBounds.height()))
        Log.d("ovb.height(): ", String.valueOf(offsetProgressBarBounds.height()))
        Log.d("progressBar?.max: ", String.valueOf(progressBar?.max))
        Log.d("progressBar?.progress: ", String.valueOf(progressBar?.progress))

        updateFinalStarsPosition()
    }

    private fun updateStartEndStarsPosition() {
        val topPosition =
            offsetProgressBarBounds.top + (offsetProgressBarBounds.height() / 2) - (imageSize.second.toInt() / 2)
        val endTopPosition = offsetProgressBarBounds.top - imageSize.second.toInt() // - margin bottom

        //start stars position
        startLeftStar1 = getProgressPosition(star1AchieveProgress)
        startTopStar1 = topPosition

        startLeftStar2 = getProgressPosition(star2AchieveProgress)
        startTopStar2 = topPosition

        startLeftStar3 = getProgressPosition(star3AchieveProgress)
        startTopStar3 = topPosition

        //final stars position
        finalLeftStar1 = startLeftStar1
        finalTopStar1 = startTopStar1

        finalLeftStar2 = startLeftStar2
        finalTopStar2 = startTopStar2

        finalLeftStar3 = startLeftStar3
        finalTopStar3 = startTopStar3

        //end stars position
        endLeftStar1 = offsetProgressBarBounds.left
        endTopStar1 = endTopPosition

        endLeftStar2 = endLeftStar1 + imageSize.first.toInt() + 50 //+ margins between
        endTopStar2 = endTopPosition

        endLeftStar3 = endLeftStar2 + imageSize.first.toInt() + 50 //+ margins between
        endTopStar3 = endTopPosition
    }

    private fun updateFinalStarsPosition() {
        with(viewRectStar1) {
            left = finalLeftStar1
            top = finalTopStar1
        }

        with(viewRectStar2) {
            left = finalLeftStar2
            top = finalTopStar2
        }

        with(viewRectStar3) {
            left = finalLeftStar3
            top = finalTopStar3
        }
    }

    fun setStarsImage(imageRes: Int) {

    }

    fun setStarsSize(size: Int) {

    }

    fun setProgress(progress: Int) {
        this.currentProgress = progress

        if (countOfStars >= 1) {
            star1Achieved = this.currentProgress >= star1AchieveProgress
        }
        if (countOfStars >= 2) {
            star2Achieved = this.currentProgress >= star2AchieveProgress
        }
        if (countOfStars >= 3) {
            star3Achieved = this.currentProgress >= star3AchieveProgress
        }

        if (star1Achieved != star1AchievedAndAnimated
            || star2Achieved != star2AchievedAndAnimated
            || star3Achieved != star3AchievedAndAnimated
        ) {
            updateViewWithAnimation()
        }
    }

    fun getAchievementsCount(): Int{
        var count = 0
        if (star1Achieved) {
            count ++
        }
        if (star2Achieved) {
            count++
        }
        if (star3Achieved) {
            count++
        }
        return count
    }

    private fun updateViewWithAnimation() {

        //decide should we animate stars or not
        val animateStar1 = star1Achieved != star1AchievedAndAnimated
        val animateStar2 = star2Achieved != star2AchievedAndAnimated
        val animateStar3 = star3Achieved != star3AchievedAndAnimated

        //animate from start position on progress bar to end position
        var startLeft1 = startLeftStar1
        var finishLeft1 = endLeftStar1
        var startTop1 = startTopStar1
        var finishTop1 = endTopStar1
        var startLeft2 = startLeftStar2
        var finishLeft2 = endLeftStar2
        var startTop2 = startTopStar2
        var finishTop2 = endTopStar2
        var startLeft3 = startLeftStar3
        var finishLeft3 = endLeftStar3
        var startTop3 = startTopStar3
        var finishTop3 = endTopStar3

        //animate from end position to start position on progress bar
        if (!star1Achieved && star1AchievedAndAnimated) {
            startLeft1 = endLeftStar1
            finishLeft1 = startLeftStar1
        }

        if (!star1Achieved && star1AchievedAndAnimated) {
            startTop1 = endTopStar1
            finishTop1 = startTopStar1
        }

        if (!star2Achieved && star2AchievedAndAnimated) {
            startLeft2 = endLeftStar2
            finishLeft2 = startLeftStar2
        }

        if (!star2Achieved && star2AchievedAndAnimated) {
            startTop2 = endTopStar2
            finishTop2 = startTopStar2
        }

        if (!star3Achieved && star3AchievedAndAnimated) {
            startLeft3 = endLeftStar3
            finishLeft3 = startLeftStar3
        }

        if (!star3Achieved && star3AchievedAndAnimated) {
            startTop3 = endTopStar3
            finishTop3 = startTopStar3
        }

        val left1 =
            PropertyValuesHolder.ofInt(
                if (animateStar1) "leftStar1" else "_leftStar1",
                startLeft1,
                finishLeft1
            )
        val top1 =
            PropertyValuesHolder.ofInt(
                if (animateStar1) "topStar1" else "_topStar1",
                startTop1,
                finishTop1
            )
        val left2 =
            PropertyValuesHolder.ofInt(
                if (animateStar2) "leftStar2" else "_leftStar2",
                startLeft2,
                finishLeft2
            )
        val top2 =
            PropertyValuesHolder.ofInt(
                if (animateStar2) "topStar2" else "_topStar2",
                startTop2,
                finishTop2
            )
        val left3 =
            PropertyValuesHolder.ofInt(
                if (animateStar3) "leftStar3" else "_leftStar3",
                startLeft3,
                finishLeft3
            )
        val top3 =
            PropertyValuesHolder.ofInt(
                if (animateStar3) "topStar3" else "_topStar3",
                startTop3,
                finishTop3
            )

        star1AchievedAndAnimated = star1Achieved
        star2AchievedAndAnimated = star2Achieved
        star3AchievedAndAnimated = star3Achieved

        val va = ValueAnimator.ofPropertyValuesHolder(left1, top1, left2, top2, left3, top3).apply {
            duration = 600
            interpolator = AnticipateInterpolator()
        }
        va.addUpdateListener {

            if (it.getAnimatedValue("leftStar1") != null) {
                finalLeftStar1 = it.getAnimatedValue("leftStar1") as Int
            }
            if (it.getAnimatedValue("topStar1") != null) {
                finalTopStar1 = it.getAnimatedValue("topStar1") as Int
            }
            if (it.getAnimatedValue("leftStar2") != null) {
                finalLeftStar2 = it.getAnimatedValue("leftStar2") as Int
            }
            if (it.getAnimatedValue("topStar2") != null) {
                finalTopStar2 = it.getAnimatedValue("topStar2") as Int
            }
            if (it.getAnimatedValue("leftStar3") != null) {
                finalLeftStar3 = it.getAnimatedValue("leftStar3") as Int
            }
            if (it.getAnimatedValue("topStar3") != null) {
                finalTopStar3 = it.getAnimatedValue("topStar3") as Int
            }

            updateFinalStarsPosition()
            invalidate()
        }
        va.start()
    }

    private fun getProgressPosition(progress: Int): Int {
        var curProgress = progress
        val maxProgress = progressBar?.max ?: 0
        var progressPercent = 0F
        if (maxProgress != 0) {
            if (curProgress > maxProgress) {
                curProgress = maxProgress
            } else if (curProgress < 0) {
                curProgress = 0
            }
            progressPercent = curProgress / maxProgress.toFloat()
        }
        val progressPosition =
            (progressPercent * (offsetProgressBarBounds.right - offsetProgressBarBounds.left)) + offsetProgressBarBounds.left - (imageSize.first.toInt() / 2)
        return progressPosition.toInt()//offsetProgressBarBounds.right * progressPercent
    }

    private fun setupReferenceView() {
        if (idProgressBar != 0) {
            val viewProgressBar: View = getRootView().findViewById(idProgressBar)
            if (viewProgressBar is ProgressBar) {
                progressBar = viewProgressBar
            } else {
                throw RuntimeException("'psv_progress_bar' should contain reference to ProgressBar view")
            }
        } else {
            throw RuntimeException("'psv_progress_bar' is not defined")
        }
        if (idParent != 0) {
            val viewGroup: View = getRootView().findViewById(idParent)
            if (viewGroup is ViewGroup) {
                parentViewGroup = viewGroup
            } else {
                throw RuntimeException("'psv_parent' should contain reference to parent ViewGroup")
            }
        } else {
            throw RuntimeException("'psv_parent' is not defined")
        }
    }

    private fun prepareImageBitmap(wh: Int) {
        //prepare buffer this
        if (wh == 0 || drawableStar == null) return
        val (bmWidth, bmHeight) = getProperWidthHeight(drawableStar, wh)
        imageSize = Pair(bmWidth.toFloat(), bmHeight.toFloat())
        imageBm = drawableStar?.toBitmap(bmWidth, bmHeight, Bitmap.Config.ARGB_8888)
    }

    private fun getProperWidthHeight(drawable: Drawable?, wh: Int): Pair<Int, Int>{
        var bmWidth = drawable?.intrinsicWidth?.toFloat() ?: wh.toFloat()
        var bmHeight = drawable?.intrinsicHeight?.toFloat() ?: wh.toFloat()
        Log.e("CUSTOM_VIEW", "BEFORE bmWidth: $bmWidth; bmHeight: $bmHeight")
        if (bmWidth >= bmHeight) {
            bmHeight *= (wh / bmWidth)
            bmWidth = wh.toFloat()
        } else {
            bmWidth *= (wh / bmHeight)
            bmHeight = wh.toFloat()
        }
        Log.e("CUSTOM_VIEW", "AFTER bmWidth: ${bmWidth.toInt()}; bmHeight: ${bmHeight.toInt()}")
        return Pair(bmWidth.toInt(), bmHeight.toInt())
    }

    private fun resolveDefaultSize(spec: Int): Int {
        return when (MeasureSpec.getMode(spec)) {
            MeasureSpec.UNSPECIFIED -> {
                MeasureSpec.getSize(spec)
//                context.dpToPx(DEFAULT_SIZE).toInt()
            } //resolveDefaultSize()
            MeasureSpec.AT_MOST -> MeasureSpec.getSize(spec) //from spec
            MeasureSpec.EXACTLY -> MeasureSpec.getSize(spec) //from spec
            else -> MeasureSpec.getSize(spec) //from spec
        }
    }

    private fun drawStar1(canvas: Canvas) {
        imageBm?.let {
            canvas.drawBitmap(
                it,
                viewRectStar1.left.toFloat(),
                viewRectStar1.top.toFloat(),
                null
            )
        }
    }

    private fun drawStar2(canvas: Canvas) {
        imageBm?.let {
            canvas.drawBitmap(
                it,
                viewRectStar2.left.toFloat(),
                viewRectStar2.top.toFloat(),
                null
            )
        }
    }

    private fun drawStar3(canvas: Canvas) {
        imageBm?.let {
            canvas.drawBitmap(
                it,
                viewRectStar3.left.toFloat(),
                viewRectStar3.top.toFloat(),
                null
            )
        }
    }

}