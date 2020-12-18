package by.shostko.errors

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

internal class InternalErrorCode(
    private val identifier: ErrorCodeIdentifier,
    private val provider: MessageProvider
) : ErrorCode, ErrorCode.Serializable {

    override fun id(): String = identifier.short()
    override fun domain(): String = identifier.full()
    override fun isFallback(): Boolean = false
    override fun log(): String = provider.log()
    override fun message(context: Context): CharSequence? = provider.message(context)

    override fun serialize(): String = JSONObject()
        .put(CodeSerializationKey, SERIALIZATION_KEY)
        .put(
            "identifier", JSONObject()
                .put("short", identifier.short())
                .put("full", identifier.full())
        )
        .run {
            when (provider) {
                MessageProvider.Empty -> this
                is MessageProvider.Direct -> put("message", provider.message)
                is MessageProvider.FromRes -> put("messageResId", Error.config.resourceIdToName(provider.messageResId))
                is MessageProvider.FromFormattedRes -> put("messageResId", Error.config.resourceIdToName(provider.messageResId))
                    .put("args", JSONArray().apply { provider.args.forEach { put(it) } })
                else -> throw UnsupportedOperationException("Can't serialize such MessageProvider: $provider")
            }
        }
        .toString()

    internal val canBeSerialized: Boolean
        get() = when (provider) {
            MessageProvider.Empty,
            is MessageProvider.Direct,
            is MessageProvider.FromRes,
            is MessageProvider.FromFormattedRes,
            is ErrorCode.Serializable -> true
            else -> false
        }

    companion object {
        const val SERIALIZATION_KEY: String = "InternalErrorCode"
        fun deserialize(json: JSONObject) = InternalErrorCode(
            identifier = json.getJSONObject("identifier").run {
                ErrorCodeIdentifier.Simple(
                    short = getString("short"),
                    full = getString("full")
                )
            },
            provider = if (json.has("message")) {
                MessageProvider.Direct(json.getString("message"))
            } else if (json.has("messageResId") && json.has("args")) {
                MessageProvider.FromFormattedRes(
                    messageResId = Error.config.resourceNameToId(json.getString("messageResId")),
                    args = json.getJSONArray("args").run { Array(length()) { get(it) } }
                )
            } else if (json.has("messageResId")) {
                MessageProvider.FromRes(Error.config.resourceNameToId(json.getString("messageResId")))
            } else {
                MessageProvider.Empty
            }
        )
    }
}