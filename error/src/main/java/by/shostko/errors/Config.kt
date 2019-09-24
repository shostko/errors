package by.shostko.errors

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Typeface
import android.text.ParcelableSpan
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan

abstract class Config {
    abstract val isDebug: Boolean
    abstract val shouldAddErrorId: Boolean
    abstract fun addErrorId(id: String, text: String): CharSequence
    abstract fun unknownError(context: Context): String
}

open class BaseConfig(override val isDebug: Boolean = false) : Config() {

    private val style: ParcelableSpan by lazy { StyleSpan(Typeface.BOLD) }

    override val shouldAddErrorId: Boolean
        get() = false

    override fun addErrorId(id: String, text: String): CharSequence = SpannableStringBuilder(id).apply {
        append(":\n")
        val end = length
        append(text)
        setSpan(style, 0, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

    override fun unknownError(context: Context): String = context.getString(R.string.unknown)
}

open class SharedPrefsConfig(private val preferences: SharedPreferences,
                             private val key: String,
                             override val isDebug: Boolean = false) : BaseConfig() {
    override val shouldAddErrorId: Boolean
        get() = preferences.getBoolean(key, false)
}