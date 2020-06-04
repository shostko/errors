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
        clazz: Class<*>,
        message: CharSequence? = null,
        logMessage: String = message.toString(),
        fallback: Boolean = false
    ) : this(
        id = DomainToIdMapper(clazz.simpleName),
        message = message,
        domain = clazz.simpleName,
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
    override fun id(): String = id
    override fun domain(): String = domain
    override fun log(): String = logMessage
    override fun isFallback(): Boolean = fallback
    override fun message(context: Context): CharSequence? = context.getString(messageResId, args)
}