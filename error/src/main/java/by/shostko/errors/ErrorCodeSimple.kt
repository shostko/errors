@file:Suppress("unused")

package by.shostko.errors

import androidx.annotation.StringRes

open class NoMessageErrorCode(identifier: Identifier) : InternalErrorCode(identifier, MessageProvider.Empty) {
    constructor(id: String, domain: String) : this(Identifier.Simple(id, domain))
    constructor(id: String, domain: Class<*>) : this(Identifier.Simple(id, domain.toDomain()))
    constructor(domain: String) : this(Identifier.Simple(domain))
    constructor(domain: Class<*>) : this(Identifier.Simple(domain.toDomain()))
}

open class SimpleErrorCode(identifier: Identifier, message: CharSequence?) : InternalErrorCode(identifier, MessageProvider.Direct(message)) {
    constructor(id: String, domain: String, message: CharSequence?) : this(Identifier.Simple(id, domain), message)
    constructor(id: String, domain: Class<*>, message: CharSequence?) : this(Identifier.Simple(id, domain.toDomain()), message)
    constructor(domain: String, message: CharSequence?) : this(Identifier.Simple(domain), message)
    constructor(domain: Class<*>, message: CharSequence?) : this(Identifier.Simple(domain.toDomain()), message)
}

open class ResErrorCode(identifier: Identifier, @StringRes messageResId: Int) : InternalErrorCode(identifier, MessageProvider.FromRes(messageResId)) {
    constructor(id: String, domain: String, @StringRes messageResId: Int) : this(Identifier.Simple(id, domain), messageResId)
    constructor(id: String, domain: Class<*>, @StringRes messageResId: Int) : this(Identifier.Simple(id, domain.toDomain()), messageResId)
    constructor(domain: String, @StringRes messageResId: Int) : this(Identifier.Simple(domain), messageResId)
    constructor(domain: Class<*>, @StringRes messageResId: Int) : this(Identifier.Simple(domain.toDomain()), messageResId)
}

open class FormattedResErrorCode(identifier: Identifier, @StringRes messageResId: Int, args: Array<out Any?>) :
    InternalErrorCode(identifier, MessageProvider.FromFormattedRes(messageResId, args)) {
    constructor(id: String, domain: String, @StringRes messageResId: Int, vararg args: Any?) : this(Identifier.Simple(id, domain), messageResId, args)
    constructor(id: String, domain: Class<*>, @StringRes messageResId: Int, vararg args: Any?) : this(Identifier.Simple(id, domain.toDomain()), messageResId, args)
    constructor(domain: String, @StringRes messageResId: Int, vararg args: Any?) : this(Identifier.Simple(domain), messageResId, args)
    constructor(domain: Class<*>, @StringRes messageResId: Int, vararg args: Any?) : this(Identifier.Simple(domain.toDomain()), messageResId, args)
}