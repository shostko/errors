@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package by.shostko.errors

import android.content.Context
import androidx.annotation.AnyRes
import androidx.annotation.StringRes

interface ErrorCode {
    fun id(): String
    fun domain(): String
    fun log(): String
    fun isFallback(): Boolean
    fun message(context: Context): CharSequence?

    class Builder {

        private var wrapper: ((ErrorCode) -> ErrorCode)? = null
        private var cached: Boolean = true
        private var idProvider: (() -> String)? = null
        private var domainProvider: (() -> String)? = null
        private var domainToIdMapper: ((String) -> String)? = null
        private var logProvider: (() -> String)? = null
        private var extraProvider: (() -> Any?)? = null
        private var fallbackProvider: (() -> Boolean)? = null
        private var messageProvider: ((Context) -> CharSequence?)? = null

        fun wrap(wrapper: (ErrorCode) -> ErrorCode): Builder = apply {
            this@Builder.wrapper = wrapper
        }

        fun noCache(): Builder = apply {
            cached = false
        }

        fun id(id: () -> String): Builder = apply {
            idProvider = id
        }

        fun id(id: String): Builder = apply {
            idProvider = { id }
        }

        fun id(id: () -> String, extra: () -> Any?): Builder = apply {
            idProvider = id
            extraProvider = extra
        }

        fun id(id: String, extra: () -> Any?): Builder = apply {
            idProvider = { id }
            extraProvider = extra
        }

        fun id(id: () -> String, extra: Any?): Builder = apply {
            idProvider = id
            extraProvider = extra?.let { ExtraProvider(it) }
        }

        fun id(id: String, extra: Any?): Builder = apply {
            idProvider = { id }
            extraProvider = extra?.let { ExtraProvider(it) }
        }

        fun extra(extra: () -> Any?): Builder = apply {
            extraProvider = extra
        }

        fun extra(extra: Any?): Builder = apply {
            extraProvider = extra?.let { ExtraProvider(it) }
        }

        fun domain(domain: () -> String): Builder = apply {
            domainProvider = domain
        }

        fun domain(domain: String): Builder = apply {
            domainProvider = { domain }
        }

        fun domain(domain: () -> String, idMapper: (String) -> String): Builder = apply {
            domainProvider = domain
            domainToIdMapper = idMapper
        }

        fun domain(domain: String, idMapper: (String) -> String): Builder = apply {
            domainProvider = { domain }
            domainToIdMapper = idMapper
        }

        fun domainToIdMapper(mapper: (String) -> String): Builder = apply {
            domainToIdMapper = mapper
        }

        fun log(log: () -> String): Builder = apply {
            logProvider = log
        }

        fun log(log: String): Builder = apply {
            logProvider = { log }
        }

        fun noLog(): Builder = apply {
            logProvider = NullLogProvider
        }

        fun fallback(fallback: () -> Boolean): Builder = apply {
            fallbackProvider = fallback
        }

        fun fallback(fallback: Boolean = true): Builder = apply {
            fallbackProvider = if (fallback) TrueFallbackProvider else FalseFallbackProvider
        }

        fun message(message: (Context) -> CharSequence?): Builder = apply {
            messageProvider = message
        }

        fun message(message: CharSequence?): Builder = apply {
            messageProvider = message?.let { MessageProvider(it) }
            logProvider = message?.let { LogMessageProvider(it) } ?: NullLogProvider
        }

        fun message(@StringRes messageResId: Int): Builder = apply {
            messageProvider = ResMessageProvider(messageResId)
            logProvider = ResLogMessageProvider(messageResId)
        }

        fun message(@StringRes messageResId: Int, vararg args: Any): Builder = apply {
            messageProvider = FormattedResMessageProvider(messageResId, args)
            logProvider = FormattedResLogMessageProvider(messageResId, args)
        }

        fun build(): ErrorCode {
            val idProvider = idProvider ?: domainProvider?.let { IdProvider(it, domainToIdMapper ?: DomainToIdMapper) }
            requireNotNull(idProvider) { "Id or Domain is required to build ErrorCode" }
            var code: ErrorCode = BaseErrorCode(
                idProvider,
                domainProvider ?: DomainProvider,
                logProvider ?: NullLogProvider,
                extraProvider,
                messageProvider,
                fallbackProvider ?: FalseFallbackProvider
            )
            code = wrapper?.invoke(code) ?: code
            return if (cached) code.cached() else code
        }
    }
}

internal class IdProvider(
    private var domainProvider: () -> String,
    private var domainToIdMapper: (String) -> String
) : () -> String {
    override fun invoke(): String = domainToIdMapper(domainProvider())
}

internal object DomainProvider : () -> String {
    override fun invoke(): String = "Error"
}

internal object DomainToIdMapper : (String) -> String {
    override fun invoke(domain: String): String = domain.removeSuffix("ErrorCode").filter { it.isUpperCase() }
}

internal object NullLogProvider : () -> String {
    override fun invoke(): String = "null"
}

internal class LogMessageProvider(
    private val message: CharSequence
) : () -> String {
    override fun invoke(): String = message.toString()
}

internal class ResLogMessageProvider(
    @AnyRes
    private val resId: Int
) : () -> String {
    override fun invoke(): String = Error.config.getResourceName(resId)
}

internal class FormattedResLogMessageProvider(
    @AnyRes
    private val resId: Int,
    private val args: Array<out Any>
) : () -> String {
    override fun invoke(): String = StringBuilder().apply {
        append(Error.config.getResourceName(resId))
        append(" formatted with: ")
        args.forEachIndexed { index, arg ->
            if (index > 0) {
                append("; ")
            }
            append(arg)
        }
    }.toString()
}

internal class ExtraProvider(
    private val extra: Any
) : () -> Any? {
    override fun invoke(): Any? = extra
}

internal object TrueFallbackProvider : () -> Boolean {
    override fun invoke(): Boolean = true
}

internal object FalseFallbackProvider : () -> Boolean {
    override fun invoke(): Boolean = false
}

internal class MessageProvider(
    private val message: CharSequence
) : (Context) -> CharSequence? {
    override fun invoke(context: Context): CharSequence? = message
}

internal class ResMessageProvider(
    @StringRes
    private val messageResId: Int
) : (Context) -> CharSequence? {
    override fun invoke(context: Context): CharSequence? = context.getString(messageResId)
}

internal class FormattedResMessageProvider(
    @StringRes
    private val messageResId: Int,
    private val args: Array<out Any>
) : (Context) -> CharSequence? {
    override fun invoke(context: Context): CharSequence? = context.getString(messageResId, *args)
}