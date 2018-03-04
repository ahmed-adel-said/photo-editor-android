package com.ahmedadeltito.photoeditorsdk

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Typeface
import android.os.Environment
import android.support.annotation.ColorInt
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import java.io.File
import java.io.FileOutputStream
import java.util.*

/**
 * Created by Ahmed Adel on 02/06/2017.
 */

class PhotoEditorSDK private constructor(photoEditorSDKBuilder: PhotoEditorSDKBuilder) : MultiTouchListener.OnMultiTouchListener {

    private val context: Context
    private val parentView: RelativeLayout?
    private val imageView: ImageView?
    private val deleteView: View?
    private val brushDrawingView: BrushDrawingView?
    private val addedViews: MutableList<View>
    private var onPhotoEditorSDKListener: OnPhotoEditorSDKListener? = null
    private var addTextRootView: View? = null

    val eraserSize: Float
        get() = brushDrawingView?.eraserSize ?: 0f

    var brushSize: Float
        get() = brushDrawingView?.brushSize ?: 0f
        set(size) {
            if (brushDrawingView != null)
                brushDrawingView.brushSize = size
        }

    var brushColor: Int
        get() = brushDrawingView?.brushColor ?: 0
        set(@ColorInt color) {
            brushDrawingView?.brushColor = color
        }

    private val isSDCARDMounted: Boolean
        get() {
            val status = Environment.getExternalStorageState()
            return status == Environment.MEDIA_MOUNTED
        }

    init {
        this.context = photoEditorSDKBuilder.context
        this.parentView = photoEditorSDKBuilder.parentView
        this.imageView = photoEditorSDKBuilder.imageView
        this.deleteView = photoEditorSDKBuilder.deleteView
        this.brushDrawingView = photoEditorSDKBuilder.brushDrawingView
        addedViews = ArrayList()
    }

