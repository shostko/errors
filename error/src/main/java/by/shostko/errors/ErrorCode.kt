@file:Suppress("MemberVisibilityCanBePrivate")

package by.shostko.errors

import android.content.Context
import androidx.annotation.StringRes

interface ErrorCode {
    fun id(): Identifier
    fun log(): String
    fun isFallback(): Boolean
    fun message(context: Context): CharSequence?

    companion object {
        fun build(block: Builder.() -> Unit): ErrorCode = Builder().apply(block).build()
        fun serialize(code: ErrorCode): String = serializeErrorCode(code)
        fun deserialize(str: String): ErrorCode = deserializeErrorCode(str)
        fun equals(code1: ErrorCode, code2: ErrorCode): Boolean = Identifier.equals(code1.id(), code2.id())
    }

    interface Serializable {
        fun serialize(): String
    }

    @Suppress("unused")
    class Builder {

        private var id: Identifier? = null
        private var fallback: Boolean = false
        private var log: String? = null
        private var noLog: Boolean = false
        private var message: MessageProvider = MessageProvider.Empty

        fun id(id: Identifier): Builder = apply {
            this.id = id
        }

        fun id(short: String, full: String): Builder = apply {
            this.id = Identifier.Simple(short, full)
        }

        fun id(full: String): Builder = apply {
            this.id = Identifier.Simple(full)
        }

        fun id(domain: Class<*>): Builder = apply {
            this.id = Identifier.Simple(domain.toDomain())
        }

        fun id(domain: Any): Builder = apply {
            this.id = Identifier.Simple(domain.toDomain())
        }

        fun id(domain: String, index: Int, description: String): Builder = apply {
            this.id = Identifier.Impl(domain, index, description)
        }

        fun id(domain: Class<*>, index: Int, description: String): Builder = apply {
            this.id = Identifier.Impl(domain, index, description)
        }

        fun id(domain: Any, index: Int, description: String): Builder = apply {
            this.id = Identifier.Impl(domain, index, description)
        }

        fun fallback(fallback: Boolean = true): Builder = apply {
            this.fallback = fallback
        }

        fun log(log: String): Builder = apply {
            this.log = log
        }

        fun noLog(): Builder = apply {
            this.noLog = true
        }

        fun message(message: MessageProvider): Builder = apply {
            this.message = message
        }

        fun message(message: CharSequence?): Builder = apply {
            this.message = MessageProvider.Direct(message)
        }

        fun message(@StringRes messageResId: Int): Builder = apply {
            message = MessageProvider.FromRes(messageResId)
        }

        fun message(@StringRes messageResId: Int, vararg args: Any): Builder = apply {
            message = MessageProvider.FromFormattedRes(messageResId, args)
        }

        fun build(): ErrorCode {
            val idLocal = id
            requireNotNull(idLocal) { "Identifier is required to build ErrorCode" }
            return InternalErrorCode(
                identifier = idLocal,
                provider = if (noLog) {
                    message.noLog()
                } else {
                    val logLocal = log
                    if (logLocal.isNullOrBlank()) {
                        message
                    } else {
                        message.withLog(logLocal)
                    }
                },
                fallback = fallback
            )
        }
    }
}