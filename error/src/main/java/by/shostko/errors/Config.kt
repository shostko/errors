@file:Suppress("unused")

package by.shostko.errors

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import android.graphics.Typeface
import android.text.ParcelableSpan
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import androidx.annotation.AnyRes

abstract class Config {
    abstract val isDebug: Boolean
    abstract val shouldAddErrorId: Boolean
    abstract fun addErrorId(id: String, text: CharSequence): CharSequence
    abstract fun unknownError(context: Context, cause: Throwable?): String
    abstract fun getResourceName(@AnyRes resId: Int): String
}

open class BaseConfig private constructor(
    private val resourceNameProvider: (Int) -> String,
    override val isDebug: Boolean = false
) : Config() {
    constructor(context: Context, isDebug: Boolean = false) : this(context.resources, isDebug)
    constructor(resources: Resources, isDebug: Boolean = false) : this({ resources.getResourceName(it) }, isDebug)
    constructor(isDebug: Boolean = false) : this({ "unknown:res/$it" }, isDebug)

    private val style: ParcelableSpan by lazy { StyleSpan(Typeface.BOLD) }

    override val shouldAddErrorId: Boolean
        get() = false

    override fun addErrorId(id: String, text: CharSequence): CharSequence = SpannableStringBuilder(id).apply {
        append(":\n")
        val end = length
        append(text)
        setSpan(style, 0, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

    override fun unknownError(context: Context, cause: Throwable?): String = context.getString(R.string.unknown)

    override fun getResourceName(resId: Int): String = resourceNameProvider(resId)
}

open class SharedPrefsConfig(
    context: Context,
    private val preferences: SharedPreferences,
    private val key: String,
    isDebug: Boolean = false
) : BaseConfig(context, isDebug) {
    override val shouldAddErrorId: Boolean
        get() = preferences.getBoolean(key, false)
}