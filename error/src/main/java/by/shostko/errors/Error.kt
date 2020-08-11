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
            append(tmp.asString())
            tmp = tmp.cause
        }
    }.toString()

    open fun text(context: Context): CharSequence {
        var (message, _) = message(context)
        if (config.isDebug && message.isNullOrBlank()) {
            message = original().asString()
        }
        if (message.isNullOrBlank()) {
            message = config.unknownError(context, cause)
        }
        return if (config.shouldAddErrorId) config.addErrorId(id(), message) else message
    }

    internal open fun message(context: Context): Pair<CharSequence?, Boolean> = code.message(context) to code.isFallback()

    override fun toString(): String = asString()

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

    fun hasCode(id: String): Boolean = hasCode { this.id() == id }

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

    private class Materialized(override val cause: Throwable) : Error(SimpleErrorCode(cause.javaClass, cause.localizedMessage), cause)

    class Unexpected(override val cause: Throwable) : Error(SimpleErrorCode(cause.javaClass), cause)

    open class Custom(code: ErrorCode) : Error(code, null) {
        final override fun id(): String = super.id()
        final override fun text(context: Context): CharSequence = super.text(context)
        final override fun toString(): String = super.toString()
    }

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
private fun Error.asString(): String = "${code.domain()}(${code.id()}; ${code.log()})"
private fun Throwable.asString(): String =
    if (this is Error) {
        this.asString()
    } else {
        val domain = javaClass.simpleName
        val id = DomainToIdMapper(domain)
        "$domain($id; ${message})"
    }