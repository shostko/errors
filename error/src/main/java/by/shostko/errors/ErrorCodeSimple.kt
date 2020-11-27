@file:Suppress("unused")

package by.shostko.errors

import android.content.Context
import androidx.annotation.StringRes
import org.json.JSONArray
import org.json.JSONObject

sealed class BaseSimpleErrorCode(
    private val id: String,
    private val domain: String
) : ErrorCode, ErrorCode.Serializable {
    final override fun id(): String = id
    final override fun domain(): String = domain
    final override fun isFallback(): Boolean = false

    fun asFallback(): ErrorCode = BaseFallbackDelegate(this)

    protected fun superJsonObject(): JSONObject = JSONObject()
        .put("id", id)
        .put("domain", domain)
}

open class NoMessageErrorCode(
    private val id: String,
    private val domain: String
) : BaseSimpleErrorCode(id, domain) {

    companion object {
        const val SERIALIZATION_KEY: String = "NoMessageErrorCode"
        fun deserialize(json: JSONObject) = NoMessageErrorCode(
            id = json.getString("id"),
            domain = json.getString("domain")
        )
    }

    constructor(
        id: String,
        domain: Class<*>
    ) : this(
        id = id,
        domain = domain.simpleName
    )

    constructor(
        domain: Class<*>
    ) : this(
        id = Error.config.domainToId(domain.simpleName),
        domain = domain.simpleName
    )

    constructor(
        domain: String
    ) : this(
        id = Error.config.domainToId(domain),
        domain = domain
    )

    final override fun log(): String = Error.config.nullLog
    final override fun message(context: Context): CharSequence? = null

    override fun serialize(): String = superJsonObject()
        .put(CodeSerializationKey, SERIALIZATION_KEY)
        .toString()
}

open class SimpleErrorCode(
    private val id: String,
    private val domain: String,
    private val message: CharSequence?
) : BaseSimpleErrorCode(id, domain) {

    companion object {
        const val SERIALIZATION_KEY: String = "SimpleErrorCode"
        fun deserialize(json: JSONObject) = SimpleErrorCode(
            id = json.getString("id"),
            domain = json.getString("domain"),
            message = json.getStringOrNull("message")
        )
    }

    constructor(
        id: String,
        domain: Class<*>,
        message: CharSequence?
    ) : this(
        id = id,
        domain = domain.simpleName,
        message = message
    )

    constructor(
        domain: Class<*>,
        message: CharSequence?
    ) : this(
        id = Error.config.domainToId(domain.simpleName),
        domain = domain.simpleName,
        message = message
    )

    constructor(
        domain: String,
        message: CharSequence?
    ) : this(
        id = Error.config.domainToId(domain),
        domain = domain,
        message = message
    )

    final override fun log(): String = message?.let { Error.config.messageToLog(it) } ?: Error.config.nullLog
    final override fun message(context: Context): CharSequence? = message

    override fun serialize(): String = superJsonObject()
        .put("message", message?.let { JSONObject.quote(it.toString()) })
        .put(CodeSerializationKey, SERIALIZATION_KEY)
        .toString()
}

open class ResErrorCode(
    private val id: String,
    private val domain: String,
    @StringRes private val messageResId: Int
) : BaseSimpleErrorCode(id, domain) {

    companion object {
        const val SERIALIZATION_KEY: String = "ResErrorCode"
        fun deserialize(json: JSONObject) = ResErrorCode(
            id = json.getString("id"),
            domain = json.getString("domain"),
            messageResId = Error.config.resourceNameToId(json.getString("messageResId"))
        )
    }

    constructor(
        id: String,
        domain: Class<*>,
        @StringRes messageResId: Int
    ) : this(
        id = id,
        domain = domain.simpleName,
        messageResId = messageResId
    )

    constructor(
        domain: Class<*>,
        @StringRes messageResId: Int
    ) : this(
        id = Error.config.domainToId(domain.simpleName),
        domain = domain.simpleName,
        messageResId = messageResId
    )

    constructor(
        domain: String,
        @StringRes messageResId: Int
    ) : this(
        id = Error.config.domainToId(domain),
        domain = domain,
        messageResId = messageResId
    )

    final override fun log(): String = Error.config.messageToLog(messageResId)
    final override fun message(context: Context): CharSequence? = context.getString(messageResId)

    override fun serialize(): String = superJsonObject()
        .put("messageResId", Error.config.resourceIdToName(messageResId))
        .put(CodeSerializationKey, SERIALIZATION_KEY)
        .toString()
}

open class FormattedResErrorCode(
    private val id: String,
    private val domain: String,
    @StringRes private val messageResId: Int,
    private vararg val args: Any?
) : BaseSimpleErrorCode(id, domain) {

    companion object {
        const val SERIALIZATION_KEY: String = "FormattedResErrorCode"
        fun deserialize(json: JSONObject) = FormattedResErrorCode(
            id = json.getString("id"),
            domain = json.getString("domain"),
            messageResId = Error.config.resourceNameToId(json.getString("messageResId")),
            args = json.getJSONArray("args").run { Array(length()) { get(it) } }
        )
    }

    constructor(
        id: String,
        domain: Class<*>,
        @StringRes messageResId: Int,
        vararg args: Any?
    ) : this(
        id = id,
        domain = domain.simpleName,
        messageResId = messageResId,
        args = args
    )

    constructor(
        domain: Class<*>,
        @StringRes messageResId: Int,
        vararg args: Any?
    ) : this(
        id = Error.config.domainToId(domain.simpleName),
        domain = domain.simpleName,
        messageResId = messageResId,
        args = args
    )

    constructor(
        domain: String,
        @StringRes messageResId: Int,
        vararg args: Any?
    ) : this(
        id = Error.config.domainToId(domain),
        domain = domain,
        messageResId = messageResId,
        args = args
    )

    final override fun log(): String = Error.config.messageToLog(messageResId, args)
    final override fun message(context: Context): CharSequence? = context.getString(messageResId, *args)

    override fun serialize(): String = superJsonObject()
        .put("messageResId", Error.config.resourceIdToName(messageResId))
        .put("args", JSONArray().apply { args.forEach { put(it) } })
        .put(CodeSerializationKey, SERIALIZATION_KEY)
        .toString()
}

internal class BaseFallbackDelegate(
    private val code: ErrorCode
) : ErrorCode by code, ErrorCode.Serializable {

    companion object {
        const val SERIALIZATION_KEY: String = "BaseFallbackDelegate"
        fun deserialize(json: JSONObject) = BaseFallbackDelegate(
            ErrorCode.deserialize(json.getString("wrapped"))
        )
    }

    override fun isFallback(): Boolean = true
    override fun serialize(): String = JSONObject()
        .put("wrapped", ErrorCode.serialize(code))
        .put(CodeSerializationKey, SERIALIZATION_KEY)
        .toString()
}