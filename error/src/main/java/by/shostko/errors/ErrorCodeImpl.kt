@file:Suppress("unused")

package by.shostko.errors

import android.content.Context
import androidx.annotation.StringRes
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

internal abstract class AbsErrorCode(
    private val id: String,
    private val domain: String?,
    private val fallback: Boolean,
    private val logMessage: String?
) : ErrorCode, ErrorCode.Serializable {
    override fun isFallback(): Boolean = fallback
    override fun domain(): String = domain ?: Error.config.defaultDomain
    override fun log(): String = logMessage ?: Error.config.nullLog
    override fun id(): String = id
    protected fun superJsonObject(): JSONObject = JSONObject()
        .put("id", id)
        .put("domain", domain)
        .put("fallback", fallback)
        .put("log", logMessage)
}

internal class StaticMessageErrorCode(
    id: String,
    domain: String?,
    fallback: Boolean,
    logMessage: String?,
    private val message: CharSequence?
) : AbsErrorCode(id, domain, fallback, logMessage) {

    companion object {
        const val SERIALIZATION_KEY: String = "StaticMessageErrorCode"
        fun deserialize(json: JSONObject) = StaticMessageErrorCode(
            id = json.getString("id"),
            domain = json.getStringOrNull("domain"),
            fallback = json.getBoolean("fallback"),
            logMessage = json.getStringOrNull("log"),
            message = json.getStringOrNull("message")
        )
    }

    override fun message(context: Context): CharSequence? = message

    override fun serialize(): String = superJsonObject()
        .put("message", message?.let { JSONObject.quote(it.toString()) })
        .put(CodeSerializationKey, SERIALIZATION_KEY)
        .toString()
}

internal class BaseResErrorCode(
    id: String,
    domain: String?,
    fallback: Boolean,
    logMessage: String?,
    @StringRes private val messageResId: Int
) : AbsErrorCode(id, domain, fallback, logMessage) {

    companion object {
        const val SERIALIZATION_KEY: String = "BaseResErrorCode"
        fun deserialize(json: JSONObject) = BaseResErrorCode(
            id = json.getString("id"),
            domain = json.getStringOrNull("domain"),
            fallback = json.getBoolean("fallback"),
            logMessage = json.getStringOrNull("log"),
            messageResId = Error.config.resourceNameToId(json.getString("messageResId"))
        )
    }

    override fun message(context: Context): CharSequence? = context.getString(messageResId)

    override fun serialize(): String = superJsonObject()
        .put("messageResId", Error.config.resourceIdToName(messageResId))
        .put(CodeSerializationKey, SERIALIZATION_KEY)
        .toString()
}

internal class BaseFormattedResErrorCode(
    id: String,
    domain: String?,
    fallback: Boolean,
    logMessage: String?,
    @StringRes private val messageResId: Int,
    private val args: Array<out Any>
) : AbsErrorCode(id, domain, fallback, logMessage) {

    companion object {
        const val SERIALIZATION_KEY: String = "BaseFormattedResErrorCode"
        fun deserialize(json: JSONObject) = BaseFormattedResErrorCode(
            id = json.getString("id"),
            domain = json.getStringOrNull("domain"),
            fallback = json.getBoolean("fallback"),
            logMessage = json.getStringOrNull("log"),
            messageResId = Error.config.resourceNameToId(json.getString("messageResId")),
            args = json.getJSONArray("args").run { Array(length()) { get(it) } }
        )
    }

    override fun message(context: Context): CharSequence? = context.getString(messageResId, *args)

    override fun serialize(): String = superJsonObject()
        .put("messageResId", Error.config.resourceIdToName(messageResId))
        .put("args", JSONArray().apply { args.forEach { put(it) } })
        .put(CodeSerializationKey, SERIALIZATION_KEY)
        .toString()
}