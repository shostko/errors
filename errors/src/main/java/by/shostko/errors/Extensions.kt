@file:Suppress("unused")

package by.shostko.errors

import androidx.annotation.StringRes

fun Throwable.asError(): Error = Error.cast(this)

fun Throwable.wrap(code: ErrorCode): Error = Error.wrap(this, code)
fun Throwable.wrap(id: String): Error = Error.wrap(this, id)
fun Throwable.wrap(id: String, message: String?): Error = Error.wrap(this, id, message)
fun Throwable.wrap(id: String, @StringRes resId: Int): Error = Error.wrap(this, id, resId)
fun Throwable.wrap(id: String, @StringRes resId: Int, vararg args: Any): Error = Error.wrap(this, id, resId, args)