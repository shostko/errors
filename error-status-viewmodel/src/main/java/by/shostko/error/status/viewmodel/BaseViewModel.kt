@file:Suppress("unused")

package by.shostko.error.status.viewmodel

import androidx.annotation.StringRes
import by.shostko.error.status.ErrorStatusFactory
import by.shostko.errors.*
import by.shostko.statushandler.Status
import by.shostko.statushandler.viewmodel.MultiProcessingViewModel
import by.shostko.statushandler.viewmodel.ProcessingViewModel

open class BaseProcessingViewModel private constructor(mapper: ((Throwable) -> Error)?) : ProcessingViewModel<Error>(NoError, mapper?.let { ErrorStatusFactory(it) }) {
    constructor() : this(null)
    constructor(clazz: Class<*>) : this({ it.wrap { Identifier.Simple(clazz) } })
    constructor(code: ErrorCode) : this({ it.wrap(code) })
    constructor(id: Identifier) : this({ it.wrap(id) })
    constructor(id: Identifier, message: MessageProvider) : this({ it.wrap(id, message) })
    constructor(id: Identifier, message: String?) : this({ it.wrap(id, message) })
    constructor(id: Identifier, @StringRes resId: Int) : this({ it.wrap(id, resId) })
    constructor(id: Identifier, @StringRes resId: Int, vararg args: Any) : this({ it.wrap(id, resId, args) })

    override fun requireFactory(): Status.Factory<Error> = ErrorStatusFactory { it.wrap { Identifier.Simple(this@BaseProcessingViewModel.javaClass) } }
}

open class BaseMultiProcessingViewModel private constructor(mapper: ((Throwable) -> Error)?) : MultiProcessingViewModel<Error>(NoError, mapper?.let { ErrorStatusFactory(it) }) {
    constructor() : this(null)
    constructor(clazz: Class<*>) : this({ it.wrap { Identifier.Simple(clazz) } })
    constructor(code: ErrorCode) : this({ it.wrap(code) })
    constructor(id: Identifier) : this({ it.wrap(id) })
    constructor(id: Identifier, message: MessageProvider) : this({ it.wrap(id, message) })
    constructor(id: Identifier, message: String?) : this({ it.wrap(id, message) })
    constructor(id: Identifier, @StringRes resId: Int) : this({ it.wrap(id, resId) })
    constructor(id: Identifier, @StringRes resId: Int, vararg args: Any) : this({ it.wrap(id, resId, args) })

    override fun requireFactory(): Status.Factory<Error> = ErrorStatusFactory { it.wrap { Identifier.Simple(this@BaseMultiProcessingViewModel.javaClass) } }
}