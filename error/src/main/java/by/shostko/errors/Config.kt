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
import by.shostko.errors.Identifier.Companion.put
import org.json.JSONObject

interface Config {
    val isDebug: Boolean
    val shouldAddErrorId: Boolean
    fun addErrorId(error: Error, text: CharSequence): CharSequence
    fun unknownError(context: Context, cause: Throwable?): String

    fun prettifyDomain(domain: String): String
    fun domainToId(domain: String): String
    val nullLog: String
    fun messageToLog(message: CharSequence): String
    fun messageToLog(@StringRes resId: Int): String
    fun messageToLog(@StringRes resId: Int, args: Array<out Any?>): String

    fun resourceIdToName(@StringRes resId: Int): String
    @StringRes
    fun resourceNameToId(resName: String): Int

    fun serialize(code: ErrorCode): String
    fun deserialize(str: String): ErrorCode?

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
        override val nullLog: String = "null"
    ) : Config {

        private val style: ParcelableSpan by lazy { StyleSpan(Typeface.BOLD) }

        override val shouldAddErrorId: Boolean
            get() = false

        override fun addErrorId(error: Error, text: CharSequence) = SpannableStringBuilder(error.short()).apply {
            append(":\n")
            val end = length
            append(text)
            setSpan(style, 0, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        override fun unknownError(context: Context, cause: Throwable?): String = context.getString(R.string.by_shostko_error_unknown)

        override fun prettifyDomain(domain: String): String = domain.removeSuffix("ErrorCode")

        override fun domainToId(domain: String): String = prettifyDomain(domain).filter { it.isUpperCase() }

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

        override fun resourceIdToName(resId: Int): String = context?.resources?.getResourceName(resId) ?: resId.toString()
        override fun resourceNameToId(resName: String): Int {
            val result = if (context == null) {
                0
            } else {
                try {
                    val resPackage = resName.substringBefore(':')
                    val resType = resName.substringAfter(':').substringBefore('/')
                    val resEntry = resName.substringAfter('/')
                    context.resources.getIdentifier(resEntry, resType, resPackage)
                } catch (th: Throwable) {
                    0
                }
            }
            return if (result == 0) resName.toIntOrNull() ?: 0 else result
        }

        override fun serialize(code: ErrorCode): String = JSONObject()
            .put(code.id())
            .put("log", code.log())
            .put("fallback", code.isFallback())
            .put(CodeSerializationKey, code::class.java.name)
            .toString()

        override fun deserialize(str: String): ErrorCode? = null
    }

    class Builder {
        private var isDebug: Boolean? = null
        private var shouldAddErrorId: (() -> Boolean)? = null
        private var addErrorIdFunc: ((Error, CharSequence) -> CharSequence)? = null
        private var unknownErrorFunc: ((Context, Throwable?) -> String)? = null
        private var prettifyDomainFunc: ((String) -> String)? = null
        private var domainToIdFunc: ((String) -> String)? = null
        private var defaultDomain: String? = null
        private var nullLog: String? = null
        private var messageToLogFunc: ((CharSequence) -> String)? = null
        private var messageResToLogFunc: ((Int) -> String)? = null
        private var messageResFormattedToLogFunc: ((Int, Array<out Any?>) -> String)? = null
        private var resourceIdToNameFunc: ((Int) -> String)? = null
        private var resourceNameToIdFunc: ((String) -> Int)? = null
        private var serializeFunc: ((ErrorCode) -> String)? = null
        private var deserializeFunc: ((String) -> ErrorCode?)? = null

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

        fun addErrorId(func: (Error, CharSequence) -> CharSequence): Builder = apply {
            addErrorIdFunc = func
        }

        fun unknownError(text: String): Builder = apply {
            unknownErrorFunc = { _, _ -> text }
        }

        fun unknownError(func: (Context, Throwable?) -> String): Builder = apply {
            unknownErrorFunc = func
        }

        fun prettifyDomain(func: (String) -> String): Builder = apply {
            prettifyDomainFunc = func
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

        fun resourceIdToName(func: (Int) -> String): Builder = apply {
            resourceIdToNameFunc = func
        }

        fun resourceNameToId(func: (String) -> Int): Builder = apply {
            resourceNameToIdFunc = func
        }

        fun serialize(func: (ErrorCode) -> String): Builder = apply {
            serializeFunc = func
        }

        fun deserialize(func: (String) -> ErrorCode?): Builder = apply {
            deserializeFunc = func
        }

        fun build(): Config = ConfigImpl(
            isDebug = isDebug ?: false,
            shouldAddErrorIdFunc = shouldAddErrorId,
            addErrorIdFunc = addErrorIdFunc,
            unknownErrorFunc = unknownErrorFunc,
            prettifyDomainFunc = prettifyDomainFunc,
            domainToIdFunc = domainToIdFunc,
            nullLog = nullLog ?: Default.nullLog,
            messageToLogFunc = messageToLogFunc,
            messageResToLogFunc = messageResToLogFunc,
            messageResFormattedToLogFunc = messageResFormattedToLogFunc,
            resourceIdToNameFunc = resourceIdToNameFunc,
            resourceNameToIdFunc = resourceNameToIdFunc,
            serializeFunc = serializeFunc,
            deserializeFunc = deserializeFunc
        )
    }

    private class ConfigImpl(
        override val isDebug: Boolean,
        private val shouldAddErrorIdFunc: (() -> Boolean)?,
        private val addErrorIdFunc: ((Error, CharSequence) -> CharSequence)?,
        private val unknownErrorFunc: ((Context, Throwable?) -> String)?,
        private val prettifyDomainFunc: ((String) -> String)?,
        private val domainToIdFunc: ((String) -> String)?,
        override val nullLog: String,
        private val messageToLogFunc: ((CharSequence) -> String)?,
        private val messageResToLogFunc: ((Int) -> String)?,
        private val messageResFormattedToLogFunc: ((Int, Array<out Any?>) -> String)?,
        private val resourceIdToNameFunc: ((Int) -> String)?,
        private val resourceNameToIdFunc: ((String) -> Int)?,
        private val serializeFunc: ((ErrorCode) -> String)?,
        private val deserializeFunc: ((String) -> ErrorCode?)?
    ) : Config {
        override fun addErrorId(error: Error, text: CharSequence) = addErrorIdFunc?.invoke(error, text) ?: Default.addErrorId(error, text)
        override fun unknownError(context: Context, cause: Throwable?) = unknownErrorFunc?.invoke(context, cause) ?: Default.unknownError(context, cause)
        override fun prettifyDomain(domain: String): String = prettifyDomainFunc?.invoke(domain) ?: Default.prettifyDomain(domain)
        override fun domainToId(domain: String) = domainToIdFunc?.invoke(domain) ?: Default.domainToId(domain)
        override fun messageToLog(message: CharSequence) = messageToLogFunc?.invoke(message) ?: Default.messageToLog(message)
        override fun messageToLog(resId: Int) = messageResToLogFunc?.invoke(resId) ?: Default.messageToLog(resId)
        override fun messageToLog(resId: Int, args: Array<out Any?>) = messageResFormattedToLogFunc?.invoke(resId, args) ?: Default.messageToLog(resId, args)
        override fun resourceIdToName(resId: Int): String = resourceIdToNameFunc?.invoke(resId) ?: Default.resourceIdToName(resId)
        override fun resourceNameToId(resName: String): Int = resourceNameToIdFunc?.invoke(resName) ?: Default.resourceNameToId(resName)
        override fun serialize(code: ErrorCode): String = serializeFunc?.invoke(code) ?: Default.serialize(code)
        override fun deserialize(str: String): ErrorCode? = deserializeFunc?.invoke(str) ?: Default.deserialize(str)
        override val shouldAddErrorId: Boolean
            get() = shouldAddErrorIdFunc?.invoke() ?: Default.shouldAddErrorId
    }
}

internal fun Any.toDomain(): String = Error.config.prettifyDomain(this::class.java.simpleName)
internal fun Class<*>.toDomain(): String = Error.config.prettifyDomain(this.simpleName)
internal fun String.prettifyDomain(): String = Error.config.prettifyDomain(this)
internal fun String.domainToId(): String = Error.config.domainToId(this)
internal fun Int.toResourceName(): String = Error.config.resourceIdToName(this)
internal fun String.toResourceId(): Int = Error.config.resourceNameToId(this)
internal fun CharSequence?.messageToLog(): String = this?.let { Error.config.messageToLog(it) } ?: Error.config.nullLog
internal fun Int.messageToLog(): String = Error.config.messageToLog(this)
internal fun Int.messageToLog(args: Array<out Any?>): String = Error.config.messageToLog(this, args)