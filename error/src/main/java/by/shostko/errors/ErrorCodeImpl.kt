@file:Suppress("unused")

package by.shostko.errors

import android.content.Context
import androidx.annotation.StringRes

internal abstract class AbsErrorCode(
    private val id: String,
    private val domain: String?,
    private val fallback: Boolean,
    private val logMessage: String?
) : ErrorCode {
    override fun isFallback(): Boolean = fallback
    override fun domain(): String = domain ?: Error.config.defaultDomain
    override fun log(): String = logMessage ?: Error.config.nullLog
    override fun id(): String = id
}

internal class BaseResErrorCode(
    id: String,
    domain: String?,
    fallback: Boolean,
    logMessage: String?,
    @StringRes private val messageResId: Int
) : AbsErrorCode(id, domain, fallback, logMessage) {
    override fun message(context: Context): CharSequence? = context.getString(messageResId)
}

internal class BaseFormattedResErrorCode(
    id: String,
    domain: String?,
    fallback: Boolean,
    logMessage: String?,
    @StringRes private val messageResId: Int,
    private val args: Array<out Any>
) : AbsErrorCode(id, domain, fallback, logMessage) {
    override fun message(context: Context): CharSequence? = context.getString(messageResId, *args)
}

internal class StaticMessageErrorCode(
    id: String,
    domain: String?,
    fallback: Boolean,
    logMessage: String?,
    private val message: CharSequence?
) : AbsErrorCode(id, domain, fallback, logMessage) {
    override fun message(context: Context): CharSequence? = message
}