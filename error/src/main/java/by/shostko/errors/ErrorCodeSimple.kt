@file:Suppress("unused")

package by.shostko.errors

import android.content.Context
import androidx.annotation.StringRes

sealed class BaseSimpleErrorCode(
    private val id: String,
    private val domain: String
) : ErrorCode {
    final override fun id(): String = id
    final override fun domain(): String = domain
    final override fun isFallback(): Boolean = false

    fun asFallback(): ErrorCode = BaseFallbackDelegate(this)
}

open class NoMessageErrorCode(
    private val id: String,
    private val domain: String
) : BaseSimpleErrorCode(id, domain) {

    constructor(
        id: String,
        domain: Class<*>
    ) : this(
        id = id,
        domain = domain.simpleName
    )

    constructor(
        domain: String
    ) : this(
        id = Error.config.domainToId(domain),
        domain = domain
    )

    override fun log(): String = Error.config.nullLog
    override fun message(context: Context): CharSequence? = null
}

open class SimpleErrorCode(
    private val id: String,
    private val domain: String,
    private val message: CharSequence?
) : BaseSimpleErrorCode(id, domain) {

    constructor(
        id: String,
        domain: Class<*>,
        message: CharSequence?
    ) : this(
        id = id,
        domain = domain.simpleName,
        message = message
    )

    constructor(
        domain: String,
        message: CharSequence?
    ) : this(
        id = Error.config.domainToId(domain),
        domain = domain,
        message = message
    )

    override fun log(): String = message?.let { Error.config.messageToLog(it) } ?: Error.config.nullLog
    override fun message(context: Context): CharSequence? = message
}

open class ResErrorCode(
    private val id: String,
    private val domain: String,
    @StringRes private val messageResId: Int
) : BaseSimpleErrorCode(id, domain) {

    constructor(
        id: String,
        domain: Class<*>,
        @StringRes messageResId: Int
    ) : this(
        id = id,
        domain = domain.simpleName,
        messageResId = messageResId
    )

    constructor(
        domain: String,
        @StringRes messageResId: Int
    ) : this(
        id = Error.config.domainToId(domain),
        domain = domain,
        messageResId = messageResId
    )

    override fun log(): String = Error.config.messageToLog(messageResId)
    override fun message(context: Context): CharSequence? = context.getString(messageResId)
}

open class FormattedResErrorCode(
    private val id: String,
    private val domain: String,
    @StringRes private val messageResId: Int,
    private vararg val args: Any?
) : BaseSimpleErrorCode(id, domain) {

    constructor(
        id: String,
        domain: Class<*>,
        @StringRes messageResId: Int,
        vararg args: Any?
    ) : this(
        id = id,
        domain = domain.simpleName,
        messageResId = messageResId,
        args = args
    )

    constructor(
        domain: String,
        @StringRes messageResId: Int,
        vararg args: Any?
    ) : this(
        id = Error.config.domainToId(domain),
        domain = domain,
        messageResId = messageResId,
        args = args
    )

    override fun log(): String = Error.config.messageToLog(messageResId, args)
    override fun message(context: Context): CharSequence? = context.getString(messageResId, *args)
}

private class BaseFallbackDelegate(
    code: ErrorCode
) : ErrorCode by code {
    override fun isFallback(): Boolean = true
}