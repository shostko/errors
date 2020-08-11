@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package by.shostko.errors

import android.content.Context
import android.os.Bundle
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
        private var idProvider: (() -> String)? = null
        private var domainProvider: (() -> String)? = null
        private var logProvider: (() -> String)? = null
        private var extra: Any? = null
        private var fallback: Boolean = false
        private var messageProvider: ((Context) -> CharSequence?)? = null

        fun noCache(): Builder = apply {
            cached = false
        }

        fun id(id: String): Builder = apply {
            idProvider = { id }
        }

        fun id(id: String, extra: Any?): Builder = apply {
            idProvider = { id }
            this.extra = extra
        }

        fun domain(domain: String): Builder = apply {
            domainProvider = { domain }
        }

        fun domain(domain: Class<*>): Builder = apply {
            domainProvider = { domain.simpleName }
        }

        fun domain(domain: () -> String): Builder = apply {
            domainProvider = domain
        }

        fun log(log: String): Builder = apply {
            logProvider = { log }
        }

        fun noLog(): Builder = apply {
            logProvider = NullLogProvider
        }

        fun fallback(fallback: Boolean = true): Builder = apply {
            this.fallback = fallback
        }

        fun message(message: (Context) -> CharSequence?): Builder = apply {
            messageProvider = message
        }

        fun message(message: CharSequence?): Builder = apply {
            messageProvider = message?.let { MessageProvider(it) }
            if (logProvider == null) {
                logProvider = message?.let { LogMessageProvider(it) } ?: NullLogProvider
            }
        }

        fun message(@StringRes messageResId: Int): Builder = apply {
            messageProvider = ResMessageProvider(messageResId)
            if (logProvider == null) {
                logProvider = ResLogMessageProvider(messageResId)
            }
        }

        fun message(@StringRes messageResId: Int, vararg args: Any): Builder = apply {
            messageProvider = FormattedResMessageProvider(messageResId, args)
            if (logProvider == null) {
                logProvider = FormattedResLogMessageProvider(messageResId, args)
            }
        }

        fun build(): ErrorCode {
            val idProvider = idProvider ?: domainProvider?.let { IdProvider(it) }
            requireNotNull(idProvider) { "Id or Domain is required to build ErrorCode" }
            val creator: (
                    () -> String, // id
                    () -> String, // domain
                    () -> String, // log
                    Any?, // extra
                    ((Context) -> CharSequence?)?, // message
                    Boolean // fallback
            ) -> ErrorCode = if (cached) ::CachedErrorCode else ::ErrorCodeImpl
            return creator(
                idProvider!!,
                domainProvider ?: DefaultDomainProvider,
                logProvider ?: NullLogProvider,
                extra,
                messageProvider,
                fallback
            )
        }
    }
}