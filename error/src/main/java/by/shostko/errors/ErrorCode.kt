@file:Suppress("MemberVisibilityCanBePrivate", "unused")

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

    class Builder {

        private var cached: Boolean = true
        private var fallback: Boolean = false
        private var idHolder: IdHolder? = null
        private var domainHolder: DomainHolder = DomainHolder.Default
        private var logHolder: LogHolder = LogHolder.Null
        private var messageHolder: MessageHolder = MessageHolder.Null

        fun noCache(): Builder = apply {
            cached = false
        }

        fun id(id: String): Builder = apply {
            this.idHolder = IdHolder.Direct(id)
        }

        fun id(id: String, extra: Any?): Builder = apply {
            this.idHolder = IdHolder.Direct(id, extra)
        }

        fun domain(domain: String): Builder = apply {
            domainHolder = DomainHolder.Direct(domain)
            if (idHolder == null) {
                idHolder = IdHolder.FromDomain(domain)
            }
        }

        fun domain(clazz: Class<*>): Builder = apply {
            domainHolder = DomainHolder.FromClass(clazz)
            if (idHolder == null) {
                idHolder = IdHolder.FromDomainClass(clazz)
            }
        }

        fun domain(domain: () -> String): Builder = apply {
            domainHolder = DomainHolder.Provider(domain)
            if (idHolder == null) {
                idHolder = IdHolder.FromDomainProvider(domain)
            }
        }

        fun log(log: String): Builder = apply {
            logHolder = LogHolder.Direct(log)
        }

        fun noLog(): Builder = apply {
            logHolder = LogHolder.Null
        }

        fun fallback(fallback: Boolean = true): Builder = apply {
            this.fallback = fallback
        }

        fun message(provider: (Context) -> CharSequence?): Builder = apply {
            messageHolder = MessageHolder.Provider(provider)
        }

        fun message(message: CharSequence?): Builder = apply {
            messageHolder = message?.let { MessageHolder.Direct(it) } ?: MessageHolder.Null
            if (logHolder == LogHolder.Null) {
                logHolder = message?.let { LogHolder.FromMessage(it) } ?: LogHolder.Null
            }
        }

        fun message(@StringRes messageResId: Int): Builder = apply {
            messageHolder = MessageHolder.FromRes(messageResId)
            if (logHolder == LogHolder.Null) {
                logHolder = LogHolder.FromRes(messageResId)
            }
        }

        fun message(@StringRes messageResId: Int, vararg args: Any): Builder = apply {
            messageHolder = MessageHolder.FromFormattedRes(messageResId, args)
            if (logHolder == LogHolder.Null) {
                logHolder = LogHolder.FromFormattedRes(messageResId, args)
            }
        }

        fun build(): ErrorCode {
            val idLocal = idHolder
            requireNotNull(idLocal) { "Id or Domain is required to build ErrorCode" }
            val isGeneralStatic = idLocal.isStatic && domainHolder.isStatic && logHolder.isStatic
            val isAllStatic = isGeneralStatic && messageHolder.isStatic
            return when {
                isAllStatic -> FullyStaticErrorCode(
                    id = idLocal.static(),
                    domain = domainHolder.static(),
                    logMessage = logHolder.static(),
                    message = messageHolder.static(),
                    fallback = fallback
                )
                isGeneralStatic -> StaticErrorCode(
                    id = idLocal.static(),
                    domain = domainHolder.static(),
                    logMessage = logHolder.static(),
                    messageProvider = messageHolder.function(),
                    fallback = fallback
                )
                cached -> CachedErrorCode(
                    idProvider = idLocal.function(),
                    domainProvider = domainHolder.function(),
                    logMessageProvider = logHolder.function(),
                    messageProvider = messageHolder.function(),
                    fallback = fallback
                )
                else -> ErrorCodeImpl(
                    idProvider = idLocal.function(),
                    domainProvider = domainHolder.function(),
                    logMessageProvider = logHolder.function(),
                    messageProvider = messageHolder.function(),
                    fallback = fallback
                )
            }
        }
    }
}

private abstract class Holder<T>(val isStatic: Boolean) {
    abstract fun static(): T
    abstract fun function(): () -> T
}

