package com.ahmedadeltito.photoeditorsdk

/**
 * Created by Ahmed Adel on 02/06/2017.
 */

interface OnPhotoEditorSDKListener {

    fun onEditTextChangeListener(text: String, colorCode: Int)

    fun onAddViewListener(viewType: ViewType, numberOfAddedViews: Int)

    fun onRemoveViewListener(numberOfAddedViews: Int)

    fun onStartViewChangeListener(viewType: ViewType)

    fun onStopViewChangeListener(viewType: ViewType)
}
