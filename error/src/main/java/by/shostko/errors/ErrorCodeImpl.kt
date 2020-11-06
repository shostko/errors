@file:Suppress("unused")

package by.shostko.errors

import android.content.Context

internal class ErrorCodeImpl(
    private val idProvider: () -> String,
    private val domainProvider: () -> String,
    private val logMessageProvider: () -> String,
    private val messageProvider: ((Context) -> CharSequence?)?,
    private val fallback: Boolean
) : ErrorCode {
    override fun message(context: Context): CharSequence? = messageProvider?.invoke(context)
    override fun isFallback(): Boolean = fallback
    override fun domain(): String = domainProvider.invoke()
    override fun log(): String = logMessageProvider.invoke()
    override fun id(): String = idProvider.invoke()
}

internal class CachedErrorCode(
    private val idProvider: () -> String,
    private val domainProvider: () -> String,
    private val logMessageProvider: () -> String,
    private val messageProvider: ((Context) -> CharSequence?)?,
    private val fallback: Boolean
) : ErrorCode {

    private var domain: String? = null
    private var log: String? = null
    private var id: String? = null

    override fun message(context: Context): CharSequence? = messageProvider?.invoke(context)
    override fun isFallback(): Boolean = fallback
    override fun domain(): String = domain ?: domainProvider.invoke().apply { domain = this }
    override fun log(): String = log ?: logMessageProvider.invoke().apply { log = this }
    override fun id(): String = id ?: idProvider.invoke().apply { id = this }
}

internal class StaticErrorCode(
    private val id: String,
    private val domain: String,
    private val logMessage: String,
    private val messageProvider: (Context) -> CharSequence?,
    private val fallback: Boolean
) : ErrorCode {
    override fun message(context: Context): CharSequence? = messageProvider.invoke(context)
    override fun isFallback(): Boolean = fallback
    override fun domain(): String = domain
    override fun log(): String = logMessage
    override fun id(): String = id
}

internal class FullyStaticErrorCode(
    private val id: String,
    private val domain: String,
    private val logMessage: String,
    private val message: CharSequence?,
    private val fallback: Boolean
) : ErrorCode {
    override fun message(context: Context): CharSequence? = message
    override fun isFallback(): Boolean = fallback
    override fun domain(): String = domain
    override fun log(): String = logMessage
    override fun id(): String = id
}