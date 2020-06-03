package by.shostko.errors

import android.content.Context

fun ErrorCode.cached(): ErrorCode = CachedErrorCode(this)

private class CachedErrorCode(
    private val wrapped: ErrorCode
) : ErrorCode {

    private val id by lazy { wrapped.id() }
    private val domain by lazy { wrapped.domain() }
    private val log by lazy { wrapped.log() }
    private val fallback by lazy { wrapped.isFallback() }

    override fun id(): String = id
    override fun domain(): String = domain
    override fun log(): String = log
    override fun isFallback(): Boolean = fallback
    override fun message(context: Context): CharSequence? = wrapped.message(context)
}