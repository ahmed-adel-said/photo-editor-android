package com.ahmedadeltito.photoeditorsdk

import android.annotation.SuppressLint
import android.graphics.Rect
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.widget.ImageView
import android.widget.TextView

internal class MultiTouchListener(private val deleteView: View, private val photoEditImageView: ImageView,
                                  private val onPhotoEditorSDKListener: OnPhotoEditorSDKListener?) : OnTouchListener {

    private val minimumScale = 0.5f
    private val maximumScale = 10.0f
    private var mActivePointerId = INVALID_POINTER_ID
    private var mPrevX: Float = 0.toFloat()
    private var mPrevY: Float = 0.toFloat()
    private var mPrevRawX: Float = 0.toFloat()
    private var mPrevRawY: Float = 0.toFloat()
    private val mScaleGestureDetector: ScaleGestureDetector

    private val location = IntArray(2)
    private val outRect: Rect

    private var onMultiTouchListener: OnMultiTouchListener? = null

    init {
        mScaleGestureDetector = ScaleGestureDetector(ScaleGestureListener())
        outRect = Rect(deleteView.left, deleteView.top,
                deleteView.right, deleteView.bottom)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(view: View, event: MotionEvent): Boolean {
        mScaleGestureDetector.onTouchEvent(view, event)

        val action = event.action

        val x = event.rawX.toInt()
        val y = event.rawY.toInt()

        when (action and event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                mPrevX = event.x
                mPrevY = event.y
                mPrevRawX = event.rawX
                mPrevRawY = event.rawY
                mActivePointerId = event.getPointerId(0)
                deleteView.visibility = View.VISIBLE
                view.bringToFront()
                firePhotoEditorSDKListener(view, true)
            }
            MotionEvent.ACTION_MOVE -> {
                val pointerIndexMove = event.findPointerIndex(mActivePointerId)
                if (pointerIndexMove != -1) {
                    val currX = event.getX(pointerIndexMove)
                    val currY = event.getY(pointerIndexMove)
                    if (!mScaleGestureDetector.isInProgress) {
                        adjustTranslation(view, currX - mPrevX, currY - mPrevY)
                    }
                }
            }
            MotionEvent.ACTION_CANCEL -> mActivePointerId = INVALID_POINTER_ID
            MotionEvent.ACTION_UP -> {
                mActivePointerId = INVALID_POINTER_ID
                if (isViewInBounds(deleteView, x, y)) {
                    if (onMultiTouchListener != null)
                        onMultiTouchListener!!.onRemoveViewListener(view)
                } else if (!isViewInBounds(photoEditImageView, x, y)) {
                    view.animate().translationY(0f).translationY(0f)
                }
                deleteView.visibility = View.GONE
                firePhotoEditorSDKListener(view, false)
                val mCurrentCancelX = event.rawX
                val mCurrentCancelY = event.rawY
                if (mCurrentCancelX == mPrevRawX || mCurrentCancelY == mPrevRawY) {
                    if (view is TextView) {
                        if (onMultiTouchListener != null) {
                            onMultiTouchListener!!.onEditTextClickListener(
                                    view.text.toString(), view.currentTextColor)
                        }
                        onPhotoEditorSDKListener?.onEditTextChangeListener(
                                view.text.toString(), view.currentTextColor)
                    }
                }
            }
            MotionEvent.ACTION_POINTER_UP -> {
                val pointerIndexPointerUp = action and MotionEvent.ACTION_POINTER_INDEX_MASK shr MotionEvent.ACTION_POINTER_INDEX_SHIFT
                val pointerId = event.getPointerId(pointerIndexPointerUp)
                if (pointerId == mActivePointerId) {
                    val newPointerIndex = if (pointerIndexPointerUp == 0) 1 else 0
                    mPrevX = event.getX(newPointerIndex)
                    mPrevY = event.getY(newPointerIndex)
                    mActivePointerId = event.getPointerId(newPointerIndex)
                }
            }
        }

        return true
    }

    private fun firePhotoEditorSDKListener(view: View, isStart: Boolean) {
        if (view is TextView) {
            if (onMultiTouchListener != null) {
                if (onPhotoEditorSDKListener != null) {
                    if (isStart)
                        onPhotoEditorSDKListener.onStartViewChangeListener(ViewType.TEXT)
                    else
                        onPhotoEditorSDKListener.onStopViewChangeListener(ViewType.TEXT)
                }
            } else {
                if (onPhotoEditorSDKListener != null) {
                    if (isStart)
                        onPhotoEditorSDKListener.onStartViewChangeListener(ViewType.EMOJI)
                    else
                        onPhotoEditorSDKListener.onStopViewChangeListener(ViewType.EMOJI)
                }
            }
        } else {
            if (onPhotoEditorSDKListener != null) {
                if (isStart)
                    onPhotoEditorSDKListener.onStartViewChangeListener(ViewType.IMAGE)
                else
                    onPhotoEditorSDKListener.onStopViewChangeListener(ViewType.IMAGE)
            }
        }
    }

    private fun isViewInBounds(view: View, x: Int, y: Int): Boolean {
        view.getDrawingRect(outRect)
        view.getLocationOnScreen(location)
        outRect.offset(location[0], location[1])
        return outRect.contains(x, y)
    }

    fun setOnMultiTouchListener(onMultiTouchListener: OnMultiTouchListener) {
        this.onMultiTouchListener = onMultiTouchListener
    }

    private inner class ScaleGestureListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {

        private var mPivotX: Float = 0.toFloat()
        private var mPivotY: Float = 0.toFloat()
        private val mPrevSpanVector = Vector2D()

        override fun onScaleBegin(view: View, detector: ScaleGestureDetector): Boolean {
            mPivotX = detector.focusX
            mPivotY = detector.focusY
            mPrevSpanVector.set(detector.currentSpanVector)
            return true
        }

        override fun onScale(view: View, detector: ScaleGestureDetector): Boolean {
            val info = TransformInfo()
            info.deltaScale = detector.scaleFactor
            info.deltaAngle = mPrevSpanVector.getAngle(mPrevSpanVector, detector.currentSpanVector)
            info.deltaX = 0.0f
            info.deltaY = 0.0f
            info.pivotX = mPivotX
            info.pivotY = mPivotY
            info.minimumScale = minimumScale
            info.maximumScale = maximumScale
            move(view, info)
            return false
        }
    }

    private inner class TransformInfo {
        internal var deltaX: Float = 0.toFloat()
        internal var deltaY: Float = 0.toFloat()
        internal var deltaScale: Float = 0.toFloat()
        internal var deltaAngle: Float = 0.toFloat()
        internal var pivotX: Float = 0.toFloat()
        internal var pivotY: Float = 0.toFloat()
        internal var minimumScale: Float = 0.toFloat()
        internal var maximumScale: Float = 0.toFloat()
    }

    internal interface OnMultiTouchListener {
        fun onEditTextClickListener(text: String, colorCode: Int)
        fun onRemoveViewListener(removedView: View)
    }

    companion object {

        private const val INVALID_POINTER_ID = -1

        private fun adjustAngle(degrees: Float): Float {
            var localDegrees = degrees
            if (localDegrees > 180.0f) {
                localDegrees -= 360.0f
            } else if (localDegrees < -180.0f) {
                localDegrees += 360.0f
            }
            return localDegrees
        }

        private fun move(view: View, info: TransformInfo) {
            computeRenderOffset(view, info.pivotX, info.pivotY)
            adjustTranslation(view, info.deltaX, info.deltaY)

            var scale = view.scaleX * info.deltaScale
            scale = Math.max(info.minimumScale, Math.min(info.maximumScale, scale))
            view.scaleX = scale
            view.scaleY = scale

            val rotation = adjustAngle(view.rotation + info.deltaAngle)
            view.rotation = rotation
        }

        private fun adjustTranslation(view: View, deltaX: Float, deltaY: Float) {
            val deltaVector = floatArrayOf(deltaX, deltaY)
            view.matrix.mapVectors(deltaVector)
            view.translationX = view.translationX + deltaVector[0]
            view.translationY = view.translationY + deltaVector[1]
        }

        private fun computeRenderOffset(view: View, pivotX: Float, pivotY: Float) {
            if (view.pivotX == pivotX && view.pivotY == pivotY) {
                return
            }

            val prevPoint = floatArrayOf(0.0f, 0.0f)
            view.matrix.mapPoints(prevPoint)

            view.pivotX = pivotX
            view.pivotY = pivotY

            val currPoint = floatArrayOf(0.0f, 0.0f)
            view.matrix.mapPoints(currPoint)

            val offsetX = currPoint[0] - prevPoint[0]
            val offsetY = currPoint[1] - prevPoint[1]

            view.translationX = view.translationX - offsetX
            view.translationY = view.translationY - offsetY
        }
    }
}