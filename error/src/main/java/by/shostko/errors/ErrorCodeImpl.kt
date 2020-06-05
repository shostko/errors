@file:Suppress("unused")

package by.shostko.errors

import android.content.Context
import androidx.annotation.StringRes

open class BaseErrorCode(
    private val idProvider: () -> String,
    private val domainProvider: () -> String,
    private val logMessageProvider: () -> String,
    private val extraProvider: (() -> Any?)?,
    private val messageProvider: ((Context) -> CharSequence?)?,
    private val fallbackProvider: () -> Boolean
) : ErrorCode {
    override fun domain(): String = domainProvider.invoke()
    override fun log(): String = logMessageProvider.invoke()
    override fun isFallback(): Boolean = fallbackProvider.invoke()
    override fun message(context: Context): CharSequence? = messageProvider?.invoke(context)
    override fun id(): String {
        val id = idProvider.invoke()
        val extra = extraProvider?.invoke()
        return if (extra == null) id else "$id$extra"
    }
}

open class SimpleErrorCode(
    private val id: String,
    private val message: CharSequence? = null,
    private val domain: String = id,
    private val logMessage: String = message.toString(),
    private val fallback: Boolean = false
) : ErrorCode {

    constructor(
        domain: Class<*>,
        message: CharSequence? = null,
        logMessage: String = message.toString(),
        fallback: Boolean = false
    ) : this(
        id = DomainToIdMapper(domain.simpleName),
        message = message,
        domain = domain.simpleName,
        logMessage = logMessage,
        fallback = fallback
    )

    override fun id(): String = id
    override fun domain(): String = domain
    override fun log(): String = logMessage
    override fun isFallback(): Boolean = fallback
    override fun message(context: Context): CharSequence? = message
}

open class ResErrorCode(
    private val id: String,
    @StringRes private val messageResId: Int,
    private val domain: String = id,
    private val logMessage: String = Error.config.getResourceName(messageResId),
    private val fallback: Boolean = false
) : ErrorCode {

    constructor(
        domain: Class<*>,
        @StringRes messageResId: Int,
        logMessage: String = Error.config.getResourceName(messageResId),
        fallback: Boolean = false
    ) : this(
        id = DomainToIdMapper(domain.simpleName),
        messageResId = messageResId,
        domain = domain.simpleName,
        logMessage = logMessage,
        fallback = fallback
    )

    override fun id(): String = id
    override fun domain(): String = domain
    override fun log(): String = logMessage
    override fun isFallback(): Boolean = fallback
    override fun message(context: Context): CharSequence? = context.getString(messageResId)
}

open class FormattedResErrorCode(
    private val id: String,
    @StringRes private val messageResId: Int,
    private val args: Array<out Any>,
    private val domain: String = id,
    private val logMessage: String = Error.config.getResourceName(messageResId),
    private val fallback: Boolean = false
) : ErrorCode {

    constructor(
        id: String,
        @StringRes messageResId: Int,
        arg: Any,
        domain: String,
        logMessage: String = Error.config.getResourceName(messageResId),
        fallback: Boolean = false
    ) : this(
        id = id,
        messageResId = messageResId,
        args = arrayOf(arg),
        domain = domain,
        logMessage = logMessage,
        fallback = fallback
    )

    constructor(
        domain: Class<*>,
        @StringRes messageResId: Int,
        args: Array<out Any>,
        logMessage: String = Error.config.getResourceName(messageResId),
        fallback: Boolean = false
    ) : this(
        id = DomainToIdMapper(domain.simpleName),
        messageResId = messageResId,
        args = args,
        domain = domain.simpleName,
        logMessage = logMessage,
        fallback = fallback
    )

    constructor(
        domain: Class<*>,
        @StringRes messageResId: Int,
        arg: Any,
        logMessage: String = Error.config.getResourceName(messageResId),
        fallback: Boolean = false
    ) : this(
        id = DomainToIdMapper(domain.simpleName),
        messageResId = messageResId,
        args = arrayOf(arg),
        domain = domain.simpleName,
        logMessage = logMessage,
        fallback = fallback
    )

    override fun id(): String = id
    override fun domain(): String = domain
    override fun log(): String = logMessage
    override fun isFallback(): Boolean = fallback
    override fun message(context: Context): CharSequence? = context.getString(messageResId, args)
}