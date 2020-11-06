@file:Suppress("MemberVisibilityCanBePrivate")

package by.shostko.errors

import android.content.Context
import androidx.annotation.StringRes

interface ErrorCode {
    fun id(): String
    fun domain(): String
    fun log(): String
    fun isFallback(): Boolean
    fun message(context: Context): CharSequence?

    companion object {
        fun build(block: Builder.() -> Unit): ErrorCode = Builder().apply(block).build()
    }

    @Suppress("unused")
    class Builder {

        private var id: String? = null
        private var domain: String? = null
        private var fallback: Boolean = false
        private var log: String? = null
        private var messageHolder: MessageHolder = MessageHolder.Null

        fun id(id: String): Builder = apply {
            this.id = id
        }

        fun id(id: String, extra: Any?): Builder = apply {
            this.id = if (extra == null) id else "$id$extra"
        }

        fun domain(domain: String): Builder = apply {
            this.domain = domain
            if (id == null) {
                id = Error.config.domainToId(domain)
            }
        }

        fun domain(clazz: Class<*>): Builder = apply {
            this.domain = clazz.simpleName
            if (id == null) {
                id = Error.config.domainToId(clazz.simpleName)
            }
        }

        fun fallback(fallback: Boolean = true): Builder = apply {
            this.fallback = fallback
        }

        fun log(log: String): Builder = apply {
            this.log = log
        }

        fun noLog(): Builder = apply {
            this.log = null
        }

        fun message(message: CharSequence?): Builder = apply {
            messageHolder = message?.let { MessageHolder.Direct(it) } ?: MessageHolder.Null
            if (log == null && message != null) {
                log = Error.config.messageToLog(message)
            }
        }

        fun message(@StringRes messageResId: Int): Builder = apply {
            messageHolder = MessageHolder.FromRes(messageResId)
            if (log == null) {
                log = Error.config.messageToLog(messageResId)
            }
        }

        fun message(@StringRes messageResId: Int, vararg args: Any): Builder = apply {
            messageHolder = MessageHolder.FromFormattedRes(messageResId, args)
            if (log == null) {
                log = Error.config.messageToLog(messageResId, args)
            }
        }

        fun build(): ErrorCode {
            val idLocal = id
            requireNotNull(idLocal) { "Id or Domain is required to build ErrorCode" }
            return when (val messageLocal = messageHolder) {
                MessageHolder.Null -> StaticMessageErrorCode(
                    id = idLocal,
                    domain = domain,
                    logMessage = log,
                    message = null,
                    fallback = fallback
                )
                is MessageHolder.Direct -> StaticMessageErrorCode(
                    id = idLocal,
                    domain = domain,
                    logMessage = log,
                    message = messageLocal.message,
                    fallback = fallback
                )
                is MessageHolder.FromRes -> BaseResErrorCode(
                    id = idLocal,
                    domain = domain,
                    logMessage = log,
                    messageResId = messageLocal.messageResId,
                    fallback = fallback
                )
                is MessageHolder.FromFormattedRes -> BaseFormattedResErrorCode(
                    id = idLocal,
                    domain = domain,
                    logMessage = log,
                    messageResId = messageLocal.messageResId,
                    args = messageLocal.args,
                    fallback = fallback
                )
            }
        }
    }
}

private sealed class MessageHolder {
    object Null : MessageHolder()
    class Direct(val message: CharSequence) : MessageHolder()
    class FromRes(@StringRes val messageResId: Int) : MessageHolder()
    class FromFormattedRes(@StringRes val messageResId: Int, val args: Array<out Any>) : MessageHolder()
}