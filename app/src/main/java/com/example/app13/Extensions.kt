package com.example.app13

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.text.Editable
import android.text.InputType
import android.text.Spannable
import android.text.Spanned
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.text.style.TypefaceSpan
import android.text.style.URLSpan
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.example.app13.databinding.MenuItemBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textfield.TextInputEditText
import java.util.*

fun String.applySpans(representations: List<SpanRepresentation>): Editable {
    val editable = Editable.Factory.getInstance().newEditable(this)
    representations.forEach { (bold, link, italic, monospace, strikethrough, start, end) ->
        if (bold) {
            editable.setSpan(StyleSpan(Typeface.BOLD), start, end)
        }
        if (italic) {
            editable.setSpan(StyleSpan(Typeface.ITALIC), start, end)
        }
        if (link) {
            val url = getURL(start, end)
            editable.setSpan(URLSpan(url), start, end)
        }
        if (monospace) {
            editable.setSpan(TypefaceSpan("monospace"), start, end)
        }
        if (strikethrough) {
            editable.setSpan(StrikethroughSpan(), start, end)
        }
    }
    return editable
}
fun String.getURL(start: Int, end: Int): String {
    return if (end <= length) {
        TakeNote.getURLFrom(substring(start, end))
    } else TakeNote.getURLFrom(substring(start, length))
}
fun TextInputEditText.setOnNextAction(onNext: () -> Unit) {
    setRawInputType(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES)
    setOnKeyListener { v, keyCode, event ->
        if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
            onNext()
            return@setOnKeyListener true
        } else return@setOnKeyListener false
    }
    setOnEditorActionListener { v, actionId, event ->
        if (actionId == EditorInfo.IME_ACTION_NEXT) {
            onNext()
            return@setOnEditorActionListener true
        } else return@setOnEditorActionListener false
    }
}
fun Spannable.setSpan(span: Any, start: Int, end: Int) {
    try {
        if (end <= length) {
            setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        } else setSpan(span, start, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    } catch (exception: Exception) {
        exception.printStackTrace()
    }
}
fun Context.getLocale(): Locale {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        resources.configuration.locales[0]
    } else resources.configuration.locale
}
class Operation(val textId: Int, val drawableId: Int, val operation: () -> Unit)
fun Fragment.showMenu(vararg operations: Operation) {
    val context = requireContext()
    val linearLayout = LinearLayout(context)
//    linearLayout.setBackgroundColor(Color.parseColor("#2121FF"))
    linearLayout.orientation = LinearLayout.VERTICAL
    linearLayout.layoutParams = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT
    )
    val dialog = BottomSheetDialog(context)
    dialog.setContentView(linearLayout)
    for (operation in operations) {
        val item = MenuItemBinding.inflate(layoutInflater).root
        item.setText(operation.textId)
        item.setOnClickListener {
            dialog.dismiss()
            operation.operation.invoke()
        }
        item.setCompoundDrawablesRelativeWithIntrinsicBounds(operation.drawableId, 0, 0, 0)
        linearLayout.addView(item)
    }
    dialog.show()
}