private sealed class IdHolder(isStatic: Boolean) : Holder<String>(isStatic) {

    class Direct(private val id: String) : IdHolder(true) {
        constructor(id: String, extra: Any?) : this(if (extra == null) id else "$id$extra")

        override fun static(): String = id
        override fun function(): () -> String = { id }
    }

    class FromDomain(private val domain: String) : IdHolder(true) {
        override fun static(): String = Error.config.domainToId(domain)
        override fun function(): () -> String = { Error.config.domainToId(domain) }
    }

    class FromDomainClass(private val domain: Class<*>) : IdHolder(true) {
        override fun static(): String = Error.config.domainToId(domain.simpleName)
        override fun function(): () -> String = { Error.config.domainToId(domain.simpleName) }
    }

    class FromDomainProvider(private val domainProvider: () -> String) : IdHolder(false) {
        override fun static(): String = Error.config.domainToId(domainProvider.invoke())
        override fun function(): () -> String = { Error.config.domainToId(domainProvider.invoke()) }
    }
}

private sealed class DomainHolder(isStatic: Boolean) : Holder<String>(isStatic) {

    object Default : DomainHolder(true) {
        override fun static(): String = Error.config.defaultDomain
        override fun function(): () -> String = { Error.config.defaultDomain }
    }

    class Direct(private val domain: String) : DomainHolder(true) {
        override fun static(): String = domain
        override fun function(): () -> String = { domain }
    }

    class FromClass(private val clazz: Class<*>) : DomainHolder(true) {
        override fun static(): String = clazz.simpleName
        override fun function(): () -> String = { clazz.simpleName }
    }

    class Provider(private val provider: () -> String) : DomainHolder(false) {
        override fun static(): String = provider.invoke()
        override fun function(): () -> String = provider
    }
}

private sealed class LogHolder : Holder<String>(true) {

    object Null : LogHolder() {
        override fun static(): String = Error.config.nullLog
        override fun function(): () -> String = { Error.config.nullLog }
    }

    class Direct(private val log: String) : LogHolder() {
        override fun static(): String = log
        override fun function(): () -> String = { log }
    }

    class FromMessage(private val message: CharSequence) : LogHolder() {
        override fun static(): String = Error.config.messageToLog(message)
        override fun function(): () -> String = { Error.config.messageToLog(message) }
    }

    class FromRes(@StringRes private val messageResId: Int) : LogHolder() {
        override fun static(): String = Error.config.messageToLog(messageResId)
        override fun function(): () -> String = { Error.config.messageToLog(messageResId) }
    }

    class FromFormattedRes(@StringRes private val messageResId: Int, private val args: Array<out Any>) : LogHolder() {
        override fun static(): String = Error.config.messageToLog(messageResId, args)
        override fun function(): () -> String = { Error.config.messageToLog(messageResId, args) }
    }
}

private sealed class MessageHolder(val isStatic: Boolean) {
    abstract fun static(): CharSequence?
    abstract fun function(): (Context) -> CharSequence?

    object Null : MessageHolder(true) {
        override fun static(): CharSequence? = null
        override fun function(): (Context) -> CharSequence? = { null }
    }

    class Direct(private val message: CharSequence) : MessageHolder(true) {
        override fun static(): CharSequence? = message
        override fun function(): (Context) -> CharSequence? = { message }
    }

    class Provider(private val provider: (Context) -> CharSequence?) : MessageHolder(false) {
        override fun static(): CharSequence? = throw UnsupportedOperationException("Can't extract static message from provider")
        override fun function(): (Context) -> CharSequence? = provider
    }

    class FromRes(@StringRes private val messageResId: Int) : MessageHolder(false) {
        override fun static(): CharSequence? = throw UnsupportedOperationException("Can't extract static message from messageResId")
        override fun function(): (Context) -> CharSequence? = { it.getString(messageResId) }
    }

    class FromFormattedRes(@StringRes private val messageResId: Int, private val args: Array<out Any>) : MessageHolder(false) {
        override fun static(): CharSequence? = throw UnsupportedOperationException("Can't extract static message from messageResId with args")
        override fun function(): (Context) -> CharSequence? = { it.getString(messageResId, *args) }
    }
}