package by.shostko.errors

import android.content.Context
import androidx.annotation.DrawableRes

open class ErrorCodeWrapped(
    private val wrapped: ErrorCode
) : ErrorCode {
    override fun id(): String = wrapped.id()
    override fun domain(): String = wrapped.domain()
    override fun log(): String = wrapped.log()
    override fun isFallback(): Boolean = wrapped.isFallback()
    override fun message(context: Context): CharSequence? = wrapped.message(context)
}