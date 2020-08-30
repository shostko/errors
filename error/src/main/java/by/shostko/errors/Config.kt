@file:Suppress("unused")

package by.shostko.errors

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Typeface
import android.text.ParcelableSpan
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import androidx.annotation.StringRes

interface Config {
    val isDebug: Boolean
    val shouldAddErrorId: Boolean
    fun addErrorId(id: String, text: CharSequence): CharSequence
    fun unknownError(context: Context, cause: Throwable?): String

    fun domainToId(domain: String): String
    val defaultDomain: String
    val nullLog: String
    fun messageToLog(message: CharSequence): String
    fun messageToLog(@StringRes resId: Int): String
    fun messageToLog(@StringRes resId: Int, args: Array<out Any?>): String

    companion object {
        fun build(func: Builder.() -> Unit): Config = Builder().run {
            func()
            build()
        }
    }

    object Default : Base()

    open class Base(
        private val context: Context? = null,
        override val isDebug: Boolean = false,
        override val defaultDomain: String = "Error",
        override val nullLog: String = "null"
    ) : Config {

        private val style: ParcelableSpan by lazy { StyleSpan(Typeface.BOLD) }

        override val shouldAddErrorId: Boolean
            get() = false

        override fun addErrorId(id: String, text: CharSequence) = SpannableStringBuilder(id).apply {
            append(":\n")
            val end = length
            append(text)
            setSpan(style, 0, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        override fun unknownError(context: Context, cause: Throwable?): String = context.getString(R.string.by_shostko_error_unknown)

        override fun domainToId(domain: String): String = domain.removeSuffix("ErrorCode").filter { it.isUpperCase() }

        override fun messageToLog(message: CharSequence): String = message.toString()

        override fun messageToLog(resId: Int): String = context?.resources?.getResourceName(resId) ?: "unknown:res/$resId"

        override fun messageToLog(resId: Int, args: Array<out Any?>): String = StringBuilder().apply {
            append(messageToLog(resId))
            append(" formatted with: ")
            args.forEachIndexed { index, arg ->
                if (index > 0) {
                    append("; ")
                }
                append(arg)
            }
        }.toString()
    }

    class Builder {
        private var isDebug: Boolean? = null
        private var shouldAddErrorId: (() -> Boolean)? = null
        private var addErrorIdFunc: ((String, CharSequence) -> CharSequence)? = null
        private var unknownErrorFunc: ((Context, Throwable?) -> String)? = null
        private var domainToIdFunc: ((String) -> String)? = null
        private var defaultDomain: String? = null
        private var nullLog: String? = null
        private var messageToLogFunc: ((CharSequence) -> String)? = null
        private var messageResToLogFunc: ((Int) -> String)? = null
        private var messageResFormattedToLogFunc: ((Int, Array<out Any?>) -> String)? = null

        fun debug(debug: Boolean): Builder = apply {
            isDebug = debug
        }

        fun shouldAddErrorId(add: Boolean): Builder = apply {
            shouldAddErrorId = { add }
        }

        fun shouldAddErrorId(func: () -> Boolean): Builder = apply {
            shouldAddErrorId = func
        }

        fun shouldAddErrorId(preferences: SharedPreferences, key: String): Builder = apply {
            shouldAddErrorId = { preferences.getBoolean(key, false) }
        }

        fun addErrorId(func: (String, CharSequence) -> CharSequence): Builder = apply {
            addErrorIdFunc = func
        }

        fun unknownError(text: String): Builder = apply {
            unknownErrorFunc = { _, _ -> text }
        }

        fun unknownError(func: (Context, Throwable?) -> String): Builder = apply {
            unknownErrorFunc = func
        }

        fun domainToId(func: (String) -> String): Builder = apply {
            domainToIdFunc = func
        }

        fun defaultDomain(domain: String): Builder = apply {
            defaultDomain = domain
        }

        fun nullLog(log: String): Builder = apply {
            nullLog = log
        }

        fun messageToLog(func: (CharSequence) -> String): Builder = apply {
            messageToLogFunc = func
        }

        fun messageResToLog(func: (Int) -> String): Builder = apply {
            messageResToLogFunc = func
        }

        fun messageResFormattedToLog(func: (Int, Array<out Any?>) -> String): Builder = apply {
            messageResFormattedToLogFunc = func
        }

        fun build(): Config = ConfigImpl(
            isDebug = isDebug ?: false,
            shouldAddErrorIdFunc = shouldAddErrorId,
            addErrorIdFunc = addErrorIdFunc,
            unknownErrorFunc = unknownErrorFunc,
            domainToIdFunc = domainToIdFunc,
            defaultDomain = defaultDomain ?: Default.defaultDomain,
            nullLog = nullLog ?: Default.nullLog,
            messageToLogFunc = messageToLogFunc,
            messageResToLogFunc = messageResToLogFunc,
            messageResFormattedToLogFunc = messageResFormattedToLogFunc
        )
    }

    private class ConfigImpl(
        override val isDebug: Boolean,
        private val shouldAddErrorIdFunc: (() -> Boolean)?,
        private val addErrorIdFunc: ((String, CharSequence) -> CharSequence)?,
        private val unknownErrorFunc: ((Context, Throwable?) -> String)?,
        private val domainToIdFunc: ((String) -> String)?,
        override val defaultDomain: String,
        override val nullLog: String,
        private val messageToLogFunc: ((CharSequence) -> String)?,
        private val messageResToLogFunc: ((Int) -> String)?,
        private val messageResFormattedToLogFunc: ((Int, Array<out Any?>) -> String)?
    ) : Config {
        override fun addErrorId(id: String, text: CharSequence) = addErrorIdFunc?.invoke(id, text) ?: Default.addErrorId(id, text)
        override fun unknownError(context: Context, cause: Throwable?) = unknownErrorFunc?.invoke(context, cause) ?: Default.unknownError(context, cause)
        override fun domainToId(domain: String) = domainToIdFunc?.invoke(domain) ?: Default.domainToId(domain)
        override fun messageToLog(message: CharSequence) = messageToLogFunc?.invoke(message) ?: Default.messageToLog(message)
        override fun messageToLog(resId: Int) = messageResToLogFunc?.invoke(resId) ?: Default.messageToLog(resId)
        override fun messageToLog(resId: Int, args: Array<out Any?>) = messageResFormattedToLogFunc?.invoke(resId, args) ?: Default.messageToLog(resId, args)
        override val shouldAddErrorId: Boolean
            get() = shouldAddErrorIdFunc?.invoke() ?: Default.shouldAddErrorId
    }
}