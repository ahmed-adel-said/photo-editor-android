package com.ahmedadeltito.photoeditorsdk

import android.content.Context
import android.graphics.*
import android.support.annotation.ColorInt
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

/**
 * Created by Ahmed Adel on 5/8/17.
 */

class BrushDrawingView : View {

    private var drawPath: Path? = null
    private var drawPaint: Paint? = null
    private var canvasPaint: Paint? = null

    private var drawCanvas: Canvas? = null
    private var canvasBitmap: Bitmap? = null
    private var brushDrawMode: Boolean = false

    private var onPhotoEditorSDKListener: OnPhotoEditorSDKListener? = null

    internal var brushColor: Int
        get() = drawPaint!!.color
        set(@ColorInt color) {
            drawPaint!!.color = color
            refreshBrushDrawing()
        }
    internal var brushSize = 10f
        set(size) {
            field = size
            refreshBrushDrawing()
        }
    internal var eraserSize = 100f

    @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : super(context, attrs) {
        setupBrushDrawing()
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        setupBrushDrawing()
    }

    private fun setupBrushDrawing() {
        drawPath = Path()
        drawPaint = Paint()
        drawPaint!!.isAntiAlias = true
        drawPaint!!.isDither = true
        drawPaint!!.color = Color.BLACK
        drawPaint!!.style = Paint.Style.STROKE
        drawPaint!!.strokeJoin = Paint.Join.ROUND
        drawPaint!!.strokeCap = Paint.Cap.ROUND
        drawPaint!!.strokeWidth = this.brushSize
        drawPaint!!.xfermode = PorterDuffXfermode(PorterDuff.Mode.DARKEN)
        canvasPaint = Paint(Paint.DITHER_FLAG)
        this.visibility = View.GONE
    }

    private fun refreshBrushDrawing() {
        brushDrawMode = true
        drawPaint!!.isAntiAlias = true
        drawPaint!!.isDither = true
        drawPaint!!.style = Paint.Style.STROKE
        drawPaint!!.strokeJoin = Paint.Join.ROUND
        drawPaint!!.strokeCap = Paint.Cap.ROUND
        drawPaint!!.strokeWidth = this.brushSize
        drawPaint!!.xfermode = PorterDuffXfermode(PorterDuff.Mode.DARKEN)
    }

    internal fun brushEraser() {
        drawPaint!!.strokeWidth = eraserSize
        drawPaint!!.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }

    internal fun setBrushDrawingMode(brushDrawMode: Boolean) {
        this.brushDrawMode = brushDrawMode
        if (brushDrawMode) {
            this.visibility = View.VISIBLE
            refreshBrushDrawing()
        }
    }

    internal fun setBrushEraserSize(brushEraserSize: Float) {
        this.eraserSize = brushEraserSize
    }

    internal fun setBrushEraserColor(@ColorInt color: Int) {
        drawPaint!!.color = color
        refreshBrushDrawing()
    }

    internal fun clearAll() {
        drawCanvas!!.drawColor(0, PorterDuff.Mode.CLEAR)
        invalidate()
    }

    fun setOnPhotoEditorSDKListener(onPhotoEditorSDKListener: OnPhotoEditorSDKListener) {
        this.onPhotoEditorSDKListener = onPhotoEditorSDKListener
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        drawCanvas = Canvas(canvasBitmap!!)
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawBitmap(canvasBitmap!!, 0f, 0f, canvasPaint)
        canvas.drawPath(drawPath!!, drawPaint!!)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (brushDrawMode) {
            val touchX = event.x
            val touchY = event.y
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    drawPath!!.moveTo(touchX, touchY)
                    if (onPhotoEditorSDKListener != null)
                        onPhotoEditorSDKListener!!.onStartViewChangeListener(ViewType.BRUSH_DRAWING)
                }
                MotionEvent.ACTION_MOVE -> drawPath!!.lineTo(touchX, touchY)
                MotionEvent.ACTION_UP -> {
                    drawCanvas!!.drawPath(drawPath!!, drawPaint!!)
                    drawPath!!.reset()
                    if (onPhotoEditorSDKListener != null)
                        onPhotoEditorSDKListener!!.onStopViewChangeListener(ViewType.BRUSH_DRAWING)
                }
                else -> return false
            }
            invalidate()
            return true
        } else {
            return false
        }
    }
}
