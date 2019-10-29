@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package by.shostko.errors

import android.content.Context
import androidx.annotation.StringRes

sealed class Error(val code: ErrorCode, cause: Throwable?) : Throwable(cause) {

    companion object {
        private var config: Config = BaseConfig()
        fun init(config: Config) {
            this.config = config
        }

        fun cast(throwable: Throwable): Error = throwable as? Error ?: Unexpected(throwable)
        fun wrap(throwable: Throwable, code: ErrorCode): Error = Child(code, cast(throwable))
        fun wrap(throwable: Throwable, id: String): Error = Child(SimpleErrorCode(id), cast(throwable))
        fun wrap(throwable: Throwable, id: String, message: String?): Error = Child(SimpleErrorCode(id, message), cast(throwable))
        fun wrap(throwable: Throwable, id: String, @StringRes resId: Int): Error = Child(ResErrorCode(id, resId), cast(throwable))
        fun wrap(throwable: Throwable, id: String, @StringRes resId: Int, vararg args: Any): Error = Child(FormattedResErrorCode(id, resId, args), cast(throwable))
        fun custom(code: ErrorCode): Error = Custom(code)
        fun custom(id: String): Error = Custom(SimpleErrorCode(id))
        fun custom(id: String, message: String?): Error = Custom(SimpleErrorCode(id, message))
        fun custom(id: String, @StringRes resId: Int): Error = Custom(ResErrorCode(id, resId))
        fun custom(id: String, @StringRes resId: Int, vararg args: Any): Error = Custom(FormattedResErrorCode(id, resId, args))
    }

    open fun id(): String = code.id()
    protected open fun message(context: Context): String? = code.message(context)
    private fun original(): String? = cause?.let { if (it is Error) it.original() else it.message }
    override fun toString(): String = "Error(${id()}; ${original()})"

    open fun text(context: Context): CharSequence {
        var message = message(context)
        if (config.isDebug && message.isNullOrBlank()) {
            message = super.message
        }
        if (message.isNullOrBlank()) {
            message = config.unknownError(context)
        }
        val result: String = message.toString()
        return if (config.shouldAddErrorId) config.addErrorId(id(), result) else result
    }

    open class Unexpected(cause: Throwable) : Error(SimpleErrorCode(cause::class.java, null, null), cause)

    open class Custom(code: ErrorCode) : Error(code, null)

    open class Child(code: ErrorCode, override val cause: Error) : Error(code, cause) {
        override fun id(): String = "${super.id()}-${cause.id()}"
        override fun message(context: Context): String? = cause.message(context).let { if (it.isNullOrBlank()) super.message(context) else it }
    }
}

object NoError : Error(EmptyErrorCode, null) {
    override fun text(context: Context): CharSequence = ""
    override fun toString(): String = "NoError"
}

abstract class ErrorCode private constructor(private val idProvider: (ErrorCode) -> String) {
    constructor(id: String) : this({ id })
    constructor(domain: String, value: Any? = null) : this({ concat(domain, value) })
    constructor(domain: Class<*>, value: Any? = null) : this({ concat(domain.simpleName.removeSuffix(), value) })
    constructor(value: Any? = null) : this({ concat(it::class.java.simpleName.removeSuffix(), value) })

    companion object {
        private val SUFFIX: String = ErrorCode::class.java.simpleName
        private fun String.removeSuffix(): String = removeSuffix(SUFFIX)
        fun concat(domain: String, value: Any?): String = domain.filter { it.isUpperCase() }.let { if (value == null) it else "$it$value" }
    }

    abstract fun message(context: Context): String?
    fun id(): String = idProvider(this)
}

object EmptyErrorCode : ErrorCode("X") {
    override fun message(context: Context): String? = null
}

open class SimpleErrorCode : ErrorCode {
    constructor(id: String) : super(id)
    constructor(domain: String, value: Any? = null) : super(domain, value)
    constructor(domain: Class<*>, value: Any? = null) : super(domain, value)
    constructor(value: Any? = null) : super(value)
    constructor(id: String, text: String?) : super(id) {
        this.text = text
    }

    constructor(domain: String, value: Any? = null, text: String?) : super(domain, value) {
        this.text = text
    }

    constructor(domain: Class<*>, value: Any? = null, text: String?) : super(domain, value) {
        this.text = text
    }

    constructor(value: Any?, text: String?) : super(value) {
        this.text = text
    }

    private var text: String? = null
    override fun message(context: Context): String? = text
}

open class ResErrorCode : ErrorCode {
    constructor(id: String, @StringRes resId: Int) : super(id) {
        this.resId = resId
    }

    constructor(domain: String, value: Any? = null, @StringRes resId: Int) : super(domain, value) {
        this.resId = resId
    }

    constructor(domain: Class<*>, value: Any? = null, @StringRes resId: Int) : super(domain, value) {
        this.resId = resId
    }

    constructor(value: Any?, @StringRes resId: Int) : super(value) {
        this.resId = resId
    }

    constructor(@StringRes resId: Int) : super(null) {
        this.resId = resId
    }

    private var resId: Int
    override fun message(context: Context): String? = context.getString(resId)
}

open class FormattedResErrorCode : ErrorCode {
    constructor(id: String, @StringRes resId: Int, vararg args: Any) : super(id) {
        this.resId = resId
        this.args = args
    }

    constructor(domain: String, value: Any? = null, @StringRes resId: Int, vararg args: Any) : super(domain, value) {
        this.resId = resId
        this.args = args
    }

    constructor(domain: Class<*>, value: Any? = null, @StringRes resId: Int, vararg args: Any) : super(domain, value) {
        this.resId = resId
        this.args = args
    }

    constructor(value: Any?, @StringRes resId: Int, vararg args: Any) : super(value) {
        this.resId = resId
        this.args = args
    }

    constructor(@StringRes resId: Int, vararg args: Any) : super(null) {
        this.resId = resId
        this.args = args
    }

    private val resId: Int
    private val args: Array<out Any>
    override fun message(context: Context): String? = context.getString(resId, *args)
}