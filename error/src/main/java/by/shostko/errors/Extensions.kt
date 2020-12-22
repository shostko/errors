@file:Suppress("unused")

package by.shostko.errors

import android.content.Context
import androidx.annotation.StringRes

fun Error.asThrowable(): Throwable? = if (this is NoError) null else this
fun Throwable.asError(): Error = Error.cast(this)
fun Throwable.materialize(): Error = Error.materialize(this)

fun Throwable.wrap(code: ErrorCode): Error = Error.wrap(this, code)
fun Throwable.wrap(builder: ErrorCode.Builder.() -> Unit): Error = Error.wrap(this, builder)
fun Throwable.wrap(id: Identifier): Error = Error.wrap(this, id)
fun Throwable.wrap(id: Identifier, message: MessageProvider): Error = Error.wrap(this, id, message)
fun Throwable.wrap(id: Identifier, message: String?): Error = Error.wrap(this, id, message)
fun Throwable.wrap(id: Identifier, @StringRes resId: Int): Error = Error.wrap(this, id, resId)
fun Throwable.wrap(id: Identifier, @StringRes resId: Int, vararg args: Any): Error = Error.wrap(this, id, resId, args)

fun Throwable.errorIdFull(): String = when (this) {
    is Error -> code.id().full()
    is ReplicaThrowable -> domain
    else -> javaClass.toDomain()
}

fun Throwable.errorIdShort(): String = when (this) {
    is Error -> code.id().short()
    is ReplicaThrowable -> domain.domainToId()
    else -> javaClass.toDomain().domainToId()
}

fun Throwable.errorMessageLog(): String = when (this) {
    is Error -> code.log()
    else -> message ?: Error.config.nullLog
}

fun Throwable.errorMessage(context: Context): CharSequence? = if (this === NoError) null else Error.cast(this).text(context)
