package com.tilseier.starsforprogressbar.ui.custom

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.view.animation.LinearInterpolator
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.IntegerRes
import androidx.annotation.Px
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.animation.doOnRepeat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.toRectF
import com.tilseier.myfirstkotlinapp.extantions.dpToPx
import com.tilseier.starsforprogressbar.R
import kotlin.math.max
import kotlin.math.truncate


class AvatarImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    companion object {
        private const val DEFAULT_BORDER_WIDTH = 2
        private const val DEFAULT_BORDER_COLOR = Color.WHITE
        private const val DEFAULT_SIZE = 40

        public val bgColors = arrayOf(
            Color.parseColor("#7BC862"),
            Color.parseColor("#E17076"),
            Color.parseColor("#B32344"),
            Color.parseColor("#E53534"),
            Color.parseColor("#E98657"),
            Color.parseColor("#7B2234"),
            Color.parseColor("#235235"),
            Color.parseColor("#645644"),
            Color.parseColor("#567A65")
        )
    }

    @Px
    var borderWidth: Float = context.dpToPx(DEFAULT_BORDER_WIDTH)

    @ColorInt
    private var borderColor: Int = Color.WHITE
    private var initials: String = "??"

    private var avatarPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var initialsPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var borderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var viewRect = Rect()
    private var borderRect = Rect()

    private var drawableImage: Drawable? = null

    private var isAvatarMode = true
    private var size = 0

    init {
        if (attrs != null) {
            val ta = context.obtainStyledAttributes(attrs, R.styleable.AvatarImageView)
            borderWidth = ta.getDimension(
                R.styleable.AvatarImageView_aiv_borderWidth,
                context.dpToPx(DEFAULT_BORDER_WIDTH)
            )

            drawableImage = ta.getDrawable(R.styleable.AvatarImageView_aiv_src)

            borderColor =
                ta.getColor(R.styleable.AvatarImageView_aiv_borderColor, DEFAULT_BORDER_COLOR)
            initials = ta.getString(R.styleable.AvatarImageView_aiv_initials) ?: "??"
            ta.recycle()//for more efficient use if resources
        }

        scaleType = ScaleType.CENTER_CROP
        setup()
        setOnLongClickListener { handleLongClick() }
    }

//    override fun onAttachedToWindow() {
//        super.onAttachedToWindow()
//        Log.e("CUSTOM_VIEW", "onAttachedToWindow")
//    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        Log.e(
            "CUSTOM_VIEW", "" +
                    "onMeasure" +
                    "\nwidth: ${MeasureSpec.toString(widthMeasureSpec)}" +
                    "\nheight: ${MeasureSpec.toString(heightMeasureSpec)}"
        )
        val initSize = resolveDefaultSize(widthMeasureSpec)
        setMeasuredDimension(max(initSize, size), max(initSize, size))
        Log.e("CUSTOM_VIEW", "onMeasure after set size: $measuredWidth $measuredHeight")
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        Log.e("CUSTOM_VIEW", "onSizeChanged")
        if (w == 0) return
        with(viewRect) {
            left = 0
            top = 0
            right = w
            bottom = h
        }

        prepareShader(w, h)
    }

    //we don't need onLayout method because our view doesn't contain child elements
