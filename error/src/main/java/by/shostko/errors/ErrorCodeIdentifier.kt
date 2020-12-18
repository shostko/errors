@file:Suppress("unused")

package by.shostko.errors

import android.content.Context
import androidx.annotation.StringRes
import org.json.JSONObject

interface ErrorCodeIdentifier {
    fun short(): String
    fun full(): String

    interface Abs : ErrorCodeIdentifier {
        val domain: String
        val index: Int
        val description: String

        override fun short(): String = domain.filter { it.isUpperCase() } + index
        override fun full(): String = "$domain.$description"
    }

    data class Impl(
        override val domain: String,
        override val index: Int,
        override val description: String
    ) : ErrorCodeIdentifier.Abs {
        constructor(domain: Class<*>, index: Int, description: String) : this(domain.simpleName, index, description)
        constructor(domain: Any, index: Int, description: String) : this(domain::class.java.simpleName, index, description)
    }

    data class Simple(
        val short: String,
        val full: String
    ) : ErrorCodeIdentifier {
        override fun short(): String = short
        override fun full(): String = full
    }

    companion object
}

@Suppress("RedundantNullableReturnType")
interface MessageProvider {
    fun log(): String
    fun message(context: Context): CharSequence?

    object Empty : MessageProvider {
        override fun log(): String = Error.config.nullLog
        override fun message(context: Context): CharSequence? = null
    }

    data class Direct(internal val message: String?) : MessageProvider {
        override fun log(): String = message?.let { Error.config.messageToLog(it) } ?: Error.config.nullLog
        override fun message(context: Context): CharSequence? = message
    }

    class FromRes(@StringRes internal val messageResId: Int) : MessageProvider {
        override fun log(): String = Error.config.messageToLog(messageResId)
        override fun message(context: Context): CharSequence? = context.getString(messageResId)
    }

    class FromFormattedRes(@StringRes internal val messageResId: Int, internal val args: Array<out Any>) : MessageProvider {
        override fun log(): String = Error.config.messageToLog(messageResId, args)
        override fun message(context: Context): CharSequence? = context.getString(messageResId, *args)
    }
}

fun ErrorCodeIdentifier.build(): ErrorCode = InternalErrorCode(this, MessageProvider.Empty)
fun ErrorCodeIdentifier.with(provider: MessageProvider): ErrorCode = InternalErrorCode(this, provider)
fun ErrorCodeIdentifier.with(message: String?): ErrorCode = InternalErrorCode(this, MessageProvider.Direct(message))
fun ErrorCodeIdentifier.with(messageResId: Int): ErrorCode = InternalErrorCode(this, MessageProvider.FromRes(messageResId))
fun ErrorCodeIdentifier.with(messageResId: Int, vararg args: Any): ErrorCode = InternalErrorCode(this, MessageProvider.FromFormattedRes(messageResId, args))