@file:Suppress("unused")

package by.shostko.error.status.viewmodel

import androidx.annotation.StringRes
import by.shostko.error.status.ErrorStatusFactory
import by.shostko.errors.*
import by.shostko.statushandler.Status
import by.shostko.statushandler.viewmodel.CustomViewModel

open class BaseViewModel private constructor(mapper: ((Throwable) -> Error)?) : CustomViewModel<Error>(NoError, mapper?.let { ErrorStatusFactory(it) }) {
    constructor() : this(null)
    constructor(clazz: Class<*>) : this({ it.wrap(SimpleErrorCode(clazz)) })
    constructor(code: ErrorCode) : this({ it.wrap(code) })
    constructor(id: String) : this({ it.wrap(id) })
    constructor(id: String, message: String?) : this({ it.wrap(id, message) })
    constructor(id: String, @StringRes resId: Int) : this({ it.wrap(id, resId) })
    constructor(id: String, @StringRes resId: Int, vararg args: Any) : this({ it.wrap(id, resId, args) })

    override fun requireFactory(): Status.Factory<Error> = ErrorStatusFactory { it.wrap(SimpleErrorCode(javaClass)) }
}