//    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
//        super.onLayout(changed, left, top, right, bottom)
//        Log.e("CUSTOM_VIEW", "onLayout")
//    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        Log.e("CUSTOM_VIEW", "onDraw")
        //NOT allocate, ONLY draw
        if (drawableImage != null && isAvatarMode) {
            Log.e("CUSTOM_VIEW", "drawAvatar")
            drawAvatar(canvas)
        } else {
            Log.e("CUSTOM_VIEW", "drawInitials")
            drawInitials(canvas)
        }

        //resize rect
        val half = (borderWidth / 2).toInt()
        borderRect.set(viewRect)
        borderRect.inset(half, half)
        canvas.drawOval(borderRect.toRectF(), borderPaint)
    }

    override fun onSaveInstanceState(): Parcelable? {
        Log.e("CUSTOM_VIEW", "onSaveInstanceState $id")
        val savedState = SavedState(super.onSaveInstanceState())
        savedState.isAvatarMode = isAvatarMode
        savedState.borderWidth = borderWidth
        savedState.borderColor = borderColor
        return savedState
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        Log.e("CUSTOM_VIEW", "onRestoreInstanceState $id")
        if (state is SavedState) {
            super.onRestoreInstanceState(state)
            isAvatarMode = state.isAvatarMode
            borderWidth = state.borderWidth
            borderColor = state.borderColor

            with(borderPaint){
                color = borderColor
                strokeWidth = borderWidth
            }
        } else {
            super.onRestoreInstanceState(state)
        }
    }

    override fun setImageBitmap(bm: Bitmap?) {
        super.setImageBitmap(bm)
        if (isAvatarMode) prepareShader(width, height)
        Log.e("CUSTOM_VIEW", "setImageBitmap")
    }

    override fun setImageDrawable(drawable: Drawable?) {
        super.setImageDrawable(drawable)
        if (isAvatarMode) prepareShader(width, height)
        Log.e("CUSTOM_VIEW", "setImageDrawable")
    }

    override fun setImageResource(resId: Int) {
        super.setImageResource(resId)
        if (isAvatarMode) prepareShader(width, height)
        Log.e("CUSTOM_VIEW", "setImageResource")

    }

    fun setInitials(initials: String) {
        Log.e("CUSTOM_VIEW", "setInitials: $initials")
        this.initials = initials
        if (!isAvatarMode) {
            invalidate()
        }
    }

    fun setBorderColor(@ColorInt color: Int) {
        Log.e("CUSTOM_VIEW", "setInitials: $initials")
        this.borderColor = color
        borderPaint.color = borderColor
        invalidate()
    }

    fun setBorderWidth(@Dimension width: Int) {
        Log.e("CUSTOM_VIEW", "setInitials: $initials")
        this.borderWidth = context.dpToPx(width)
        borderPaint.strokeWidth = borderWidth
        invalidate()
    }

    fun setAvatarImage(imageRes: Int) {
        Log.e("CUSTOM_VIEW", "setInitials: $initials")
        this.drawableImage = context.getDrawable(imageRes)
        if (isAvatarMode) {
            invalidate()
        }
    }

    private fun setup() {
        with(borderPaint) {
            style = Paint.Style.STROKE
            strokeWidth = borderWidth
            color = borderColor
        }
    }

    private fun prepareShader(w: Int, h: Int) {
        //prepare buffer this
        if (w == 0 || drawableImage == null) return
        val srcBm = drawableImage?.toBitmap(w, h, Bitmap.Config.ARGB_8888)
        avatarPaint.shader =
            srcBm?.let { BitmapShader(it, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP) }

    }

    private fun resolveDefaultSize(spec: Int): Int {
        return when (MeasureSpec.getMode(spec)) {
            MeasureSpec.UNSPECIFIED -> {
                context.dpToPx(DEFAULT_SIZE).toInt()
            } //resolveDefaultSize()
            MeasureSpec.AT_MOST -> MeasureSpec.getSize(spec) //from spec
            MeasureSpec.EXACTLY -> MeasureSpec.getSize(spec) //from spec
            else -> MeasureSpec.getSize(spec) //from spec
        }
    }

    private fun drawAvatar(canvas: Canvas) {
        canvas.drawOval(viewRect.toRectF(), avatarPaint)
    }

    private fun drawInitials(canvas: Canvas) {
        initialsPaint.color = initialsToColor(initials)
        canvas.drawOval(viewRect.toRectF(), initialsPaint)
        with(initialsPaint) {
            color = Color.WHITE
            textAlign = Paint.Align.CENTER
            textSize = height * 0.33f
        }
        val offsetY = (initialsPaint.descent() + initialsPaint.ascent()) / 2
        canvas.drawText(initials, viewRect.exactCenterX(), viewRect.exactCenterY() - offsetY, initialsPaint)
    }

    private fun initialsToColor(letters: String): Int {
        val b = letters[0].toByte()
        val len = bgColors.size
        val d = b / len.toDouble()
        val index = ((d - truncate(d)) * len).toInt()
        return bgColors[index]
    }

    private fun handleLongClick(): Boolean {
        val va = ValueAnimator.ofInt(width, width*2).apply {
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

    private class SavedState : BaseSavedState, Parcelable{
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