    @SuppressLint("InflateParams")
    fun addImage(desiredImage: Bitmap) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val imageRootView = inflater.inflate(R.layout.photo_editor_sdk_image_item_list, null)
        val imageView = imageRootView.findViewById(R.id.photo_editor_sdk_image_iv) as ImageView
        imageView.setImageBitmap(desiredImage)
        imageView.layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT)
        val multiTouchListener = MultiTouchListener(deleteView!!,
                this.imageView!!, onPhotoEditorSDKListener)
        multiTouchListener.setOnMultiTouchListener(this)
        imageRootView.setOnTouchListener(multiTouchListener)
        val params = RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE)
        parentView!!.addView(imageRootView, params)
        addedViews.add(imageRootView)
        if (onPhotoEditorSDKListener != null)
            onPhotoEditorSDKListener!!.onAddViewListener(ViewType.IMAGE, addedViews.size)
    }

    @SuppressLint("InflateParams")
    fun addText(text: String, colorCodeTextView: Int) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        addTextRootView = inflater.inflate(R.layout.photo_editor_sdk_text_item_list, null)
        val addTextView = addTextRootView!!.findViewById(R.id.photo_editor_sdk_text_tv) as TextView
        addTextView.gravity = Gravity.CENTER
        addTextView.text = text
        if (colorCodeTextView != -1)
            addTextView.setTextColor(colorCodeTextView)
        val multiTouchListener = MultiTouchListener(deleteView!!,
                this.imageView!!, onPhotoEditorSDKListener)
        multiTouchListener.setOnMultiTouchListener(this)
        addTextRootView!!.setOnTouchListener(multiTouchListener)
        val params = RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE)
        parentView!!.addView(addTextRootView, params)
        addedViews.add(addTextRootView!!)
        if (onPhotoEditorSDKListener != null)
            onPhotoEditorSDKListener!!.onAddViewListener(ViewType.TEXT, addedViews.size)
    }

    @SuppressLint("InflateParams")
    fun addEmoji(emojiName: String, emojiFont: Typeface) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val emojiRootView = inflater.inflate(R.layout.photo_editor_sdk_text_item_list, null)
        val emojiTextView = emojiRootView.findViewById(R.id.photo_editor_sdk_text_tv) as TextView
        emojiTextView.typeface = emojiFont
        emojiTextView.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        emojiTextView.text = convertEmoji(emojiName)
        val multiTouchListener = MultiTouchListener(deleteView!!,
                this.imageView!!, onPhotoEditorSDKListener)
        multiTouchListener.setOnMultiTouchListener(this)
        emojiRootView.setOnTouchListener(multiTouchListener)
        val params = RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE)
        parentView!!.addView(emojiRootView, params)
        addedViews.add(emojiRootView)
        if (onPhotoEditorSDKListener != null)
            onPhotoEditorSDKListener!!.onAddViewListener(ViewType.EMOJI, addedViews.size)
    }

    fun setBrushDrawingMode(brushDrawingMode: Boolean) {
        brushDrawingView?.setBrushDrawingMode(brushDrawingMode)
    }

    fun setBrushEraserSize(brushEraserSize: Float) {
        brushDrawingView?.setBrushEraserSize(brushEraserSize)
    }

    fun setBrushEraserColor(@ColorInt color: Int) {
        brushDrawingView?.setBrushEraserColor(color)
    }

    fun brushEraser() {
        brushDrawingView?.brushEraser()
    }

    fun viewUndo() {
        if (addedViews.size > 0) {
            parentView!!.removeView(addedViews.removeAt(addedViews.size - 1))
            if (onPhotoEditorSDKListener != null)
                onPhotoEditorSDKListener!!.onRemoveViewListener(addedViews.size)
        }
    }

    private fun viewUndo(removedView: View) {
        if (addedViews.size > 0) {
            if (addedViews.contains(removedView)) {
                parentView!!.removeView(removedView)
                addedViews.remove(removedView)
                if (onPhotoEditorSDKListener != null)
                    onPhotoEditorSDKListener!!.onRemoveViewListener(addedViews.size)
            }
        }
    }

    fun clearBrushAllViews() {
        brushDrawingView?.clearAll()
    }

    fun clearAllViews() {
        for (i in addedViews.indices) {
            parentView!!.removeView(addedViews[i])
        }
        brushDrawingView?.clearAll()
    }

    fun saveImage(folderName: String, imageName: String): String {
        var selectedOutputPath = ""
        if (isSDCARDMounted) {
            val mediaStorageDir = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), folderName)
            // Create a storage directory if it does not exist
            if (!mediaStorageDir.exists()) {
                if (!mediaStorageDir.mkdirs()) {
                    Log.d("PhotoEditorSDK", "Failed to create directory")
                }
            }
            // Create a media file name
            selectedOutputPath = mediaStorageDir.path + File.separator + imageName
            Log.d("PhotoEditorSDK", "selected camera path $selectedOutputPath")
            val file = File(selectedOutputPath)
            try {
                val out = FileOutputStream(file)
                if (parentView != null) {
                    parentView.isDrawingCacheEnabled = true
                    parentView.drawingCache.compress(Bitmap.CompressFormat.JPEG, 80, out)
                }
                out.flush()
                out.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
        return selectedOutputPath
    }

    private fun convertEmoji(emoji: String): String {
        val returnedEmoji: String
        returnedEmoji = try {
            val convertEmojiToInt = Integer.parseInt(emoji.substring(2), 16)
            getEmojiByUnicode(convertEmojiToInt)
        } catch (e: NumberFormatException) {
            ""
        }
        return returnedEmoji
    }

    private fun getEmojiByUnicode(unicode: Int): String {
        return String(Character.toChars(unicode))
    }

    fun setOnPhotoEditorSDKListener(onPhotoEditorSDKListener: OnPhotoEditorSDKListener) {
        this.onPhotoEditorSDKListener = onPhotoEditorSDKListener
        brushDrawingView!!.setOnPhotoEditorSDKListener(onPhotoEditorSDKListener)
    }

    override fun onEditTextClickListener(text: String, colorCode: Int) {
        if (addTextRootView != null) {
            parentView!!.removeView(addTextRootView)
            addedViews.remove(addTextRootView!!)
        }
    }

    override fun onRemoveViewListener(removedView: View) {
        viewUndo(removedView)
    }

    class PhotoEditorSDKBuilder(internal val context: Context) {
        internal var parentView: RelativeLayout? = null
        internal var imageView: ImageView? = null
        internal var deleteView: View? = null
        internal var brushDrawingView: BrushDrawingView? = null

        fun parentView(parentView: RelativeLayout): PhotoEditorSDKBuilder {
            this.parentView = parentView
            return this
        }

        fun childView(imageView: ImageView): PhotoEditorSDKBuilder {
            this.imageView = imageView
            return this
        }

        fun deleteView(deleteView: View): PhotoEditorSDKBuilder {
            this.deleteView = deleteView
            return this
        }

        fun brushDrawingView(brushDrawingView: BrushDrawingView): PhotoEditorSDKBuilder {
            this.brushDrawingView = brushDrawingView
            return this
        }

        fun buildPhotoEditorSDK(): PhotoEditorSDK {
            return PhotoEditorSDK(this)
        }
    }
}
