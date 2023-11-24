package com.nameiskey.intermediate2.component

import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import android.util.Patterns
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.widget.doOnTextChanged
import com.nameiskey.intermediate2.R

class EditText: AppCompatEditText {
    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    var isError: Boolean = false

    private fun init() {
        doOnTextChanged { text, _, _, _ ->
            when (inputType) {
                InputType.TYPE_TEXT_VARIATION_PASSWORD or InputType.TYPE_CLASS_TEXT -> {
                    if (text != null && text.length < PASSWORD_MINIMUM_CHARACTER) {
                        error = context.getString(
                            R.string.error_password_minimum,
                            PASSWORD_MINIMUM_CHARACTER
                        )
                        isError = true
                    } else {
                        isError = false
                    }
                }
                InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS or InputType.TYPE_CLASS_TEXT -> {
                    if (!Patterns.EMAIL_ADDRESS.matcher(text.toString()).matches()) {
                        error = context.getString(R.string.error_email_invalid)
                        isError = true
                    } else {
                        isError = false
                    }
                }
            }
        }
    }

    companion object {
        private const val PASSWORD_MINIMUM_CHARACTER = 8
    }

}