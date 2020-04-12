package com.tilseier.starsforprogressbar.ui.custom

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.ProgressBar
import androidx.annotation.Px
import androidx.core.animation.doOnRepeat
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
        private const val DEFAULT_STARS_SIZE = 20
        private const val DEFAULT_STAR_1_PROGRESS = 50
        private const val DEFAULT_STAR_2_PROGRESS = 80
        private const val DEFAULT_STAR_3_PROGRESS = 100
    }

    @Px
    private var starsSize: Float = context.dpToPx(DEFAULT_STARS_SIZE)

    private var viewRectStar1 = Rect()
    private var viewRectStar2 = Rect()
    private var viewRectStar3 = Rect()

    private var offsetProgressBarBounds = Rect()

    private var star1Bm: Bitmap? = null
    private var star2Bm: Bitmap? = null
    private var star3Bm: Bitmap? = null
    private var progressBar: ProgressBar? = null
    private var parentViewGroup: ViewGroup? = null
    private var drawableStar: Drawable? = null

    private var isAvatarMode = true
    private var size = 0

    private var idProgressBar = 0
    private var idParent = 0

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

    init {
        if (attrs != null) {
            val ta = context.obtainStyledAttributes(attrs, R.styleable.ProgressStarsView)
            starsSize = ta.getDimension(
                R.styleable.ProgressStarsView_psv_stars_size,
                context.dpToPx(DEFAULT_STARS_SIZE)
            )

            star1AchieveProgress = ta.getInt(R.styleable.ProgressStarsView_psv_star1_progress, DEFAULT_STAR_1_PROGRESS)
            star2AchieveProgress = ta.getInt(R.styleable.ProgressStarsView_psv_star2_progress, DEFAULT_STAR_2_PROGRESS)
            star3AchieveProgress = ta.getInt(R.styleable.ProgressStarsView_psv_star3_progress, DEFAULT_STAR_2_PROGRESS)

            drawableStar = ta.getDrawable(R.styleable.ProgressStarsView_psv_stars_src)

            idProgressBar = ta.getResourceId(R.styleable.ProgressStarsView_psv_progress_bar, 0)
            idParent = ta.getResourceId(R.styleable.ProgressStarsView_psv_parent, 0)
            ta.recycle()//for more efficient use if resources
        }

//        scaleType = ScaleType.CENTER_CROP

        //TODO
//        isClickable = false
//        isFocusable = false

//        setOnLongClickListener { handleLongClick() }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        Log.e("CUSTOM_VIEW", "onAttachedToWindow")
        setupReferenceView()
        setup()
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

    private fun prepareAllStars() {

        val starsWidthHeight = starsSize.toInt()

        if (offsetProgressBarBounds.width() <= 0) {
            //returns the visible bounds
            progressBar?.getDrawingRect(offsetProgressBarBounds)
            // calculates the relative coordinates to the parent
            parentViewGroup?.offsetDescendantRectToMyCoords(progressBar, offsetProgressBarBounds)
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

        val topOffset =
            offsetProgressBarBounds.top + (offsetProgressBarBounds.height() / 2) - (starsWidthHeight / 2)

        var leftStar1 = 0//getProgressPosition(star1AchieveProgress)
        var topStar1 = 0//topOffset
        var leftStar2 = 0//getProgressPosition(star2AchieveProgress)
        var topStar2 = 0//topOffset
        var leftStar3 = 0//getProgressPosition(star3AchieveProgress)
        var topStar3 = 0//topOffset

        if (!star1Achieved && !star1AchievedAndAnimated) {
            leftStar1 = getProgressPosition(star1AchieveProgress)
            topStar1 = topOffset
        } else {
            leftStar1 = 100
            topStar1 = 100
        }

        if (!star2Achieved && !star2AchievedAndAnimated) {
            leftStar2 = getProgressPosition(star2AchieveProgress)
            topStar2 = topOffset
        } else {
            leftStar2 = 200
            topStar2 = 100
        }

        if (!star3Achieved && !star3AchievedAndAnimated) {
            leftStar3 = getProgressPosition(star3AchieveProgress)
            topStar3 = topOffset
        } else {
            leftStar3 = 300
            topStar3 = 100
        }

        with(viewRectStar1) {
            left = leftStar1//offsetProgressBarBounds.right - (starsWidthHeight/2)
            top = topStar1
        }

        with(viewRectStar2) {
            left = leftStar2
            top = topStar2
        }

        with(viewRectStar3) {
            left = leftStar3
            top = topStar3
        }

        prepareStar1Bitmap(starsWidthHeight, starsWidthHeight)
        prepareStar2Bitmap(starsWidthHeight, starsWidthHeight)
        prepareStar3Bitmap(starsWidthHeight, starsWidthHeight)

    }

    //we don't need onLayout method because our view doesn't contain child elements
//    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
//        super.onLayout(changed, left, top, right, bottom)
//        Log.e("CUSTOM_VIEW", "onLayout")
//    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        Log.e("CUSTOM_VIEW", "onDraw progressBar?.progress: ${progressBar?.progress}")
        //NOT allocate, ONLY draw
        drawStar1(canvas)
        drawStar2(canvas)
        drawStar3(canvas)

//        if (drawableStar1 != null && isAvatarMode) {
//            Log.e("CUSTOM_VIEW", "drawAvatar")
//            drawAvatar(canvas)
//        } else {
//            Log.e("CUSTOM_VIEW", "drawInitials")
//            drawInitials(canvas)
//        }

        //resize rect
//        val half = (borderWidth / 2).toInt()
//        borderRect.set(viewRect)
//        borderRect.inset(half, half)
//        canvas.drawOval(borderRect.toRectF(), borderPaint)
    }

//    override fun onSaveInstanceState(): Parcelable? {
//        Log.e("CUSTOM_VIEW", "onSaveInstanceState $id")
//        val savedState = SavedState(super.onSaveInstanceState())
//        savedState.isAvatarMode = isAvatarMode
//        savedState.borderWidth = borderWidth
//        savedState.borderColor = borderColor
//        return savedState
//    }
//
//    override fun onRestoreInstanceState(state: Parcelable?) {
//        Log.e("CUSTOM_VIEW", "onRestoreInstanceState $id")
//        if (state is SavedState) {
//            super.onRestoreInstanceState(state)
//            isAvatarMode = state.isAvatarMode
//            borderWidth = state.borderWidth
//            borderColor = state.borderColor
//
//            with(borderPaint){
//                color = borderColor
//                strokeWidth = borderWidth
//            }
//        } else {
//            super.onRestoreInstanceState(state)
//        }
//    }

    private fun setup() {

//        with(star1Paint) {
//            style = Paint.Style.STROKE
//            strokeWidth = borderWidth
//            color = borderColor
//        }
    }

    fun setProgress(progress: Int){
        this.currentProgress = progress
        star1Achieved = this.currentProgress >= star1AchieveProgress
        star2Achieved = this.currentProgress >= star2AchieveProgress
        star3Achieved = this.currentProgress >= star3AchieveProgress
        //TODO ANIMATION ValueAnimator for left top stars achieved
        //TODO connect GIT to project
        prepareAllStars()
        invalidate()
//        requestLayout()//TODO REMOVE
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
            (progressPercent * (offsetProgressBarBounds.right - offsetProgressBarBounds.left)) + offsetProgressBarBounds.left - (starsSize.toInt() / 2)
        return progressPosition.toInt()//offsetProgressBarBounds.right * progressPercent
    }

    private fun setupReferenceView() {
        if (idProgressBar != 0) {
            progressBar = getRootView().findViewById(idProgressBar)
        }
        if (idParent != 0) {
            parentViewGroup = getRootView().findViewById(idParent)
        }
//        currentProgress = progressBar?.progress ?: 0
        setProgress(progressBar?.progress ?: 0)

        Log.e("CUSTOM_VIEW", "setupReferenceView progressBar?.progress: ${progressBar?.progress}")
    }

    private fun prepareStar1Bitmap(w: Int, h: Int) {
        //prepare buffer this
        if (w == 0 || drawableStar == null) return
        star1Bm = drawableStar?.toBitmap(w, h, Bitmap.Config.ARGB_8888)
    }

    private fun prepareStar2Bitmap(w: Int, h: Int) {
        //prepare buffer this
        if (w == 0 || drawableStar == null) return
        star2Bm = drawableStar?.toBitmap(w, h, Bitmap.Config.ARGB_8888)
    }

    private fun prepareStar3Bitmap(w: Int, h: Int) {
        //prepare buffer this
        if (w == 0 || drawableStar == null) return
        star3Bm = drawableStar?.toBitmap(w, h, Bitmap.Config.ARGB_8888)
    }

//    private fun prepareStar2Shader(w: Int, h: Int) {
//        //prepare buffer this
//        if (w == 0 || drawableStar == null) return
//        val srcBm = drawableStar?.toBitmap(w, h, Bitmap.Config.ARGB_8888)
//        star2Paint.shader =
//            srcBm?.let { BitmapShader(it, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP) }
//    }
//
//    private fun prepareStar3Shader(w: Int, h: Int) {
//        //prepare buffer this
//        if (w == 0 || drawableStar == null) return
//        val srcBm = drawableStar?.toBitmap(w, h, Bitmap.Config.ARGB_8888)
//        star3Paint.shader =
//            srcBm?.let { BitmapShader(it, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP) }
//    }


//    private fun prepareShader(w: Int, h: Int) {
//        //prepare buffer this
//        if (w == 0 || drawableStar1 == null) return
//        val srcBm = drawableStar1?.toBitmap(w, h, Bitmap.Config.ARGB_8888)
//        avatarPaint.shader =
//            srcBm?.let { BitmapShader(it, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP) }
//
//    }

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

//    private fun drawAvatar(canvas: Canvas) {
//        canvas.drawOval(viewRect.toRectF(), avatarPaint)
//    }

    private fun drawStar1(canvas: Canvas) {
        star1Bm?.let {
            canvas.drawBitmap(
                it,
                viewRectStar1.left.toFloat(),
                viewRectStar1.top.toFloat(),
                null
            )
        }
    }

    private fun drawStar2(canvas: Canvas) {
        star2Bm?.let {
            canvas.drawBitmap(
                it,
                viewRectStar2.left.toFloat(),
                viewRectStar2.top.toFloat(),
                null
            )
        }
    }

    private fun drawStar3(canvas: Canvas) {
        star3Bm?.let {
            canvas.drawBitmap(
                it,
                viewRectStar3.left.toFloat(),
                viewRectStar3.top.toFloat(),
                null
            )
        }
    }

    private fun handleLongClick(): Boolean {
        val va = ValueAnimator.ofInt(width, width * 2).apply {
            duration = 300
            interpolator = LinearInterpolator()
            repeatMode = ValueAnimator.REVERSE
            repeatCount = 1//default 0
        }
        va.addUpdateListener {
            size = it.animatedValue as Int
            requestLayout()
        }
        va.doOnRepeat { toggleMode() }
        va.start()
        return true
    }

    private fun toggleMode() {
        isAvatarMode = !isAvatarMode
        invalidate()
    }

    private class SavedState : BaseSavedState, Parcelable {
        var isAvatarMode: Boolean = true
        var borderWidth: Float = 0f
        var borderColor: Int = 0

        constructor(superState: Parcelable?) : super(superState)

        constructor(src: Parcel) : super(src) {
            //restore state from parcel
            isAvatarMode = src.readInt() == 1
            borderWidth = src.readFloat()
            borderColor = src.readInt()
        }

        override fun writeToParcel(dst: Parcel, flags: Int) {
            //write state to parcel
            super.writeToParcel(dst, flags)
            dst.writeInt(if (isAvatarMode) 1 else 0)
            dst.writeFloat(borderWidth)
            dst.writeInt(borderColor)
        }

        override fun describeContents() = 0

        companion object CREATOR : Parcelable.Creator<SavedState> {
            override fun createFromParcel(parcel: Parcel) = SavedState(parcel)

            override fun newArray(size: Int): Array<SavedState?> = arrayOfNulls(size)
        }

    }

}