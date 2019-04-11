package com.devourer.alexb.diaryforthecoolestboys.Snowfall

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Handler
import android.os.HandlerThread
import android.util.AttributeSet
import android.util.Log
import android.view.View
import com.devourer.alexb.diaryforthecoolestboys.R
import java.util.*

class SnowfallView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private val DEFAULT_SNOWFLAKES_NUM = 200
    private val DEFAULT_SNOWFLAKE_ALPHA_MIN = 150
    private val DEFAULT_SNOWFLAKE_ALPHA_MAX = 250
    private val DEFAULT_SNOWFLAKE_ANGLE_MAX = 10
    private val DEFAULT_SNOWFLAKE_SIZE_MIN_IN_DP = 2
    private val DEFAULT_SNOWFLAKE_SIZE_MAX_IN_DP = 8
    private val DEFAULT_SNOWFLAKE_SPEED_MIN = 2
    private val DEFAULT_SNOWFLAKE_SPEED_MAX = 8
    private val DEFAULT_SNOWFLAKES_FADING_ENABLED = false
    private val DEFAULT_SNOWFLAKES_ALREADY_FALLING = false

    private var snowflakesNum: Int
    private var snowflakeImages = ArrayList<Bitmap>()
    private var snowflakeAlphaMin: Int
    private var snowflakeAlphaMax: Int
    private val snowflakeAngleMax: Int
    private var snowflakeSizeMinInPx: Int
    private var snowflakeSizeMaxInPx: Int
    private val snowflakeSpeedMin: Int
    private val snowflakeSpeedMax: Int
    private val snowflakesFadingEnabled: Boolean
    private val snowflakesAlreadyFalling: Boolean

    private lateinit var updateSnowflakesThread: UpdateSnowflakesThread
    private var snowflakes: Array<Snowflake>? = null

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.SnowfallView)
        try {
            snowflakesNum = a.getInt(R.styleable.SnowfallView_snowflakesNum, DEFAULT_SNOWFLAKES_NUM)
            snowflakeAlphaMin = a.getInt(R.styleable.SnowfallView_snowflakeAlphaMin, DEFAULT_SNOWFLAKE_ALPHA_MIN)
            snowflakeAlphaMax = a.getInt(R.styleable.SnowfallView_snowflakeAlphaMax, DEFAULT_SNOWFLAKE_ALPHA_MAX)
            snowflakeAngleMax = a.getInt(R.styleable.SnowfallView_snowflakeAngleMax, DEFAULT_SNOWFLAKE_ANGLE_MAX)
            snowflakeSizeMinInPx = a.getDimensionPixelSize(R.styleable.SnowfallView_snowflakeSizeMin, dpToPx(DEFAULT_SNOWFLAKE_SIZE_MIN_IN_DP))
            snowflakeSizeMaxInPx = a.getDimensionPixelSize(R.styleable.SnowfallView_snowflakeSizeMax, dpToPx(DEFAULT_SNOWFLAKE_SIZE_MAX_IN_DP))
            snowflakeSpeedMin = a.getInt(R.styleable.SnowfallView_snowflakeSpeedMin, DEFAULT_SNOWFLAKE_SPEED_MIN)
            snowflakeSpeedMax = a.getInt(R.styleable.SnowfallView_snowflakeSpeedMax, DEFAULT_SNOWFLAKE_SPEED_MAX)
            snowflakesFadingEnabled = a.getBoolean(R.styleable.SnowfallView_snowflakesFadingEnabled, DEFAULT_SNOWFLAKES_FADING_ENABLED)
            snowflakesAlreadyFalling = a.getBoolean(R.styleable.SnowfallView_snowflakesAlreadyFalling, DEFAULT_SNOWFLAKES_ALREADY_FALLING)
        } finally {
            a.recycle()
        }

    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        updateSnowflakesThread = UpdateSnowflakesThread()
    }

    override fun onDetachedFromWindow() {
        updateSnowflakesThread.quit()
        super.onDetachedFromWindow()
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        snowflakes = createSnowflakes()
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        if (changedView === this && visibility == GONE) {
            snowflakes?.forEach { it.reset() }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (isInEditMode) {
            return
        }
        val fallingSnowflakes = snowflakes?.filter { it.isStillFalling() }
        if (fallingSnowflakes?.isNotEmpty() == true) {
            fallingSnowflakes.forEach { it.draw(canvas) }
            updateSnowflakes()
        }
        else {
            visibility = GONE
        }
    }

    fun stopFalling() {
        Log.w("Main", "snowflakes.size -> ${snowflakes?.size}")
        snowflakes?.forEach { it.shouldRecycleFalling = false }
    }

    fun restartFalling() {
        snowflakes?.forEach { it.shouldRecycleFalling = true }
    }

    private fun createSnowflakes(): Array<Snowflake> {
        return Array(snowflakesNum) { Snowflake(getRandParams()) }
    }

    private fun updateSnowflakes() {
        val fallingSnowflakes = snowflakes?.filter { it.isStillFalling() }
        if (fallingSnowflakes?.isNotEmpty() == true) {
            updateSnowflakesThread.handler.post {
                fallingSnowflakes.forEach { it.update() }
                postInvalidateOnAnimation()
            }
        }
    }

    private class UpdateSnowflakesThread : HandlerThread("SnowflakesComputations") {
        val handler by lazy { Handler(looper) }

        init {
            start()
        }
    }

    fun setImage(sharedPreferences: SharedPreferences){
        Log.w("Main", sharedPreferences.getString("Choose flakes", "-1"))
        snowflakeImages.clear()
        when (sharedPreferences.getString("Choose flakes", "-1")){
            "flowers" -> {
                snowflakeImages.add(context.getDrawable(R.drawable.flakes_flower_0)!!.toBitmap())
                snowflakeImages.add(context.getDrawable(R.drawable.flakes_flower_1)!!.toBitmap())
                setMinAndMaxSize(8, 16)
                setMinAndMaxAlpha(150, 255)
                setCount(60)
            }
            "snowflakes" -> {
                snowflakeImages.add(context.getDrawable(R.drawable.flakes_snowflake_0)!!.toBitmap())
                setCount(120)
                setMinAndMaxSize(8, 16)
                setMinAndMaxAlpha(150, 255)
            }
            "penis" -> {
                snowflakeImages.add(context.getDrawable(R.drawable.flakes_penis_0)!!.toBitmap())
                snowflakeImages.add(context.getDrawable(R.drawable.flakes_penis_1)!!.toBitmap())
                setCount(40)
                setMinAndMaxAlpha(255, 255)
                setMinAndMaxSize(24, 28)
            }
            "geek" -> {
                snowflakeImages.add(context.getDrawable(R.drawable.flakes_geek_0)!!.toBitmap())
                snowflakeImages.add(context.getDrawable(R.drawable.flakes_geek_1)!!.toBitmap())
                snowflakeImages.add(context.getDrawable(R.drawable.flakes_geek_4)!!.toBitmap())
                setCount(40)
                setMinAndMaxAlpha(255, 255)
                setMinAndMaxSize(24, 28)
            }
            "batarangs" -> {
                snowflakeImages.add(context.getDrawable(R.drawable.flakes_batarang_0)!!.toBitmap())
                setCount(40)
                setMinAndMaxAlpha(255, 255)
                setMinAndMaxSize(30, 34)
            }
        }
        snowflakes = createSnowflakes()
    }

    private fun getRandBitmap() : Bitmap? {
        return if (snowflakeImages.size == 0)
            null
        else{
            val rand = Random().nextInt(snowflakeImages.size)
            snowflakeImages[rand]
        }
    }

    private fun getRandParams() : Snowflake.Params {
        return Snowflake.Params(
            parentWidth = width,
            parentHeight = height,
            image = getRandBitmap(),
            alphaMin = snowflakeAlphaMin,
            alphaMax = snowflakeAlphaMax,
            angleMax = snowflakeAngleMax,
            sizeMinInPx = snowflakeSizeMinInPx,
            sizeMaxInPx = snowflakeSizeMaxInPx,
            speedMin = snowflakeSpeedMin,
            speedMax = snowflakeSpeedMax,
            fadingEnabled = snowflakesFadingEnabled,
            alreadyFalling = snowflakesAlreadyFalling
        )
    }

    private fun setMinAndMaxSize(min: Int, max: Int){
        snowflakeSizeMinInPx = dpToPx(min)
        snowflakeSizeMaxInPx = dpToPx(max)
    }

    private fun setCount(count: Int){
        snowflakesNum = count
    }

    private fun setMinAndMaxAlpha(min: Int, max: Int){
        snowflakeAlphaMin = min
        snowflakeAlphaMax = max
    }
}