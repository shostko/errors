@file:Suppress("unused")

package by.shostko.errors

import androidx.annotation.DrawableRes

class DrawableResErrorCode(
    wrapped: ErrorCode,
    @DrawableRes private val drawableRes: Int
) : ErrorCodeWrapped(wrapped) {
    @DrawableRes
    fun drawableRes(): Int = drawableRes
}

fun ErrorCode.Builder.drawableRes(@DrawableRes drawableRes: Int): ErrorCode.Builder = wrap { DrawableResErrorCode(it, drawableRes) }

@DrawableRes
fun Error.drawableRes(): Int? {
    var res: Int? = null
    var tmp: Error? = this
    while (res == null && tmp != null) {
        res = (tmp.code as? DrawableResErrorCode)?.drawableRes()
        tmp = cause as? Error
    }
    return res
}