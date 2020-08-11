package by.shostko.errors

import android.content.Context
import androidx.annotation.AnyRes
import androidx.annotation.StringRes

internal class IdProvider(
    private var domainProvider: () -> String
) : () -> String {
    override fun invoke(): String = Error.config.domainToId(domainProvider())
}

internal object DomainToIdMapper : (String) -> String {
    override fun invoke(domain: String): String = Error.config.domainToId(domain)
}

internal object DefaultDomainProvider : () -> String {
    override fun invoke(): String = Error.config.defaultDomain
}

internal object NullLogProvider : () -> String {
    override fun invoke(): String = Error.config.nullLog
}

internal class LogMessageProvider(
    private val message: CharSequence
) : () -> String {
    override fun invoke(): String = Error.config.messageToLog(message)
}

internal class ResLogMessageProvider(
    @AnyRes
    private val resId: Int
) : () -> String {
    override fun invoke(): String = Error.config.messageToLog(resId)
}

internal class FormattedResLogMessageProvider(
    @AnyRes
    private val resId: Int,
    private val args: Array<out Any>
) : () -> String {
    override fun invoke(): String = Error.config.messageToLog(resId, args)
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