package com.notesync.notes.framework.presentation.util


import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.TextInputLayout
import com.notesync.notes.R
import android.widget.EditText





class CustomTextInputLayout  : TextInputLayout {

    constructor(context:Context,attributeSet: AttributeSet) :super(context,attributeSet)

    constructor(context: Context):super(context)
    constructor(context: Context,attributeSet: AttributeSet,styleAttr:Int):super(context,attributeSet,styleAttr)

    override fun drawableStateChanged() {
        super.drawableStateChanged()
        clearEditTextColorfilter()
    }

    override fun setError( error: CharSequence?) {
        super.setError(error)
        clearEditTextColorfilter()
    }

    private fun clearEditTextColorfilter() {
        val editText = editText
        if (editText != null) {
            val background = editText.background
            background?.clearColorFilter()
        }
    }
}