package by.shostko.errors

import android.content.Context
import by.shostko.errors.Identifier.Companion.getIdentifier
import by.shostko.errors.Identifier.Companion.put
import by.shostko.errors.MessageProvider.Companion.canBeSerialized
import by.shostko.errors.MessageProvider.Companion.getMessageProvider
import by.shostko.errors.MessageProvider.Companion.put
import org.json.JSONObject

open class InternalErrorCode(
    private val identifier: Identifier,
    private val provider: MessageProvider,
    private val fallback: Boolean = false
) : ErrorCode, ErrorCode.Serializable {

    final override fun id(): Identifier = identifier
    final override fun log(): String = provider.log()
    final override fun message(context: Context): CharSequence? = provider.message(context)
    final override fun isFallback(): Boolean = fallback

    final override fun serialize(): String = JSONObject()
        .put(CodeSerializationKey, SERIALIZATION_KEY)
        .put(identifier)
        .put(provider)
        .put("fallback", fallback)
        .toString()

    internal val canBeSerialized: Boolean
        get() = provider.canBeSerialized

    companion object {
        internal const val SERIALIZATION_KEY: String = "InternalErrorCode"
        internal fun deserialize(json: JSONObject): ErrorCode {
            json.requireCodeSerializationKey(SERIALIZATION_KEY)
            return InternalErrorCode(
                identifier = json.getIdentifier(),
                provider = json.getMessageProvider(),
                fallback = json.getBoolean("fallback")
            )
        }
    }
}