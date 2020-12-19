package by.shostko.errors

import org.json.JSONObject

fun ErrorCode.asFallback(): ErrorCode = FallbackDelegate(this)

internal class FallbackDelegate(
    private val code: ErrorCode
) : ErrorCode by code, ErrorCode.Serializable {

    companion object {
        const val SERIALIZATION_KEY: String = "FallbackDelegate"
        fun deserialize(json: JSONObject) {
            json.requireCodeSerializationKey(SERIALIZATION_KEY)
            FallbackDelegate(deserializeErrorCode(json.getString("wrapped")))
        }
    }

    override fun isFallback(): Boolean = true
    override fun serialize(): String = JSONObject()
        .put("wrapped", serializeErrorCode(code))
        .put(CodeSerializationKey, SERIALIZATION_KEY)
        .toString()
}