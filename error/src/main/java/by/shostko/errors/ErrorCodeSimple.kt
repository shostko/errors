package by.shostko.errors

import android.content.Context
import androidx.annotation.StringRes

open class SimpleErrorCode(
    private val id: String,
    private val domain: String,
    private val message: CharSequence? = null,
    private val fallback: Boolean = false
) : ErrorCode {

    constructor(
        id: String,
        domain: Class<*>,
        message: CharSequence? = null,
        fallback: Boolean = false
    ) : this(
        id = id,
        domain = domain.simpleName,
        message = message,
        fallback = fallback
    )

    override fun id(): String = id
    override fun domain(): String = domain
    override fun log(): String = message?.let { Error.config.messageToLog(it) } ?: Error.config.nullLog
    override fun isFallback(): Boolean = fallback
    override fun message(context: Context): CharSequence? = message
}

open class ResErrorCode(
    private val id: String,
    private val domain: String,
    @StringRes private val messageResId: Int,
    private val fallback: Boolean = false
) : ErrorCode {

    constructor(
        id: String,
        domain: Class<*>,
        @StringRes messageResId: Int,
        fallback: Boolean = false
    ) : this(
        id = id,
        domain = domain.simpleName,
        messageResId = messageResId,
        fallback = fallback
    )

    override fun id(): String = id
    override fun domain(): String = domain
    override fun log(): String = Error.config.messageToLog(messageResId)
    override fun isFallback(): Boolean = fallback
    override fun message(context: Context): CharSequence? = context.getString(messageResId)
}

open class FormattedResErrorCode(
    private val id: String,
    private val domain: String,
    @StringRes private val messageResId: Int,
    private val args: Array<out Any>,
    private val fallback: Boolean = false
) : ErrorCode {

    constructor(
        id: String,
        domain: Class<*>,
        @StringRes messageResId: Int,
        args: Array<out Any>,
        fallback: Boolean = false
    ) : this(
        id = id,
        domain = domain.simpleName,
        messageResId = messageResId,
        args = args,
        fallback = fallback
    )

    override fun id(): String = id
    override fun domain(): String = domain
    override fun log(): String = Error.config.messageToLog(messageResId, args)
    override fun isFallback(): Boolean = fallback
    override fun message(context: Context): CharSequence? = context.getString(messageResId, *args)
}