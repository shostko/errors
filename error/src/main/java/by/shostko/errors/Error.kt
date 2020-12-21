@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package by.shostko.errors

import android.content.Context
import androidx.annotation.StringRes

sealed class Error(val code: ErrorCode, cause: Throwable?) : Throwable(null, cause) {

    companion object {
        internal var config: Config = Config.Default
        fun configure(context: Context, isDebug: Boolean) = configure(Config.Base(context, isDebug))
        fun configure(func: Config.Builder.() -> Unit) = configure(Config.build(func))
        fun configure(config: Config) {
            this.config = config
        }

        fun cast(throwable: Throwable): Error = throwable as? Error ?: Unexpected(throwable)
        fun materialize(throwable: Throwable): Error = throwable as? Error ?: Materialized(throwable)
        fun wrap(throwable: Throwable, code: ErrorCode): Error = Child(code, throwable)
        fun wrap(throwable: Throwable, code: EnumErrorCode): Error = Child(code, throwable)
        fun wrap(throwable: Throwable, builder: ErrorCode.Builder.() -> Unit): Error = Child(ErrorCode.build(builder), throwable)
        fun wrap(throwable: Throwable, id: Identifier): Error = Child(NoMessageErrorCode(id), throwable)
        fun wrap(throwable: Throwable, id: Identifier, message: MessageProvider): Error = Child(InternalErrorCode(id, message), throwable)
        fun wrap(throwable: Throwable, id: Identifier, message: String?): Error = Child(SimpleErrorCode(id, message), throwable)
        fun wrap(throwable: Throwable, id: Identifier, @StringRes resId: Int): Error = Child(ResErrorCode(id, resId), throwable)
        fun wrap(throwable: Throwable, id: Identifier, @StringRes resId: Int, vararg args: Any): Error = Child(FormattedResErrorCode(id, resId, args), throwable)
        fun custom(code: ErrorCode): Error = Custom(code)
        fun custom(code: EnumErrorCode): Error = Custom(code)
        fun custom(builder: ErrorCode.Builder.() -> Unit): Error = Custom(ErrorCode.build(builder))
        fun custom(id: Identifier): Error = Custom(NoMessageErrorCode(id))
        fun custom(id: Identifier, message: MessageProvider): Error = Custom(InternalErrorCode(id, message))
        fun custom(id: Identifier, message: String?): Error = Custom(SimpleErrorCode(id, message))
        fun custom(id: Identifier, @StringRes resId: Int): Error = Custom(ResErrorCode(id, resId))
        fun custom(id: Identifier, @StringRes resId: Int, vararg args: Any): Error = Custom(FormattedResErrorCode(id, resId, args))
    }

    fun short(): String = StringBuilder().apply {
        var tmp: Throwable? = this@Error
        while (tmp is Error) {
            append(tmp.code.id().short())
            tmp = if (tmp is Materialized || tmp is Unexpected) null else tmp.cause
            if (length > 0 && tmp != null) {
                append('-')
            }
        }
        if (tmp != null) {
            append(config.domainToId(tmp::class.java.simpleName))
        }
    }.toString()

    fun full(): String = StringBuilder().apply {
        var tmp: Throwable? = this@Error
        while (tmp is Error) {
            append(tmp.code.id().full())
            tmp = if (tmp is Materialized || tmp is Unexpected) null else tmp.cause
            if (length > 0 && tmp != null) {
                append(" - ")
            }
        }
        if (tmp != null) {
            append(tmp::class.java.simpleName)
        }
    }.toString()

    fun stack(): String = StringBuilder().apply {
        append(this@Error.toString())
        var tmp: Throwable? = cause
        while (tmp != null) {
            append('\n')
            append(tmp.asString())
            tmp = tmp.cause
        }
    }.toString()

    fun original(): Throwable = when (val cause = cause) {
        null -> this
        is Error -> cause.original()
        else -> cause
    }

    open fun text(context: Context): CharSequence {
        var (message, _) = message(context)
        if (config.isDebug && message.isNullOrBlank()) {
            message = original().asString()
        }
        if (message.isNullOrBlank()) {
            message = config.unknownError(context, cause)
        }
        return if (config.shouldAddErrorId) config.addErrorId(this, message) else message
    }

    internal open fun message(context: Context): Pair<CharSequence?, Boolean> = code.message(context) to code.isFallback()

    override fun toString(): String = "${code.id().full()}(${code.log()})"

    inline fun <reified T> hasCause(): Boolean = hasCause { this is T }

    fun hasCause(predicate: Throwable.() -> Boolean): Boolean {
        var tmp: Throwable? = this
        while (tmp != null) {
            if (tmp.predicate()) {
                return true
            }
            tmp = tmp.cause
        }
        return false
    }

    inline fun <reified T> hasCode(): Boolean = hasCode { this is T }

    fun hasCode(code: ErrorCode): Boolean = hasCode { this == code }

    fun hasCode(id: Identifier): Boolean = hasCode { this.id() == id }

    fun hasCode(predicate: ErrorCode.() -> Boolean): Boolean {
        var tmp: Error? = this
        while (tmp != null) {
            if (tmp.code.predicate()) {
                return true
            }
            tmp = tmp.cause as? Error
        }
        return false
    }

    private class Materialized(cause: Throwable) : Error(SimpleErrorCode(cause::class.java, cause.localizedMessage), cause)

    class Unexpected(cause: Throwable) : Error(NoMessageErrorCode(cause::class.java), cause)

    open class Custom(code: ErrorCode) : Error(code, null) {
        final override fun text(context: Context): CharSequence = super.text(context)
        final override fun toString(): String = super.toString()
    }

    class Child(code: ErrorCode, override val cause: Throwable) : Error(code, cause) {
        override fun message(context: Context): Pair<CharSequence?, Boolean> =
            if (cause is Error) {
                val parent = cause.message(context)
                val (message, fallback) = parent
                if (message.isNullOrBlank()) {
                    super.message(context)
                } else if (!fallback) {
                    parent
                } else {
                    val own = super.message(context)
                    if (own.first.isNullOrBlank()) parent else own
                }
            } else {
                super.message(context)
            }
    }
}

object NoError : Error(NoMessageErrorCode("X", "NoError"), null) {
    override fun text(context: Context): CharSequence = ""
    override fun toString(): String = "NoError"
}

object UnknownError : Error(NoMessageErrorCode("UE", "UnknownError"), null) {
    override fun toString(): String = "UnknownError"
}

private fun Throwable.asString(): String = when (this) {
    is Error -> this.toString()
    is ReplicaThrowable -> this.toString()
    else -> "${javaClass.simpleName}($message)"
}