@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package by.shostko.errors

import android.content.Context
import androidx.annotation.StringRes

sealed class Error(val code: ErrorCode, cause: Throwable?) : Throwable(cause) {

    companion object {
        internal var config: Config = BaseConfig()
        fun init(config: Config) {
            this.config = config
        }

        fun cast(throwable: Throwable): Error = throwable as? Error ?: Unexpected(throwable)
        fun materialize(throwable: Throwable): Error = throwable as? Error ?: Materialized(throwable)
        fun wrap(throwable: Throwable, code: ErrorCode): Error = Child(code, throwable)
        fun wrap(throwable: Throwable, id: String): Error = Child(SimpleErrorCode(id), throwable)
        fun wrap(throwable: Throwable, id: String, message: String?): Error = Child(SimpleErrorCode(id, message), throwable)
        fun wrap(throwable: Throwable, id: String, @StringRes resId: Int): Error = Child(ResErrorCode(id, resId), throwable)
        fun wrap(throwable: Throwable, id: String, @StringRes resId: Int, vararg args: Any): Error = Child(FormattedResErrorCode(id, resId, args), throwable)
        fun custom(code: ErrorCode): Error = Custom(code)
        fun custom(id: String): Error = Custom(SimpleErrorCode(id))
        fun custom(id: String, message: String?): Error = Custom(SimpleErrorCode(id, message))
        fun custom(id: String, @StringRes resId: Int): Error = Custom(ResErrorCode(id, resId))
        fun custom(id: String, @StringRes resId: Int, vararg args: Any): Error = Custom(FormattedResErrorCode(id, resId, args))
    }

    open fun id(): String = code.id()

    fun original(): Throwable = when (val cause = cause) {
        null -> this
        is Error -> cause.original()
        else -> cause
    }

    fun stack(): String? = StringBuilder().apply {
        append(this@Error.toString())
        var tmp: Throwable? = cause
        while (tmp != null) {
            append(" => ")
            if (tmp is Error) {
                append(tmp.toString())
            } else {
                append(tmp.javaClass.simpleName)
                append('(')
                append(tmp.id())
                append("; ")
                append(tmp.message)
                append(')')
            }
            tmp = tmp.cause
        }
    }.toString()

    open fun text(context: Context): CharSequence {
        var (message, _) = message(context)
        if (config.isDebug && message.isNullOrBlank()) {
            message = super.message
        }
        if (message.isNullOrBlank()) {
            message = config.unknownError(context, cause)
        }
        return if (config.shouldAddErrorId) config.addErrorId(id(), message) else message
    }

    protected open fun message(context: Context): Pair<CharSequence?, Boolean> = code.message(context) to code.isFallback()

    override fun toString(): String = "${code.domain()}(${code.id()}; ${code.log()})"

    private class Materialized(override val cause: Throwable) : Error(SimpleErrorCode(cause.javaClass, cause.localizedMessage), cause)

    class Unexpected(override val cause: Throwable) : Error(SimpleErrorCode(cause.javaClass), cause)

    class Custom(code: ErrorCode) : Error(code, null)

    class Child(code: ErrorCode, override val cause: Throwable) : Error(code, cause) {
        override fun id(): String = "${super.id()}-${cause.id()}"
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

object NoError : Error(SimpleErrorCode("X"), null) {
    override fun text(context: Context): CharSequence = ""
    override fun toString(): String = "NoError"
}

object UnknownError : Error(SimpleErrorCode("UE", null, "UnknownError"), null) {
    override fun toString(): String = "UnknownError"
}

private fun Throwable.id(): String = if (this is Error) id() else DomainToIdMapper(javaClass.simpleName)